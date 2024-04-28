package com.example.devtest;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> getImageLauncher;
    MatOP matOP=new MatOP();
    Polygon polygon=new Polygon();
    Mat src;
    ImageView img_before,img_after;
    SeekBar seekbar_blur,seekbar_sobel_threshold,seekbar_anchor_threshold,seekbar_min_dist,seekbar_poisson_r;
    TextView text_blur_value,text_sobel_threshold,text_anchor_threshold,text_min_dist,text_poisson_r;
    int KERNEL_LENGTH=5,SOBEL_THRESHOLD=50,ANCHOR_THRESHOLD=20,MIN_DIST,POISSON_R=18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOpenCV();
        initView();


    }

    private void initOpenCV() {
        if(OpenCVLoader.initDebug()){
            Toast.makeText(this,"OpenCV loading success",Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this,"OpenCV loading fail",Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        //init src
        try {
            src=Utils.loadResource(this, R.drawable.lenna);//导入src
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2RGB);//BGR->RGB
        }catch (IOException e){
            e.printStackTrace();
        }
        // 创建 ActivityResultLauncher 实例
        getImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleActivityResult
        );
        //object bound
         img_before=findViewById(R.id.img_before);
         img_after=findViewById(R.id.img_after);

         seekbar_blur =findViewById(R.id.seekbar_blur);
         text_blur_value=findViewById(R.id.text_blur_value);

         seekbar_sobel_threshold=findViewById(R.id.seekbar_sobel_threshold);
         text_sobel_threshold=findViewById(R.id.text_sobel_threshold);

         seekbar_anchor_threshold=findViewById(R.id.seekbar_anchor_threshold);
         text_anchor_threshold=findViewById(R.id.text_anchor_threshold);

         seekbar_min_dist=findViewById(R.id.seekbar_min_dist);
         text_min_dist=findViewById(R.id.text_min_dist);

         seekbar_poisson_r=findViewById(R.id.seekbar_poisson_r);
         text_poisson_r=findViewById(R.id.text_poisson_r);
        //seekbar initial progress
         seekbar_blur.setProgress(KERNEL_LENGTH);
         text_blur_value.setText(String.format(Locale.US,"%4d", KERNEL_LENGTH));
         seekbar_sobel_threshold.setProgress(SOBEL_THRESHOLD);
         text_sobel_threshold.setText(String.format(Locale.US,"%4d", SOBEL_THRESHOLD));
         seekbar_anchor_threshold.setProgress(ANCHOR_THRESHOLD);
         text_anchor_threshold.setText(String.format(Locale.US,"%4d", ANCHOR_THRESHOLD));
         MIN_DIST=(int)(0.02*(src.width()+src.height()));//auto init value
         seekbar_min_dist.setProgress(MIN_DIST);
         text_min_dist.setText(String.format(Locale.US,"%4d", MIN_DIST));
         seekbar_poisson_r.setProgress(POISSON_R);
         text_poisson_r.setText(String.format(Locale.US,"%4d", POISSON_R));
         //seekBarChangeListener
        seekbar_blur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 当进度改变时调用 -只能为正奇数-不能为-1
                int oddProgress = (progress % 2 == 0) ? progress + 1 : progress; // 如果progress是偶数，则减1使其成为奇数
                seekBar.setProgress(oddProgress); // 设置SeekBar的进度为正奇数
                KERNEL_LENGTH = oddProgress; // 更新当前模糊级别
                text_blur_value.setText(String.format(Locale.US,"%4d", KERNEL_LENGTH));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动滑块时调用（可选实现）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动滑块时调用（可选实现）
            }
        });
        seekbar_sobel_threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SOBEL_THRESHOLD = progress; // 更新当前模糊级别
                text_sobel_threshold.setText(String.format(Locale.US,"%4d", SOBEL_THRESHOLD));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动滑块时调用（可选实现）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动滑块时调用（可选实现）
            }
        });
        seekbar_anchor_threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ANCHOR_THRESHOLD = progress; // 锚点阈值
                text_anchor_threshold.setText(String.format(Locale.US,"%4d", ANCHOR_THRESHOLD));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动滑块时调用（可选实现）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动滑块时调用（可选实现）
            }
        });
        seekbar_min_dist.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MIN_DIST = progress;
                text_min_dist.setText(String.format(Locale.US,"%4d", MIN_DIST));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动滑块时调用（可选实现）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动滑块时调用（可选实现）
            }
        });
        seekbar_poisson_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                POISSON_R = progress;
                text_poisson_r.setText(String.format(Locale.US,"%4d", POISSON_R));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动滑块时调用（可选实现）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动滑块时调用（可选实现）
            }
        });
    }

    public void loadPic(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // 设置意图为获取图片类型的数据
        getImageLauncher.launch(intent); // 使用 launch 方法启动 Activity
//        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE); // REQUEST_CODE_GET_IMAGE是你自定义的请求码
    }
    private void handleActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Uri selectedImage = data.getData();
                try {
                    // 使用ContentResolver和BitmapFactory来获取图片的Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Utils.bitmapToMat(bitmap, src);
                    Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB);//创建的是RGBA的map RGBA->RGB 否则按位非会把透明的拉满
                    if ((int) (0.02 * (src.width() + src.height())) <= 100) {//make sure not overflow
                        MIN_DIST = (int) (0.02 * (src.width() + src.height()));//auto init value
                        seekbar_min_dist.setProgress(MIN_DIST);
                        text_min_dist.setText(String.format(Locale.US, "%4d", MIN_DIST));
                    }
                    matOP.showMat(img_before, src);//show Image
                }catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    Mat computeFeatureMap(Mat distanceMap) {
        Mat featureMap = new Mat(distanceMap.size(), distanceMap.type());
        // m=Li/2 Li=0.02*(Lw+Lh)
        double m =  0.01*(src.width()+src.height()); //
        // 获取距离图的宽度和高度
        int width = distanceMap.width();
        int height = distanceMap.height();
        // 遍历距离图的每一个像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取当前点的距离值
                double distanceValue = distanceMap.get(y, x)[0];
                // 根据公式计算特征值
                double featureValue;
                boolean b=(int)(distanceValue/2)%2==0;
                if (b) {
                    featureValue = 255.0 / m * (distanceValue%m);
                }
                else {
                    featureValue = 255.0 / m * (1 - (distanceValue % m));
                }
                // 将计算出的特征值设置到特征图的相应位置->
                featureMap.put(y, x, featureValue);
            }
        }
        return featureMap;
    }
    // button onclick activities
    public void bitNot(View view) {
        Mat dst=new Mat();
        Core.bitwise_not(src,dst); //bit_not OP
        matOP.showMat(img_after,dst);
    }

    public void blur(View view) {
        Mat dst=new Mat();
        Imgproc.GaussianBlur(src,dst, new Size(KERNEL_LENGTH, KERNEL_LENGTH),0,0); //高斯模糊
        matOP.showMat(img_after,dst);//转换成bitmap并显示
    }

    public void gray(View view) {
        Mat dst=new Mat();
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_RGB2GRAY);//BGR->GRAY
        matOP.showMat(img_after,dst);//转换成bitmap并显示
    }

    public void distance(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        Mat edge_map=new Mat(src.size(), CvType.CV_8UC1,new Scalar(255));//8UC1
        matOP.drawCurveEdges(edge_map,ed.m_edges,new Scalar(0));
        Mat distanceMap=new Mat(src.size(), CvType.CV_32FC1,new Scalar(0));//CV_32FC1
        // 计算距离映射  input Mat 8UC1  out Mat 8UC1 黑色为边缘 白色为背景
        Imgproc.distanceTransform(edge_map, distanceMap, Imgproc.DIST_L2, 5);
        // 计算feature map
        Mat featureMap=computeFeatureMap(distanceMap);
        //show feature
        Core.normalize(featureMap, featureMap, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        matOP.showMat(img_after,featureMap);
        featureMap.release();
        //show distance
        Core.normalize(distanceMap, distanceMap, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        matOP.showMat(img_before,distanceMap);
        edge_map.release();
        distanceMap.release();
    }


    public void sobel(View view) {
        final int width=src.width();
        final int height=src.height();
        final double VERTICAL =1,HORIZONTAL=2;
        // operation
        EdgeDrawing ed=new EdgeDrawing(src);
        Mat gray=ed.getGaussianBlurImage(KERNEL_LENGTH);
        gray=ed.getGrayImage(gray);
        ed.getGradientAndDirectionMap(gray,SOBEL_THRESHOLD);
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();
        //show recolor map
        Bitmap mapD=Bitmap.createBitmap (width,height,Bitmap.Config.ARGB_8888);
        Bitmap mapG=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
               if(ed.m_directionImg.get(row,col)[0]==VERTICAL) {
                   mapD.setPixel(col,row,Color.rgb(223, 194, 67));
               }else if(ed.m_directionImg.get(row,col)[0]==HORIZONTAL) {
                   mapD.setPixel(col,row,Color.rgb(218, 165, 32));
               }else{
                   mapD.setPixel(col,row, Color.rgb(0,0,0));
               }
               if(ed.m_gradientImg.get(row,col)[0]!=0) {
                   mapG.setPixel(col, row, Color.rgb(255, 255, 255));
               }else{
                   mapG.setPixel(col, row, Color.rgb(0, 0, 0));
               }
            }
        }
        //test for gray m_gradientImg not black-write
//        matOP.showMat(img_after,ed.m_gradientImg);
        //out distance map and threshold gradient map
        img_before.setImageBitmap(mapD);
        img_after.setImageBitmap(mapG);
    }


    public void ED(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        ArrayList<ArrayList<Point>>edges=ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        ArrayList<Point> constrained_point=polygon.DP_Alg(edges,MIN_DIST);
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();
//        //show anchor
//        Mat anchor_map=new Mat(src.size(), src.type(),new Scalar(0));//U8C3
//        drawPoints(anchor_map,ed.m_anchor, new Scalar(255, 255, 255));
//        matOP.showMat(img_before,anchor_map);
        //show edge
        Mat edge_map=new Mat(src.size(), CvType.CV_8UC3,new Scalar(0));//U8C3
        matOP.drawCurveEdges(edge_map,ed.m_edges,new Scalar(200,200,200));
        matOP.showMat(img_before,edge_map);
        edge_map.release();
        //show edge+constrained points
        Mat edge_point=new Mat(src.size(), CvType.CV_8UC3,new Scalar(0));//U8C3
        matOP.drawCurveEdges(edge_point,ed.m_edges,new Scalar(127,127,127));
        matOP.drawPoints(edge_point,constrained_point,new Scalar(0,255,30));
        matOP.showMat(img_after,edge_point);
        edge_point.release();
    }

    public void Poly(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        Delaunay delaunay=new Delaunay();
        ArrayList<ArrayList<Point>>edges=ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        ArrayList<Point> constrained_point=polygon.DP_Alg(edges,MIN_DIST);
        ArrayList<Point>all_points=new ArrayList<>(constrained_point);
        //add poisson noisy
        ArrayList<Point>inner_points=polygon.Poisson(constrained_point,src.width(),src.height(),POISSON_R);
        all_points.addAll(inner_points);
        ArrayList<Triangle> triangles=delaunay.Watson(src.width(),src.height(),all_points);//->instead test_point
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();
        //show triangles
        Mat triangle_map=src.clone();
        matOP.drawTriangles(triangle_map,triangles,new Scalar(20,20,200));
        matOP.showMat(img_before,triangle_map);
        //show low poly
        Mat lowPoly=src.clone();
        matOP.fillTriangles(lowPoly,triangles,1);
        matOP.showMat(img_after,lowPoly);
    }

    public void voronoi(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        ArrayList<ArrayList<Point>>edges=ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        ArrayList<Point> constrained_point=polygon.DP_Alg(edges,MIN_DIST);
        ArrayList<Point>inner_points=polygon.Poisson(constrained_point,src.width(),src.height(),POISSON_R);
        ArrayList<Point>all_points=new ArrayList<>(constrained_point);
        //Lloyd
        int Lloyd_Iteration=5;
        for(int i=0;i<Lloyd_Iteration;i++) {
            inner_points = polygon.Lloyd(inner_points, constrained_point,src.width(), src.height());
        }
        //add poisson noisy
        all_points.addAll(inner_points);
        //delaunay
        Delaunay delaunay=new Delaunay();
        ArrayList<Triangle> triangles=delaunay.Watson(src.width(),src.height(),all_points);//->instead test_point
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();
        //show voronoi
        Mat voronoiMap=src.clone();
        Subdiv2D subdiv2D=new Subdiv2D(new Rect(0, 0,src.width(), src.height()));
        subdiv2D.insert(new MatOfPoint2f(all_points.toArray(new Point[0])));
        matOP.drawVoronoi(voronoiMap,subdiv2D);
        matOP.showMat(img_before,voronoiMap);
        //show low poly after relaxing
        Mat lowPoly=src.clone();
        matOP.fillTriangles(lowPoly,triangles,1);
        matOP.showMat(img_after,lowPoly);
    }

    public void share(View view) {
        // 创建图片文件
        String fileName = "shared_image.png";
        File outputDir = getExternalFilesDir(null); // 或者其他你选择的目录
        File outputFile = new File(outputDir, fileName);
        // 存入src的图片
        Bitmap bitmap=Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,bitmap);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);//压缩为PNG
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // 创建分享Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");

        // 使用FileProvider获取URI
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    "${applicationId}.file.provider", // 替换为你的FileProvider的authorities
                    outputFile
            );
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            // 添加FLAG_GRANT_READ_URI_PERMISSION标志，允许目标应用读取文件
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 启动分享
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    public void change(View view) {
        Intent intent=new Intent(this,ShareActivity.class);
        startActivity(intent);
    }
}
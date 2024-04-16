package com.example.devtest;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Mat src;
    ImageView img_before,img_after;
    SeekBar seekbar_blur,seekbar_sobel_threshold,seekbar_anchor_threshold,seekbar_min_dist,seekbar_poisson_r;
    TextView text_blur_value,text_sobel_threshold,text_anchor_threshold,text_min_dist,text_poisson_r;
    int KERNEL_LENGTH=5,SOBEL_THRESHOLD=50,ANCHOR_THRESHOLD=20,MIN_DIST,POISSON_R=18;

    private static final int REQUEST_CODE_GET_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOpenCV();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        src.release();
    }

    private void initOpenCV() {
        if(OpenCVLoader.initDebug()){
            Toast.makeText(this,"OpenCV loading success",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"OpenCV loading fail",Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        try {//导入src
            src=Utils.loadResource(this, R.drawable.lenna);
            //BGR->RGB
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2RGB);
        }catch (IOException e){
            e.printStackTrace();
        }
        //object bound
         img_before=findViewById(R.id.img_before);
         img_after=findViewById(R.id.img_after);

         seekbar_blur =findViewById(R.id.sbar_blur);
         text_blur_value=findViewById(R.id.text_blur_value);

         seekbar_sobel_threshold=findViewById(R.id.sbar_sobel_threshold);
         text_sobel_threshold=findViewById(R.id.text_sobel_threshold);

         seekbar_anchor_threshold=findViewById(R.id.sbar_anchor_threshold);
         text_anchor_threshold=findViewById(R.id.text_anchor_threshold);

         seekbar_min_dist=findViewById(R.id.sbar_min_dist);
         text_min_dist=findViewById(R.id.text_min_dist);

         seekbar_poisson_r=findViewById(R.id.sbar_poisson_r);
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
        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE); // REQUEST_CODE_GET_IMAGE是你自定义的请求码
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData(); // 获取用户选择的图片URI
                // 在这里处理selectedImage，比如显示它或者上传到服务器
                try {
                    // 使用ContentResolver和BitmapFactory来获取图片的Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    // 现在你可以使用bitmap来显示图片或进行其他操作
                    Utils.bitmapToMat(bitmap,src);
                    //创建的是RGBA的map RGBA->RGB 否则按位非会把透明的拉满
                    Imgproc.cvtColor(src,src,Imgproc.COLOR_RGBA2RGB);
                    if((int)(0.02*(src.width()+src.height()))<=100) {//make sure not overflow
                        MIN_DIST = (int) (0.02 * (src.width() + src.height()));//auto init value
                        seekbar_min_dist.setProgress(MIN_DIST);
                        text_min_dist.setText(String.format(Locale.US,"%4d", MIN_DIST));
                    }
                    showMat(img_before, src);//自定义函数
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void showMat(ImageView view,Mat mat){
        Bitmap bitmap=Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,bitmap);
        view.setImageBitmap(bitmap);
    }

    void drawPoints(Mat mat,ArrayList<Point> points,Scalar color){
        // make sure mat CV_8UC3
        for (int i=0;i<points.size();i++) {
            Imgproc.circle(mat, points.get(i), 1, color, -1);
        }
    }

    void drawEdges(Mat mat,ArrayList<ArrayList<Point>> edges,Scalar color){
        // make sure mat CV_8UC3
        for(int i=0;i<edges.size();i++){
            for(int j=0;j<edges.get(i).size();j++){
                Imgproc.circle(mat ,edges.get(i).get(j), 1, color, -1);
            }
        }

    }
    Bitmap drawLowPoly(Bitmap bitmap, ArrayList<Triangle> triangles){
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        for(Triangle t:triangles){
            double meanX=(t.p1.x+t.p2.x+t.p3.x)/3;
            double meanY=(t.p1.y+t.p2.y+t.p3.y)/3;
            int color=bitmap.getPixel((int)meanX,(int)meanY);
            paint.setColor(color);
            Path path = new Path();
            path.moveTo((float) t.p1.x, (float) t.p1.y); // 起始点
            path.lineTo((float) t.p2.x, (float) t.p2.y); // 直线到点(x2, y2)
            path.lineTo((float) t.p3.x, (float) t.p3.y); // 再到点(x3, y3)
            path.close(); // 结束路径并闭合形状
            canvas.drawPath(path, paint);
        }
        return bitmap;
    }

    public void bitNot(View view) {
        Mat dst=new Mat();
        Core.bitwise_not(src,dst); //bit_not OP
        showMat(img_after,dst);
        dst.release();
    }

    public void blur(View view) {
        Mat dst=new Mat();
        Imgproc.GaussianBlur(src,dst, new Size(KERNEL_LENGTH, KERNEL_LENGTH),0,0); //高斯模糊
        showMat(img_after,dst);//转换成bitmap并显示
        dst.release(); //mat回收
    }

    public void gray(View view) {
        Mat dst=new Mat();
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_RGB2GRAY);//BGR->GRAY
        showMat(img_after,dst);//转换成bitmap并显示
        dst.release();          //mat回收
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
//        showMat(img_after,ed.m_gradientImg);
        //out distance map and threshold gradient map
        img_before.setImageBitmap(mapD);
        img_after.setImageBitmap(mapG);
    }


    public void ED(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        Polygon polygon=new Polygon();
        ArrayList<ArrayList<Point>>edges=ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        ArrayList<Point> constrained_point=polygon.DP_Alg(edges,MIN_DIST);
        constrained_point=polygon.Poisson(constrained_point,src.width(),src.height(),POISSON_R);
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();

//        //show anchor
//        Mat anchor_map=new Mat(src.size(), src.type(),new Scalar(0));//U8C3
//        drawPoints(anchor_map,ed.m_anchor, new Scalar(255, 255, 255));
//        showMat(img_before,anchor_map);
//        anchor_map.release();
        //show edge
        Mat edge_map=new Mat(src.size(), CvType.CV_8UC3,new Scalar(0));//U8C3
        drawEdges(edge_map,ed.m_edges,new Scalar(200,200,200));
        showMat(img_before,edge_map);
        edge_map.release();
        //show edge+constrained points
        Mat edge_point=new Mat(src.size(), CvType.CV_8UC3,new Scalar(0));//U8C3
        drawEdges(edge_point,ed.m_edges,new Scalar(127,127,127));
        drawPoints(edge_point,constrained_point,new Scalar(0,255,30));
        showMat(img_after,edge_point);
        edge_point.release();
    }

    public void Poly(View view) {
        EdgeDrawing ed=new EdgeDrawing(src);
        Polygon polygon=new Polygon();
        Delaunay delaunay=new Delaunay();
        ArrayList<ArrayList<Point>>edges=ed.getPictureEdges(KERNEL_LENGTH,SOBEL_THRESHOLD,ANCHOR_THRESHOLD);
        ArrayList<Point> constrained_point=polygon.DP_Alg(edges,MIN_DIST);
        constrained_point=polygon.Poisson(constrained_point,src.width(),src.height(),POISSON_R);
        // 去重
        Set<Point> set = new HashSet<>(constrained_point);
        constrained_point.clear(); // 清空原列表
        constrained_point.addAll(set); // 将去重后的元素添加回列表

//      //test delaunay data
//        ArrayList<Point> test_point=new ArrayList<>();
//        test_point.add(new Point(src.width()/2,src.height()/2));
//        test_point.add(new  Point(src.width()/4,src.height()/4));
        ArrayList<Triangle> triangles=delaunay.Watson(src.width(),src.height(),constrained_point);//->instead test_point
        Toast.makeText(this,"Convert finish",Toast.LENGTH_SHORT).show();
        //show triangles
        Bitmap triangle_bitmap=Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,triangle_bitmap);
        Canvas canvas = new Canvas(triangle_bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE); // 设置线条颜色
        paint.setStrokeWidth(1); // 设置线条粗细
        paint.setStyle(Paint.Style.STROKE); // 设置线条样式为实线
        for (Triangle t:triangles){
            canvas.drawLine((float) t.e12.start.x,(float) t.e12.start.y,(float) t.e12.end.x,(float) t.e12.end.y,paint);
            canvas.drawLine((float) t.e23.start.x,(float) t.e23.start.y,(float) t.e23.end.x,(float) t.e23.end.y,paint);
            canvas.drawLine((float) t.e31.start.x,(float) t.e31.start.y,(float) t.e31.end.x,(float) t.e31.end.y,paint);
        }
        img_before.setImageBitmap(triangle_bitmap);
        //show low poly
        Bitmap lowPoly=Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,lowPoly);
        lowPoly= drawLowPoly(lowPoly,triangles);
        img_after.setImageBitmap(lowPoly);

    }
}
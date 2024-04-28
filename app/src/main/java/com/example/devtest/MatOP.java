package com.example.devtest;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MatOP {
    void showMat(ImageView view, Mat mat){
        Bitmap bitmap=Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,bitmap);
        view.setImageBitmap(bitmap);
    }
    void drawPoints(Mat mat, ArrayList<Point> points, Scalar color){
        // make sure mat CV_8UC3
        for (int i=0;i<points.size();i++) {
            Imgproc.circle(mat, points.get(i), 1, color, -1);
        }
    }
    void drawCurveEdges(Mat mat,ArrayList<ArrayList<Point>> edges,Scalar color){
        // make sure mat CV_8UC3
        for(int i=0;i<edges.size();i++){
            for(int j=0;j<edges.get(i).size();j++){
                Imgproc.circle(mat ,edges.get(i).get(j), 1, color, -1);
            }
        }
    }
    void  drawTriangles(Mat mat,ArrayList<Triangle>triangles,Scalar color){
        for (Triangle t:triangles){
            Imgproc.line(mat,t.e12.start,t.e12.end,color);
            Imgproc.line(mat,t.e23.start,t.e23.end,color);
            Imgproc.line(mat,t.e31.start,t.e31.end,color);
        }
    }
    void  fillTriangles(Mat mat,ArrayList<Triangle>triangles,int colorMode){
        //mat ->8UC3
        if(mat.type()!= CvType.CV_8UC3){
            Log.e("typeError","Error mat type");
            return;
        }
        if(colorMode==1){//mode 1 -> center color
            for (Triangle t:triangles) {
                Point point = t.getCentroid();
                MatOfPoint polygon=new MatOfPoint(t.p1,t.p2,t.p3);
                double[] color = mat.get((int) point.y, (int) point.x);
                Imgproc.fillConvexPoly(mat,polygon,new Scalar(color[0],color[1],color[2]));
            }
        }
        else {
            //TODO:add other color mode
            Log.e("colorError","Error color mode");
        }
    }


    void drawVoronoi(Mat mat, Subdiv2D subdiv2D){
        List<MatOfPoint2f> polygons=new LinkedList<>();
        MatOfPoint2f centers=new MatOfPoint2f();
        subdiv2D.getVoronoiFacetList(new MatOfInt(), polygons, centers);//一一对应
        //polygons ->List<MatOfPoint>
        List<MatOfPoint> facetInt=new LinkedList<>();
        //draw polygons
        for (int i=0;i<polygons.size();i++){
            MatOfPoint2f cell=polygons.get(i);
            MatOfPoint cellInt=new MatOfPoint(cell.toList().toArray(new Point[0]));
            Point center=centers.toArray()[i];
            double[] color=mat.get((int)center.y,(int)center.x);
            Imgproc.fillConvexPoly(mat,cellInt,new Scalar(color[0],color[1],color[2]));
            facetInt.add(cellInt);
            Imgproc.circle(mat,center,1,new Scalar(255,255,255),-1);
        }
        Imgproc.polylines(mat,facetInt,true,new Scalar(255,255,255));
    }
}


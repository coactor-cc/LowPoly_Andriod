package com.example.devtest;


import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Subdiv2D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Polygon {
    ArrayList<Point> DP_Alg(ArrayList<ArrayList<Point>> edges, int minDist){
           //input edges -> out put constrained edge point
            ArrayList<Point> constrained_Points=new ArrayList<>();
            for(int i=0; i<edges.size(); i++)
            {
               if(edges.get(i).size()>=minDist)
                {   //long edge ->get k
                    int k;
                    for(k=0; k<edges.get(i).size(); k+=minDist)
                    {
                        constrained_Points.add(edges.get(i).get(k));
                    }
                }
            }
            //still too much-> max 25000
            while (constrained_Points.size()>=25000){
                Random rand = new Random();
                int randomInt = rand.nextInt(constrained_Points.size());
                constrained_Points.remove(randomInt);
            }
            return constrained_Points;
       }

    // ** return noisy but add noisy to original constrain_points **
    ArrayList<Point> Poisson(ArrayList<Point> constrain_points,int width,int height, int radius){
        ArrayList<Point> noisy=new ArrayList<>();
        for( int x=radius; x<width; x+=2*radius) {
            for (int y = radius; y < height; y += 2 * radius) {
                Rect rect=new Rect(new Point(x-radius,y-radius),new Point(x+radius,y+radius));
                boolean flag=false;
                for(Point point:constrain_points){

                    if(point.inside(rect)){
                        flag=true;
                        break;
                    }
                }
                if(!flag){
                    // 随机插入一个点 在rect中 且在图中
                    Random rand = new Random();
                    int randomInt = rand.nextInt(2*radius);
                    Point p=new Point(x-radius+randomInt,y-radius+randomInt);
                    if(p.x<width&&p.x>0&&p.y<height&&p.y>0) {
                        noisy.add(p);
                    }
                }
            }
        }
        return  noisy;
    }

     //input: inner_points+constrained_points to get voronoi cell featureMap   output:relaxing inner points
    ArrayList<Point> Lloyd(ArrayList<Point> inner_points,ArrayList<Point>constrained_points, int width,int height){
        ArrayList<Point> result=new ArrayList<>();
        Subdiv2D subdiv2D=new Subdiv2D(new Rect(0, 0,width, height));
        subdiv2D.insert(new MatOfPoint2f(inner_points.toArray(new Point[0])));
        subdiv2D.insert(new MatOfPoint2f(constrained_points.toArray(new Point[0])));
        List<MatOfPoint2f> polygons=new LinkedList<>();
        MatOfPoint2f centers=new MatOfPoint2f();
        subdiv2D.getVoronoiFacetList(new MatOfInt(), polygons, centers);//一一对应
        for (int i=0;i<inner_points.size();i++){
            List<Point> cell=polygons.get(i).toList();
            double sumX=0;
            double sumY=0;
            for(Point p:cell){
                sumY+=p.y;
                sumX+=p.x;
            }
            double meanX=sumX/cell.size();
            double meanY=sumY/cell.size();
            if(meanX>0&&meanX< width&&meanY>0&&meanY< height){
                Point newCenter= new Point(meanX,meanY);
                result.add(newCenter);
            }
        }
        return result;
    }
}


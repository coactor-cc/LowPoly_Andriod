package com.example.devtest;


import org.opencv.core.Point;
import java.util.ArrayList;

class Edge{
    Point start,end;
    Edge(Point p1,Point p2){
        start=p1;
        end=p2;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Edge e = (Edge) obj;
        return   start.equals(e.start)&&end.equals(e.end)||start.equals(e.end)&&end.equals(e.start);
    }

}

class Triangle{
    Point p1,p2,p3;
    Edge e12,e23,e31;

    Triangle(Point _p1,Point _p2,Point _p3){
       p1=_p1;
       p2=_p2;
       p3=_p3;
       e12=new Edge(p1,p2);
       e23=new Edge(p2,p3);
       e31=new Edge(p3,p1);
    }
    Point getCentroid(){
        double centroidX = (p1.x + p2.x + p3.x) / 3.0;
        double centroidY = (p1.y + p2.y + p3.y) / 3.0;
        return new Point(centroidX, centroidY);
    }

    boolean circleContain(Point p){
            double ab = p1.norm2();
            double cd = p2.norm2();
            double ef = p3.norm2();
            double center_x = (ab * (p3.y - p2.y) + cd * (p1.y - p3.y) + ef * (p2.y - p1.y)) / (p1.x * (p3.y - p2.y) + p2.x * (p1.y - p3.y) + p3.x * (p2.y - p1.y));
            double center_y = (ab * (p3.x - p2.x) + cd * (p1.x - p3.x) + ef * (p2.x - p1.x)) / (p1.y * (p3.x - p2.x) + p2.y * (p1.x - p3.x) + p3.y * (p2.x - p1.x));
            Point center=new Point(center_x/2,center_y/2);
            double radius = p1.dist2(center);//R power 2
            double dist = p.dist2(center);// dis
        return dist <= radius;
    }
    boolean contain(Point p){
        return p.equals(p1)||p.equals(p2)||p.equals(p3);
    }
    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Triangle tri = (Triangle) obj;
        return	contain(tri.p1)&&contain(tri.p2)&&contain(tri.p3);
    }
}

public class Delaunay {
    ArrayList<Triangle> Watson(int width,int height,ArrayList<Point> vertices) {

        Point p1=new Point(0,0);
        Point p2=new Point(width-1,0);
        Point p3=new Point(0,height-1);
        Point p4=new Point(width-1,height-1);
        // save initial super triangle
        ArrayList<Triangle>m_triangles=new ArrayList<>();
        m_triangles.add(new Triangle(p1, p2, p3));
        m_triangles.add(new Triangle(p2,p3,p4));

        for (Point p:vertices) {
            //find bad triangle
            ArrayList<Triangle> bad_triangles=new ArrayList<>();
            ArrayList<Edge> cavity_edges=new ArrayList<>();
            ArrayList<Edge> bad_edges=new ArrayList<>();
            // 遍历所有外接圆包含p点的三角形
            for (Triangle t : m_triangles) {
                if (t.circleContain(p))
                {
                    bad_triangles.add(t);//del triangle
                    if(cavity_edges.contains(t.e12)){
                        bad_edges.add(t.e12);
                    }else {
                        cavity_edges.add(t.e12);
                    }
                    if(cavity_edges.contains(t.e23)){
                        bad_edges.add(t.e23);
                    }else {
                        cavity_edges.add(t.e23);
                    }
                    if(cavity_edges.contains(t.e31)){
                        bad_edges.add(t.e31);
                    }else {
                        cavity_edges.add(t.e31);
                    }
                }
            }
            //delete bad triangles
            m_triangles.removeAll(bad_triangles);
            //del bad edges
            cavity_edges.removeAll(bad_edges);
            // build new triangles
            for (Edge e:cavity_edges){
                m_triangles.add(new Triangle(e.start,e.end,p));
            }
        }
        return m_triangles;
    }

}


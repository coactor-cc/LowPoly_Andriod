package com.example.devtest;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


public class EdgeDrawing {
   final int width,height;
   final double VERTICAL =1,HORIZONTAL=2;
   Mat m_originalImg,m_gradientImg,m_directionImg;
   ArrayList<Point> m_anchor;
   ArrayList< ArrayList<Point>> m_edges;


   EdgeDrawing(Mat src){//constructor
      m_originalImg=src;
      width = m_originalImg.width();
      height= m_originalImg.height();
      m_gradientImg=new Mat(height,width, CvType.CV_32FC1,new Scalar(0));
      m_directionImg=new Mat(height,width,CvType.CV_32FC1,new Scalar(0));
      m_anchor=new ArrayList<>();
      m_edges=new ArrayList<>();
   }

   Mat getGaussianBlurImage(int kernel_size){
      Mat dst=new Mat();
      Imgproc.GaussianBlur(m_originalImg,dst,new Size(kernel_size,kernel_size), 0, 0);
      return  dst;
   }
   Mat getGrayImage(Mat src){
      Mat dst=new Mat();
      Imgproc.cvtColor(src,dst,Imgproc.COLOR_RGB2GRAY);
      return dst;
   }
   void  getGradientAndDirectionMap(Mat src,int  threshold){
      Mat gx=new Mat(height,width,CvType.CV_32FC1);
      Mat gy=new Mat(height,width,CvType.CV_32FC1);
      Imgproc.Sobel(src,gx,CvType.CV_32FC1,1,0,3);//32FC1
      Imgproc.Sobel(src,gy,CvType.CV_32FC1,0,1,3);//32FC1

      double gx_data;
      double gy_data;
      double g_data;

      for(int row = 0; row<height; row++) {
         for (int col = 0; col < width; col++) {
            gx_data=Math.abs(gx.get(row,col)[0]);// abs( gradient x direction)
            gy_data=Math.abs(gy.get(row,col)[0]);// abs( gradient y direction)

            if(gx_data>gy_data)
               m_directionImg.put(row,col,VERTICAL);
            if(gx_data<gy_data)
               m_directionImg.put(row,col,HORIZONTAL);
            g_data=gx_data+gy_data;
            if (g_data>threshold){
               m_gradientImg.put(row,col,g_data);
            }
         }
      }
   }
   void getAnchors(int threshold){
      for(int row = 1; row<height-1; row++) {//to avoid out of range
         for (int col = 1; col < width-1; col++) {
            if(m_directionImg.get(row,col)[0]==HORIZONTAL){
               if(m_gradientImg.get(row,col)[0]-m_gradientImg.get(row,col-1)[0]>=threshold
                       &&m_gradientImg.get(row,col)[0]-m_gradientImg.get(row,col+1)[0]>=threshold){
                  m_anchor.add(new Point(col,row));
               }
            }else if(m_directionImg.get(row,col)[0]==VERTICAL){
               if(m_gradientImg.get(row,col)[0]-m_gradientImg.get(row-1,col)[0]>=threshold
                       &&m_gradientImg.get(row,col)[0]-m_gradientImg.get(row+1,col)[0]>=threshold){
                  m_anchor.add(new Point(col,row));
               }
            }
         }
      }
   }
   void getEdges(){
      Mat isEdge=new Mat(m_originalImg.size(),CvType.CV_8UC1,new Scalar(0));//initial 0=false
      for(int i=0;i<m_anchor.size();i++){
         ArrayList<Point> edge=new ArrayList<>();
         int x = (int)m_anchor.get(i).x;
         int y = (int)m_anchor.get(i).y;
         searchFromAnchor(x,y,isEdge,edge);
         if(edge.size()!=0)
            m_edges.add(edge);
      }
   }

   void searchFromAnchor(int x, int y,Mat isEdge,ArrayList<Point> edge){
      //attention this use x,y axis direction
      //range test
      if(x-1<0 || y-1<0 || x+1>=width || y+1>=height)
            return;
      //GMap>0 & not have been added edge->avoid repeat
      if(m_gradientImg.get(y,x)[0]>0 && isEdge.get(y,x)[0]==0)
      {
         edge.add(new Point(x,y));
         isEdge.put(y,x,1);//isEdge modify as 1=true

         if(m_directionImg.get(y,x)[0]==HORIZONTAL) // horizontal edge
         {
            // Go Left
            if(isEdge.get(y-1,x-1)[0]==0 && isEdge.get(y,x-1)[0]==0 &&isEdge.get(y+1,x-1)[0]==0)
            {
               if(m_gradientImg.get(y-1,x-1)[0] > m_gradientImg.get(y,x-1)[0]
                       && m_gradientImg.get(y-1,x-1)[0] > m_gradientImg.get(y+1,x-1)[0])
                  searchFromAnchor(x-1,y-1,isEdge,edge);
               else if(m_gradientImg.get(y+1,x-1)[0] > m_gradientImg.get(y,x-1)[0]
                       && m_gradientImg.get(y+1,x-1)[0] > m_gradientImg.get(y-1,x-1)[0])
                  searchFromAnchor(x-1,y+1,isEdge,edge);
               else
                  searchFromAnchor(x-1,y,isEdge,edge);
            }
            // Go right
            if(isEdge.get(y-1,x+1)[0]==0 && isEdge.get(y,x+1)[0]==0 && isEdge.get(y+1,x+1)[0]==0)
            {
               if(m_gradientImg.get(y-1,x+1)[0] > m_gradientImg.get(y,x+1)[0]
                       && m_gradientImg.get(y-1,x+1)[0] > m_gradientImg.get(y+1,x+1)[0])
                  searchFromAnchor(x+1,y-1,isEdge,edge);
               else if(m_gradientImg.get(y+1,x+1)[0] > m_gradientImg.get(y,x+1)[0]
                       && m_gradientImg.get(y+1,x+1)[0] > m_gradientImg.get(y-1,x+1)[0])
                  searchFromAnchor(x+1,y+1,isEdge,edge);
               else
                  searchFromAnchor(x+1,y,isEdge,edge);
            }
         }
         else if(m_directionImg.get(y,x)[0]==VERTICAL) // vertical edge
         {
            // Go Top
            if(isEdge.get(y-1,x+1)[0]==0 && isEdge.get(y-1,x)[0]==0 && isEdge.get(y-1,x-1)[0]==0)
            {
               if(m_gradientImg.get(y-1,x-1)[0] > m_gradientImg.get(y-1,x)[0]
                       && m_gradientImg.get(y-1,x-1)[0] > m_gradientImg.get(y-1,x+1)[0])
                  searchFromAnchor(x-1,y-1,isEdge,edge);
               else if(m_gradientImg.get(y-1,x+1)[0] > m_gradientImg.get(y-1,x-1)[0]
                       && m_gradientImg.get(y-1,x+1)[0] > m_gradientImg.get(y-1,x)[0])
                  searchFromAnchor(x+1,y-1,isEdge,edge);
               else
                  searchFromAnchor(x,y-1,isEdge,edge);
            }
            // Go Down
            if(isEdge.get(y+1,x+1)[0]==0 && isEdge.get(y+1,x)[0]==0 && isEdge.get(y+1,x-1)[0]==0)
            {
               if(m_gradientImg.get(y+1,x-1)[0] > m_gradientImg.get(y+1,x)[0]
                       && m_gradientImg.get(y+1,x-1)[0] > m_gradientImg.get(y+1,x+1)[0])
                  searchFromAnchor(x-1,y+1,isEdge,edge);
               else if(m_gradientImg.get(y+1,x+1)[0] > m_gradientImg.get(y+1,x-1)[0]
                       && m_gradientImg.get(y+1,x+1)[0] > m_gradientImg.get(y+1,x)[0])
                  searchFromAnchor(x+1,y+1,isEdge,edge);
               else
                  searchFromAnchor(x,y+1,isEdge,edge);
            }
         }
      }
   }

   ArrayList< ArrayList<Point>> getPictureEdges(int kernel_size,int sobel_threshold,int anchor_threshold){
      Mat gary=getGrayImage(getGaussianBlurImage(kernel_size));
      getGradientAndDirectionMap(gary,sobel_threshold);
      getAnchors(anchor_threshold);
      getEdges();
      return m_edges;
   }

}

package com.example.devtest;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


public class EdgeDrawing {
   final byte VERTICAL =127;
   final byte HORIZONTAL=-1;
   Mat  m_originalImg;
   public Mat m_gradientImg,m_directionImg;
   ArrayList<Point> m_anchor;


   EdgeDrawing(Mat src){//constructor
      m_originalImg=src;
      m_gradientImg=new Mat(m_originalImg.height(),m_originalImg.width(), CvType.CV_8UC1);
      m_directionImg=new Mat(m_originalImg.height(),m_originalImg.width(),CvType.CV_8UC1);
      m_anchor=new ArrayList<>();
      m_gradientImg.setTo(new Scalar(0));
      m_directionImg.setTo(new Scalar(0));

   }

   Mat getGaussianBlurImage(Mat src,int kernel_size){
      Mat dst=new Mat();
      Imgproc.GaussianBlur(src,dst,new Size(kernel_size,kernel_size), 0, 0);
      return  dst;
   }
   Mat getGrayImage(Mat src){
      Mat dst=new Mat();
      Imgproc.cvtColor(src,dst,Imgproc.COLOR_RGB2GRAY);
      return dst;
   }
   void  getGradientAndDirectionMap(Mat src,int  threshold){
      final int width=src.width();
      final int height=src.height();

      Mat gx=new Mat(height,width,CvType.CV_8UC1);
      Mat gy=new Mat(height,width,CvType.CV_8UC1);
      Imgproc.Sobel(src,gx,CvType.CV_8UC1,1,0,3);//8UC1
      Imgproc.Sobel(src,gy,CvType.CV_8UC1,0,1,3);//8UC1

      final int ch=gx.channels();
      byte[] gx_data=new byte[ch];
      byte[] gy_data=new byte[ch];
      byte g_data;

      for(int row=0; row<height; row++) {
         for (int col = 0; col < width; col++) {
            gx.get(row,col,gx_data);
            gy.get(row,col,gy_data);

            if(gx_data[0]>gy_data[0])
               m_directionImg.put(row,col,VERTICAL);
            if(gx_data[0]<gy_data[0])
               m_directionImg.put(row,col,HORIZONTAL);
            g_data=(byte)(gx_data[0]&0xff+gy_data[0]&0xff);
            if (g_data>threshold){
               m_gradientImg.put(row,col,g_data);
            }
         }
      }

      gx.release();
      gy.release();
   }
   void getAnchors(int threshold){
      final int width=m_originalImg.width();
      final int height= m_originalImg.height();

      for(int row=1; row<height-1; row++) {
         for (int col = 1; col < width-1; col++) {
            //内存泄漏在边缘点,我直接修改回来

            if((byte)m_directionImg.get(row,col)[0]==HORIZONTAL){
               if(m_gradientImg.get(row,col)[0]-m_gradientImg.get(row,col-1)[0]>=threshold
                       &&m_gradientImg.get(row,col)[0]-m_gradientImg.get(row,col+1)[0]>=threshold){
                  m_anchor.add(new Point(col,row));
               }
            }else if((byte)m_directionImg.get(row,col)[0]==VERTICAL){
               if(m_gradientImg.get(row,col)[0]-m_gradientImg.get(row-1,col)[0]>=threshold
                       &&m_gradientImg.get(row,col)[0]-m_gradientImg.get(row+1,col)[0]>=threshold){
                  m_anchor.add(new Point(col,row));
               }
            }
         }
      }
   }


}

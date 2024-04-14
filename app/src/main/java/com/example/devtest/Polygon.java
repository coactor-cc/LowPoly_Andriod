package com.example.devtest;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Random;

public class Polygon {

ArrayList<Point> approximation(ArrayList<ArrayList<Point>> edges,int minDist){
       //input edges -> out put constrained edge point
        ArrayList<Point> constrained_Points=new ArrayList<>();
        for(int i=0; i<edges.size(); i++)
        {
//             /*if(edges.get(i).size() <minDist && edges.get(i).size()>2 )
//            {   //test :normal short edge ->get two terminal
//                constrained_Points.add(edges.get(i).get(0));
//                constrained_Points.add(edges.get(i).get(edges.get(i).size()-1));
//            }*/
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
}

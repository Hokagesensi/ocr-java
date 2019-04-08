package ocr_test01;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class ImgPreProcess {
	
	 /**
     * Plot histogram of a single channel grayscale image
     *
     * @param img a single channel grayscale image
     */
    public static void plotGrayHistogram(Mat img) {
        java.util.List<Mat> images = new ArrayList<>();
        images.add(img); 
        MatOfInt channels = new MatOfInt(0); // 图像通道数，0表示只有一个通道 
        MatOfInt histSize = new MatOfInt(256); // CV_8U类型的图片范围是0~255，共有256个灰度级
        Mat histogramOfGray = new Mat(); // 输出直方图结果，共有256行，行数的相当于对应灰度值，每一行的值相当于该灰度值所占比例
        MatOfFloat histRange = new MatOfFloat(0, 255);
        Imgproc.calcHist(images, channels, new Mat(), histogramOfGray, histSize, histRange, false);  // 计算直方图 
        // 按行归一化
        Core.normalize(histogramOfGray, histogramOfGray, 0, histogramOfGray.rows(), Core.NORM_MINMAX, -1, new Mat());

        // 创建画布
        int histImgRows = 300;
        int histImgCols = 300;
        int colStep = (int) Math.floor(histImgCols / histSize.get(0, 0)[0]);
        Mat histImg = new Mat(histImgRows, histImgCols, CvType.CV_8UC3, new Scalar(255,255,255));  // 重新建一张图片，绘制直方图
        for (int i = 0; i < histSize.get(0, 0)[0]; i++) {  // 画出每一个灰度级分量的比例，注意OpenCV将Mat最左上角的点作为坐标原点
            Imgproc.line(histImg,
                    new org.opencv.core.Point(colStep * i, histImgRows - 20),
                    new org.opencv.core.Point(colStep * i, histImgRows - Math.round(histogramOfGray.get(i, 0)[0]) - 20),
                    new Scalar(0, 0,0), 2,8,0);  
            if (i%50 == 0) {
                 Imgproc.putText(histImg, Integer.toString(i), new org.opencv.core.Point(colStep * i, histImgRows - 5), 1, 1, new Scalar(0, 0, 0));  // 附上x轴刻度
            }
        }
//        HighGui.imshow("灰度直方分布图", histImg);
//        HighGui.waitKey(0);

    }
	
	
	
	/*
	 * 绘制图片轮廓
	 */
	public static Point[] findContours(Mat src, Mat dst) {
		Mat gray = new Mat();
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> polyContours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(src, contours, hierarchy, 
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		dst.create(src.size(), src.type());
		int maxArea = 0;
		for(int i=0;i<contours.size();i++) {
			if(Imgproc.contourArea(contours.get(i))
					>Imgproc.contourArea(contours.get(maxArea))) {
				maxArea = i;
			}
//			System.out.println("发现轮廓："+i);
//			Imgproc.drawContours(src, contours, i, 
//					new Scalar(Math.random()*255,
//							Math.random()*255,
//							Math.random()*255));
		}
		System.out.println("最大轮廓为："+maxArea+"="+
		Imgproc.contourArea(contours.get(maxArea)));
		double Area = src.height()*src.width();
		double rate = Imgproc.contourArea(contours.get(maxArea))/Area;
		System.out.println("图片的总面积大小为:"+Area);
		System.out.println("最大轮廓占比："+rate);
		MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(maxArea).toArray());
        Imgproc.approxPolyDP(matOfPoint2f, approxCurve, 0.1, true);
        MatOfPoint Curve = new MatOfPoint(approxCurve.toArray());
        polyContours.add(Curve);
        Imgproc.drawContours(dst, polyContours,0,new Scalar(128,128,128));
		Point[] points = approxCurve.toArray();
		double[] line = new double[4];
		Point[] vertex = new Point[4];
		vertex = find4Points(points,line);
		//画出四条边界线
		Imgproc.line(dst, new Point(line[0],0), 
					new Point(line[0],dst.height()), 
					new Scalar(255/4,0,0),1,Imgproc.LINE_AA);
		
		Imgproc.line(dst, new Point(line[1],0), 
				new Point(line[1],dst.height()), 
				new Scalar(255/4*2,0,0),1,Imgproc.LINE_AA);
		
		Imgproc.line(dst, new Point(0,line[2]), 
				new Point(dst.height(),line[2]), 
				new Scalar(255/4*3,0,0),1,Imgproc.LINE_AA);
		
		Imgproc.line(dst, new Point(0,line[3]), 
				new Point(dst.height(),line[3]), 
				new Scalar(255,0,0),1,Imgproc.LINE_AA);
		//画出四个顶点
		for(int i=0;i<vertex.length;i++)
		{
			Imgproc.circle(dst, vertex[i], 1, new Scalar(255,0,0));
			Imgproc.putText(dst,String.valueOf(i+1), vertex[i], 
					Core.FLAGS_MAPPING, 1.0, new Scalar(255,0,0));
			
		}		
//		HighGui.imshow("轮廓选择", dst);
//		HighGui.waitKey(0);
		//释放内存
        gray.release();
		hierarchy.release();
		return vertex;
	}
	/*
	 * 给出选定轮廓的四个顶点
	 */
	public static Point[] find4Points(Point[] points,double[] line) {
		
		double[] grad = new double[points.length];
		double center_x,center_y;
		int len = points.length;
		//记录四大部分点的索引
		int[] leftUp = new int[len];
		int[] leftDn = new int[len];
		int[] rightUp = new int[len];
		int[] rightDn = new int[len];
		int leftUpCount,leftDnCount,rightUpCount,rightDnCount;
		leftUpCount = leftDnCount = rightUpCount = rightDnCount = 0;
		line[0]=line[2]=0;
		line[1]=line[3]=Double.MAX_VALUE;
		
		//划定切割边界
		for(int i=0; i<len;i++) {
			//右边界
			if(points[i].x>line[0])
				line[0]=points[i].x;
			//左边界
			if(points[i].x<line[1])
				line[1]=points[i].x;
			//下边界
			if(points[i].y>line[2])
				line[2]=points[i].y;
			//上边界
			if(points[i].y<line[3])
				line[3]=points[i].y;
		}
		//图像轮廓x,y方向上的两个中心
		center_x = (line[1]+line[0])/2;
		center_y = (line[2]+line[3])/2;
		
		System.out.println("x中心："+center_x+",y中心："+center_y);
		//粗略划分轮廓上的点为四大部分
		for(int i=0;i<len;i++) {
			if(points[i].x>=center_x)
			{
				if(points[i].y<=center_y)
					leftUp[leftUpCount++]=i;
				else
					leftDn[leftDnCount++]=i;	
			}else {
				if(points[i].y<=center_y)
					rightUp[rightUpCount++]=i;
				else
					rightDn[rightDnCount++]=i;
			}
			System.out.println(points[i].x+","+points[i].y);
		}
		//最大对角线确定四大顶点
		//主对角线计算
		int LU,RD;
		LU=leftUp[0];
		RD=rightDn[0];
		for(int i=leftUpCount;i>=0;i--) {
			for(int j=rightDnCount;j>=0;j--)
			{
				if(Math.pow(points[leftUp[i]].x-points[rightDn[j]].x, 2)+
					Math.pow(points[leftUp[i]].y-points[rightDn[j]].y, 2)>=
					Math.pow(points[leftUp[LU]].x-points[rightDn[RD]].x, 2)+
					Math.pow(points[leftUp[LU]].y-points[rightDn[RD]].y, 2)) {
					LU=i;
					RD=j;
				}
			}
		}
		System.out.println(LU+"/"+leftUpCount+","+RD+"/"+rightDnCount);
		//副对角线计算
		int RU,LD;
		RU=rightUp[0];
		LD=leftDn[0];
		for(int i=0;i<rightUpCount;i++) {
			for(int j=0;j<leftDnCount;j++) {
				if(Math.pow(points[leftDn[i]].x-points[rightUp[j]].x, 2)+
				   Math.pow(points[leftDn[i]].y-points[rightUp[j]].y, 2)>
				   Math.pow(points[leftDn[LD]].x-points[rightUp[RU]].x, 2)+
				   Math.pow(points[leftDn[LD]].y-points[rightUp[RU]].y, 2)) {
					RU=i;
					LD=j;
				}		
			}
		}
		System.out.println(RU+"/"+rightUpCount+","+LD+"/"+leftDnCount);
		Point[] vertex = new Point[4];
		vertex[0] = points[rightUp[RU]];
		vertex[1] = points[leftUp[LU]];
		vertex[2] = points[rightDn[RD]];
		vertex[3] = points[leftDn[LD]];	
		return vertex;
	}
	
	/*
	 *透视变换图片 
	 */
	public static void persTransform(Mat src,Mat dst,Point[] vertex) {
		List<Point> listSrcs = java.util.Arrays.asList(
				vertex[0],vertex[1],vertex[2],vertex[3]);
		Mat srcPoints=Converters.vector_Point_to_Mat(listSrcs,CvType.CV_32F);
		
		vertex[1].y = vertex[0].y;
		vertex[2].x = vertex[0].x;
		vertex[3].x = vertex[1].x;
		vertex[3].y = vertex[2].y;
		List<Point> listDsts = java.util.Arrays.asList(
				vertex[0],vertex[1],vertex[2],vertex[3]);
		Mat dstPoints=Converters.vector_Point_to_Mat(listDsts,CvType.CV_32F);
		Mat perpectiveMat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
		Imgproc.warpPerspective(src, dst, perpectiveMat, src.size(),Imgproc.INTER_LINEAR);
	}
	
	/*
    调整图片的大小
    宽度范围设定在1000左右
     */
    public static Mat AdjustImgSize(Mat src){
        double nHeight = src.rows(),nWidth=src.cols();
        Mat dst=new Mat();
        double fx,fy;
        fy = 500/nHeight;
        fx = 500/nHeight;
        Size dsize = new Size(Math.round(fx*nWidth),Math.round(fy*nHeight));
        Imgproc.resize(src,dst,dsize,fx,fy,Imgproc.INTER_NEAREST);
        return dst;
    }
}

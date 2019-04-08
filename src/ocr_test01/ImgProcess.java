package ocr_test01;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;



public class ImgProcess {
	
	/*
	 * 二值图像取反
	 * 输入：Mat图像 
	 * 返回：无
	 * @author: madao
	 */
	public static void Imgreverse(Mat img)
	{
		for(int i=0;i<img.height();i++)
			for(int j=0;j<img.width();j++)
			{
				if(img.get(i, j)[0]==0.0)
					img.put(i, j, 255.0);
				else
					img.put(i, j, 0);
					
			}
	}
	
	/*
	 * 图片旋转90度
	 * @param src:输入图片MAT
	 * @param dst:输出图片Mat
	 */
	public static Mat RotateImg(Mat src) {
		int nHeight = src.rows(),nWidth=src.cols();
		byte data[] = new byte[src.channels()];
		Mat dst = Mat.eye(new Size(nHeight,nWidth),CvType.CV_8UC3);
		for(int i=0;i<nHeight;i++)
			for(int j=0;j<nWidth;j++) {
				src.get(i, j,data);
				dst.put(j, nHeight-i-1, data);
			}
		return dst;
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			String newFilename = "C:\\Users\\14345\\Pictures\\ocr_sample\\"
								 + "5.png";
			Mat src = Imgcodecs.imread(newFilename);
			Mat img;
			Mat temp = new Mat();
//			Mat temp = RotateImg(src);
			Core.transpose(src,temp);
			Mat result =new Mat();
			Core.transpose(temp,result);
//			Core.flip(temp, result, 1);
//			HighGui.imshow("旋转图片", result);
//			HighGui.waitKey();
			img = src.clone();
			System.out.println("channels = " + img.channels());
			
		
			//灰度化
			Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY); 
			ImgPreProcess.plotGrayHistogram(img);
			//自适应阈值
			Imgproc.adaptiveThreshold(img, img,255,
					Imgproc.ADAPTIVE_THRESH_MEAN_C, 
					Imgproc.THRESH_BINARY_INV, 15, 10);
			//前景后景分阈值
			Imgproc.threshold(img, img, 200, 255, 
					Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
//			HighGui.imshow("大津法阈值", img);
//			HighGui.waitKey(0);
			//中值滤波
			Imgproc.medianBlur(img, img, 5);
			//翻转像素
			Imgreverse(img);

			
			//定义形态学内核
			Mat kernel = Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(3,3));
			Mat kernel2 = Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(1,5));
//			Mat kernelDialate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4));
//			Mat kernelDialate2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,8));
//			Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,  new Size(6,6));
//			Mat kernelErode2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
//			Mat kernelErode3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
			//膨胀腐蚀运算
			
//			Imgproc.erode(img, img, kernel);
//			Imgproc.dilate(img, img, kernel);
			
//			Imgproc.dilate(img, img, kernel2);
//			Imgproc.erode(img, img, kernel2);
			
			
			//霍夫变换文本行倾斜矫正
//			ImgPreProcess.ImageHough(img);
			Mat dst = new Mat(img.size(),CvType.CV_8UC3);
			Point[] vertex=ImgPreProcess.findContours(img, dst);
			Mat imgRaw = new Mat(src.size(),src.type());
			ImgPreProcess.persTransform(src, imgRaw, vertex);
			System.out.println("四个顶点的左边分别为：");
			for(int i=0;i<4;i++)
				System.out.println((i+1)+":("+vertex[i].x+","+vertex[i].y+")");
			int x_start = (int) vertex[0].x;
			int y_start = (int) vertex[0].y;
			int height = (int) (vertex[2].y - vertex[0].y);
			int width = (int) (vertex[1].x - vertex[0].x);
			System.out.println(x_start+","+y_start+","+height+","+width);
			Mat screen = new Mat(imgRaw,new Rect(x_start,y_start,width,height));
//			HighGui.imshow("透视变换结果", screen);
//			HighGui.waitKey(0);
			screen = src;
			Imgproc.cvtColor(screen, screen, Imgproc.COLOR_RGB2GRAY); 
			Imgproc.adaptiveThreshold(screen, screen,255,
					Imgproc.ADAPTIVE_THRESH_MEAN_C, 
					Imgproc.THRESH_BINARY_INV, 25, 10);
			Imgproc.medianBlur(screen, screen, 5);
			Imgreverse(screen);
			Imgproc.erode(screen, screen, kernel);
			
			ShowImage wordpic = new ShowImage(screen);
			wordpic.getFrame().setVisible(true);
			wordpic.getFrame().setTitle("未分割前图像");
			Mat dst1 = new Mat();
			dst1 = ImgPreProcess.AdjustImgSize(screen);
			ShowImage wordpic2 = new ShowImage(dst1);
			wordpic2.getFrame().setVisible(true);
			wordpic2.getFrame().setTitle("resize后图像");
			System.out.println("resize后图像大小："+
			"Height:"+dst1.height()+",width:"+dst1.width());
			List<Mat> lines = new ArrayList<Mat>();
			List<Mat> words = new ArrayList<Mat>();
			List<Mat> rawImg = new ArrayList<Mat>();
			List<Mat> trueImg = new ArrayList<Mat>();
			List<String> data = new ArrayList<String>();
			lines = ocr.cutImgX(dst1);
			int count =0;
			int[][] number = new int[6][6];
			System.out.println("the number of the picture`s line :"
								+ String.valueOf(lines.size()) );
			String filePathOut1 = "C:\\Users\\14345\\Pictures\\words\\";
			int wordcount;
			for(Mat line : lines)
			{
				words = ocr.cutImgY(line);
				//滤除一行中不属于字符的图片
//				words=ocr.lineFilt(words);
				wordcount=0;
				for(Mat word : words)
				{
//					ShowImage wordpic = new ShowImage(word);
//					wordpic.getFrame().setVisible(true);
					if(ocr.imgFilt(word))
					{
						//精细切割文本
						word = ocr.cutTiny(word);
						if(ocr.charRecognize(word)!=-1)
							number[count][wordcount++]=ocr.charRecognize(word);
//						System.out.println("第"+count+"行第"+wordcount+"个字符图片大小：height:"+
//						word.height()+",width: "+word.width());
						ShowImage wordpic1 = new ShowImage(word);
						wordpic1.getFrame().setVisible(true);
						wordpic1.getFrame().setTitle(String.valueOf(count));
					}
				}
				System.out.print("第"+count+"行有"+wordcount+"个字符：");
				for(int i=0;i<wordcount;i++)
					System.out.print(number[count][i]);
				System.out.print("\n");
				count++;
				
			}
		}
		
}

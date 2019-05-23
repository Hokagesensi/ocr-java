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

import ocr_test01.ShowImage;
import ocr_test01.ocr;



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
		int temp = 15;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		for(int i=1;i<15;i++) {
		
			String newFilename = "C:\\Users\\14345\\Pictures\\ocr_sample\\"
								 +String.valueOf(i)+ ".png";
			System.out.println("第"+i+"张图片识别结果：");
			Mat src = Imgcodecs.imread(newFilename);
			Mat screen = src.clone();
			//调整图像大小
			Mat dst1 = ImgPreProcess.AdjustImgSize(src);
//			ShowImage wordpic2 = new ShowImage(dst1);
//			wordpic2.getFrame().setVisible(true);
//			wordpic2.getFrame().setTitle("resize后图像");
//			System.out.println("resize后图像大小："+
//					"Height:"+dst1.height()+",width:"+dst1.width());
			
			Imgproc.cvtColor(dst1, dst1, Imgproc.COLOR_RGB2GRAY); 
//			ShowImage wordpic3 = new ShowImage(dst1);
//			wordpic3.getFrame().setVisible(true);
//			wordpic3.getFrame().setTitle("灰度后图片");
			
			Imgproc.medianBlur(dst1, dst1, 15);
			
//			ShowImage wordpic4 = new ShowImage(dst1);
//			wordpic4.getFrame().setVisible(true);
//			wordpic4.getFrame().setTitle("中值滤波后图片");
			
			Imgproc.adaptiveThreshold(dst1, dst1,255,
					Imgproc.ADAPTIVE_THRESH_MEAN_C, 
					Imgproc.THRESH_BINARY, 39,8);
//			HighGui.imshow("阈值化的图片", dst1);
//			HighGui.waitKey(0);
			
//			Imgreverse(dst1);
			ocr.setFrameWhite(dst1, 10);
//			ShowImage wordpic5 = new ShowImage(dst1);
//			wordpic5.getFrame().setVisible(true);
//			wordpic5.getFrame().setTitle("边框变白后");
			
			
			Mat kernel = Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(10,15));
			Imgproc.erode(dst1, dst1, kernel);
			ShowImage wordpic = new ShowImage(dst1);
			wordpic.getFrame().setVisible(true);
			wordpic.getFrame().setTitle("膨胀后图像");
			//识别数字
			ocrRes(dst1);
		}
		}
		

	
	public static void ocrRes(Mat img){
		List<Mat> lines = new ArrayList<Mat>();
		List<Mat> words = new ArrayList<Mat>();
		List<Mat> rawImg = new ArrayList<Mat>();
		List<Mat> trueImg = new ArrayList<Mat>();
		List<String> data = new ArrayList<String>();
		lines = ocr.cutImgX(img);
		int count =0;
		int[][] number = new int[6][10];
//		System.out.println("the number of the picture`s line :"
//							+ String.valueOf(lines.size()) );
		int wordcount;
		for(Mat line : lines)
		{
			words = ocr.cutImgY(line);
			//滤除一行中不属于字符的图片
//			words=ocr.lineFilt(words);
			wordcount=0;
			for(Mat word : words)
			{
				ShowImage wordpic = new ShowImage(word);
				wordpic.getFrame().setVisible(true);
				if(ocr.imgFilt(word))
				{
					//精细切割文本
					word = ocr.cutTiny(word);
					if(ocr.charRecognize(word)!=-1&&ocr.imgFilt(word)) {
						number[count][wordcount++]=ocr.charRecognize(word);
//						System.out.println("第"+count+"行第"+wordcount+"个字符图片大小：height:"+
//						word.height()+",width: "+word.width());
						ShowImage wordpic1 = new ShowImage(word);
						wordpic1.getFrame().setVisible(true);
						wordpic1.getFrame().setTitle(String.valueOf(count+1));
					}
				}
			}
			
			System.out.print("第"+(count+1)+"行有"+wordcount+"个字符：");
			for(int i=0;i<wordcount;i++)
				System.out.print(number[count][i]);
			System.out.print("\n");
			count++;
		}
	}
}


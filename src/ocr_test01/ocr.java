package ocr_test01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

public class ocr {
	public int size = 0;
	
	/*
	 * �߿���
	 */
	public static void setFrameWhite(Mat img,int frameLength) {
		for(int i=0;i<img.width();i++) {
			for(int j=0;j<frameLength;j++)
				img.put(j, i, 255);
			for(int j=img.height();j>img.height()-frameLength;j--)
				img.put(j, i, 255);
		}
		for(int j=0;j<img.height();j++) {
			for(int i=0;i<frameLength;i++)
				img.put(j, i, 255);
			for(int i=img.width()-1;i>img.width()-frameLength;i--)
				img.put(j, i, 255);
		}
	}
	/*
	 * ��ͼƬ���л��߰���ͳ�ƺ�ɫ���صĶ���
	 * ���룺img@Mat,xy@boolean
	 * xyΪtrue�ǰ���ͳ��
	 * xyΪfalse�ǰ���ͳ��
	 * ���أ�������byte����ֱ�Ϊ�к��еĳ���
	 */
	public static int[] countBlack(Mat img, boolean xy)
	{
		int i, j;
	    int nWidth = img.cols(), nHeight = img.rows();
	    int[] xNum = new int[nHeight];
	    int[] yNum = new int[nWidth];
	    byte[] data = new byte[img.channels()];
		if(xy==true)
		{
			//����ͳ�ƺ�ɫ���ظ���
			for (i = 0; i < nHeight; i++) {
		        for (j = 0; j < nWidth; j++) {
		        	img.get(i, j,data);
		            if (data[0]==0) {
		                xNum[i]++;
		            }
		        }
		    }
			return xNum;
		}
		else
		{
			//����ͳ�ƺ�ɫ���صĸ���
			for (i = 0; i < nWidth; i++) {
		        for (j = 0; j < nHeight; j++) {
		        	img.get(j,i,data);
		            if (data[0]==0) {
		                yNum[i]++;
		            }
		        }
		    }
			return yNum;
		}
	}
	
	/*
	 * �ָ�ͼƬ�ı���
	 * ˮƽͶӰ������
	 * ���룺img@Mat
	 * �����Ymat@Mat
	 */
	public static List<Mat> cutImgX(Mat img) {
	    int i, j;
	    int nWidth = img.cols(), nHeight = img.rows();
	    int[] xNum = new int[nHeight];
	    byte[] data = new byte[nWidth];
	    
	    // ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    System.out.println("test success!");
	    xNum = countBlack(img,true);
	    
	    //�����зָ���ֵ
	    int threshLow = nWidth/30;
	    int count = 0;
	    // ����Ҫ�и��y�㶼�浽cutY��
	    List<Integer> cutY = new ArrayList<Integer>();
	    for (i = 0; i < nHeight; i++) {
	        if (xNum[i] < threshLow) {
	        	//������ӷָ���
	        	if(cutY.size()<=1)
	        	{
	        		cutY.add(i);
	        		count++;
	        	}
	        	//�ָ��߼�������ܸ߶ȵ�1/8���
	        	else if(i-cutY.get(count-1)>nHeight/4)
	        	{
	        			cutY.add(i);
	        			count++;
	        	}
	        	}
	    }

	    Mat curve = new Mat();
	    Mat src = img.clone();
	    curve.create(new Size(nWidth,nHeight), CvType.CV_8UC1);
	    for(int k=0;k<nHeight;k++)
	    {
	    	for(int u=0;u<xNum[k];u++)
	    	curve.put(k, u, 255);
	    }
	    for(i=0;i < cutY.size();i++)
	    {
	    	for(j=0;j<nWidth;j++) {
	    		src.put(cutY.get(i), j, 128);
	    		curve.put(cutY.get(i), j, 128);
	    	}
	    }
	    ShowImage linepic = new ShowImage(src);
	    linepic.getFrame().setVisible(true);
	    linepic.getFrame().setTitle("�ָ���ʾ��");
	    
	    ShowImage lineCpic = new ShowImage(curve);
	    lineCpic.getFrame().setVisible(true);
	    lineCpic.getFrame().setTitle("ͶӰ����ʾ��");
	    // ���и��ͼƬ�����浽YMat��
	    List<Mat> YMat = new ArrayList<Mat>();
	    int startY = 0;
	    int height = cutY.get(0);
	    if(height>nHeight/5)
        {
        Mat temp = new Mat(img, new Rect(0, startY, nWidth, height));
        Mat t = new Mat();
        temp.copyTo(t);
        YMat.add(t);
        }
	    for (i = 1; i < cutY.size(); i++) {
	        // ���ø���Ȥ������
	        startY = cutY.get(i - 1);
	        height = cutY.get(i) - startY;
	        //�˳�����
	        if(height>nHeight/5)
	        {
	        Mat temp = new Mat(img, new Rect(0, startY, nWidth, height));
	        Mat t = new Mat();
	        temp.copyTo(t);
	        YMat.add(t);
	        }
	    }
	    startY = cutY.get(i - 1);
	    height = nHeight - startY;
	    if(height>nHeight/6)
        {
        Mat temp = new Mat(img, new Rect(0, startY, nWidth, height));
        Mat t = new Mat();
        temp.copyTo(t);
        YMat.add(t);
        }
	    
	    return YMat;
	}
	/*
	 * �ָ�ÿ���ַ�
	 */
	public static List<Mat> cutImgY(Mat img)
	{
		int i, j;
	    int nWidth = img.cols(), nHeight = img.rows();
	    int[] yNum = new int[nWidth];
	    byte[] data = new byte[img.channels()];
	    
	    // ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    yNum = countBlack(img,false);
	    
	    //�����зָ���ֵ
	    int threshLow = nHeight/28;
	    int count = 0;
	    // ����Ҫ�и��y�㶼�浽cutY��
	    List<Integer> cutX = new ArrayList<Integer>();
	    for (i = 0; i < nWidth; i++) {
	        if (yNum[i] <= threshLow) {
	        	//������ӷָ���
	        	if(cutX.size()<=0)
	        	{
	        		cutX.add(i);
	        		count++;
	        	}
	        	//�ָ��߼�������ܿ�ȵ�1/4���
	        	else if(i-cutX.get(count-1)>=nWidth/15)
	        	{
	        			cutX.add(i);
	        			count++;
	        	}
	        	}
	    }
	    Mat src1 = img.clone();
	    
	    for(i=0;i < cutX.size();i++)
	    {
	    	for(j=0;j<nHeight;j++) {
	    		src1.put(j,cutX.get(i), 128);
	    	}
	    }
	    ShowImage charpic = new ShowImage(src1);
	    charpic.getFrame().setVisible(true);
	    charpic.getFrame().setTitle("�ַ��ָ����");
	    
	    
	 // ���и��ͼƬ�����浽YMat��
	    List<Mat> XMat = new ArrayList<Mat>();
	    int startX = 0;
	    int width = cutX.get(0);
	    if(width>nWidth/15)
        {
        Mat temp = new Mat(img, new Rect(startX,0, width,nHeight));
//        ShowImage charpic1 = new ShowImage(src1);
//	    charpic1.getFrame().setVisible(true);
        Mat t = new Mat();
        temp.copyTo(t);
        XMat.add(t);
        }
	    for (i = 1; i < cutX.size(); i++) {
	        // ���ø���Ȥ������
	        startX = cutX.get(i - 1);
	        width = cutX.get(i) - startX;
	        //�˳�����
	        if(width>nWidth/15)
	        {
	        Mat temp = new Mat(img, new Rect(startX,0, width,nHeight));
	        Mat t = new Mat();
	        temp.copyTo(t);
	        XMat.add(t);
	        }
	    }
	    startX = cutX.get(i-1);
	    width = nWidth - startX;
        if(width>nWidth/10)
        {
        Mat temp = new Mat(img, new Rect(startX,0, width,nHeight));
        Mat t = new Mat();
        temp.copyTo(t);
        XMat.add(t);
        }
		return XMat;
	}
	/*
	 * ������ƽ��ֵ
	 */
	public static int average(int[] num)
	{
		int temp=0;
		for(int i=0;i<num.length;i++)
			temp += num[i];
		temp/=num.length;
		return temp;
	}
	/*
	 * �������
	 */
	public static int sum(int[] num)
	{
		int temp=0;
		for(int i=0;i<num.length;i++)
			temp+=num[i];
		return temp;	
	}
	/*
	 * ����һ���в����ϵ�ͼƬ
	 */
	public static List<Mat> lineFilt(List<Mat> words){
		int charImgWidth=0,charImgHeight=0;
		int Length=words.size();
		for(int i=0;i<Length;i++) {
			charImgWidth+=words.get(i).width();
			charImgHeight+=words.get(i).height();
		}
		charImgWidth/=Length;
		charImgHeight/=Length;
		for(int j=0;j<Length;j++)
		{
			if(words.get(j).width()>charImgWidth/10&&
					words.get(j).height()>charImgHeight/4) {
				words.remove(j);
				Length--;
				j--;
			}
		}
		return words;
	}
	
	/*
	 * ���˿հ�ͼƬ
	 * ���룺img@Mat
	 * �����false-�հ�ͼƬ��true-��ЧͼƬ
	 */
	public static boolean imgFilt(Mat img)
	{
		int i,j;
		int nWidth = img.cols(),nHeight = img.rows();
		int[] xNum = new int[nHeight];
		int[] yNum = new int[nWidth];
		int temp1 = 0;
		int temp2 = 0;
		
		// ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    xNum = countBlack(img,true);
	    //ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    yNum = countBlack(img,false);

	    
	    temp1 = average(xNum);
	    temp2 = average(yNum);
	    
//	    int area = img.height()*img.width();
	    int threshFilt = nWidth/10;
//	    System.out.println("temp1: "+temp1+", temp2: "+temp2+",area:"+area);
	    //�ж��Ƿ�Ϊ�հ�ͼƬ
	    if(temp1<=threshFilt&&temp2<=threshFilt)
	    	return false;
	    else if(nWidth<=50&&nHeight<=50)
	    	return false;
	    else
	    	return true;
	    	 
	}
	
	
	/*
	 * �ַ���׼�и�ֱ�ˮƽ�ʹ�ֱͶӰ���ָ�ͼƬ
	 */
	public static Mat cutTiny(Mat img)
	{
		int i, j;
		boolean xJge,yJge;
		Mat TinyMat = new Mat();
		int nWidth = img.cols(),nHeight = img.rows();
		int[] xNum = new int[nHeight];
		int[] yNum = new int[nWidth];
		
		// ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    xNum = countBlack(img,true);
	    //ͳ�Ƴ�ÿ�к�ɫ���ص�ĸ���
	    yNum = countBlack(img,false);
	    List<Integer> cutX = new ArrayList<Integer>();
	    List<Integer> cutY = new ArrayList<Integer>();
	    int XthreshLow = nWidth/10;
	    int YthreshLow = nHeight/30;
	    i=0;
	    xJge=true;
	    while(xJge&&i<nHeight)
	    {
	    	if(xNum[i]>=XthreshLow)
	    	{
	    		cutX.add(i);
	    		xJge=false;
	    	}
	    	else
	    		i++;
	    }
	    i=nHeight-1;
	    xJge=true;
	    while(xJge&&i>=0)
	    {
	    	if(xNum[i]>=XthreshLow)
	    	{
	    		cutX.add(i);
	    		xJge=false;
	    	}
	    	else
	    		i--;
	    }
	    
	    j=0;
	    yJge=true;
	    while(yJge&&j<nWidth)
	    {
	    	if(yNum[j]>=YthreshLow)
	    	{
	    		cutY.add(j);
	    		yJge=false;
	    	}
	    	else
	    		j++;
	    }
	    
	    j=nWidth-1;
	    yJge=true;
	    while(yJge&&j>=0)
	    {
	    	if(yNum[j]>=YthreshLow)
	    	{
	    		cutY.add(j);
	    		yJge=false;
	    	}
	    	else
	    		j--;
	    }
	    
	    
	    int width = cutX.get(1)-cutX.get(0);
	    int height = cutY.get(1)-cutY.get(0);
//	    System.out.println("cutX(0):"+cutX.get(0)+" cutX(1): "+cutX.get(1)+" width: "+width);
//	    System.out.println("cutY(0):"+cutY.get(0)+" cutY(1): "+cutY.get(1)+" height: "+height);
//	    System.out.println("");
	    
//	    //��Ӹ��ߣ���ʾ�ָ�״��
//	    for(i=0;i<cutX.size();i++)
//	    {
//	    	for(j=0;j<nWidth;j++)
//	    	{
//	    		img.put(cutX.get(i),j, 128);
//	    	}
//	    }
//	    for(i=0;i<cutY.size();i++)
//	    {
//	    	for(j=0;j<nHeight;j++)
//	    		img.put( j,cutY.get(i), 128);
//	    }
//	    ShowImage charpic = new ShowImage(img);
//	    charpic.getFrame().setVisible(true);
	    
	    Mat temp = new Mat(img, new Rect(cutY.get(0),cutX.get(0),height,width));
		return temp;
	}
	
	
	/*
	 * ���߷�ʶ���ַ�
	 */
	
	public static int charRecognize(Mat img)
	{
		int res = 0;
		int i,j;
		int nWidth = img.cols(),nHeight = img.rows();
		//���߷�ʶ�����������
		int line1 = nWidth/2;
		int line2 = nHeight/3;
		int line3 = nHeight/3*2;
		byte[] data = new byte[img.channels()];
		int pen = nHeight/30;
		int[] tube = new int[6];
		int[] count = new int[8];
		//�ж��Ƿ�Ϊһ���⴦��
		if(nHeight>=3*nWidth)
		{
			for(j=0;j<nWidth;j++)
			{
				img.get(line2, j,data);
				if(data[0]==0)
					count[3]++;
				img.get(line3, j,data);
				if(data[0]==0)
					count[6]++;
			}
			if(count[3]>=pen||count[6]>=pen)
				return 1;
		}
		else
		{
			for(j=0;j<line1;j++)
			{
				img.get(line2, j, data);
				if(data[0]==0)
					count[1]++;
				img.get(line3, j, data);
				if(data[0]==0)
					count[5]++;
			}
			for(j=line1;j<nWidth;j++)
			{
				img.get(line2, j, data);
				if(data[0]==0)
					count[3]++;
				img.get(line3, j, data);
				if(data[0]==0)
					count[6]++;
			}
			for(i=0;i<line2;i++)
			{
				img.get(i, line1, data);
				if(data[0]==0)
					count[2]++;
			}
			for(i=line2;i<line3;i++)
			{
				img.get(i, line1, data);
				if(data[0]==0)
					count[4]++;
			}
			for(i=line3;i<nHeight;i++)
			{
				img.get(i, line1, data);
				if(data[0]==0)
					count[7]++;
			}
			
			
			//�ж�����ܵ������ֶ�
			for(i=0;i<count.length;i++)
			{
				if(count[i]>=pen)
					res+=i;
			}
			switch(res)
			{
			case 21:
				return 2;
			case 22:
				return 3;
			case 14:
				return 4;
			case 20:
				return 5;
			case 25:
				return 6;
			case 11:
				return 7;
			case 12:
				return 7;
			case 28:
				return 8;
			case 23:
				return 9;
			case 24:
				return 0;
			default:
				return -1;
			}
			
		}		
		return -1;
	}
	
	
	
}

package ocr_test01;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
 
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
 
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
public class ShowImage {
	 private JFrame frame;  
	  
	  
	    /** 
	     * Create the application. 
	     */  
	    public ShowImage(Mat mat) {  
	        initialize(mat);  
	    }  
	  
	    public JFrame getFrame() {
			return frame;
		}
	    /** 
	     * Initialize the contents of the frame. 
	     */  
	    private void initialize(Mat mat) {  
	        frame = new JFrame();  
	        frame.setBounds(20, 100, mat.width()+100, mat.height()+80);  
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	        frame.getContentPane().setLayout(null);           
	        BufferedImage image=new MatToBufImg(mat, ".png").getImage();  
	        JLabel label = new JLabel(""){
	        	/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
	        	public void setLabelFor(Component c) {
	        		// TODO Auto-generated method stub
	        		super.setLabelFor(c);
	        	}
	        };  
	        label.setBounds(0, 0, mat.width(), mat.height());
	        frame.getContentPane().add(label);  
	        label.setIcon(new ImageIcon(image));  
	    }  

}

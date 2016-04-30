package imageconversion;
import static java.lang.System.out;
import static java.nio.file.StandardOpenOption.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageConversion {

	private static final int IMG_WIDTH = 400;
	private static final int IMG_HEIGHT = 300;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte [] imageHeader = { 0x33, 0x01, (byte)0x90, 0x01, 0x2C, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		
		int Wp, Hp;
		Wp = 400; Hp = 300;

		try {

			// Open the file
			Path samplePage = Paths.get("./sample.txt");
			FileInputStream fstream = new FileInputStream(samplePage.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			StringBuffer stringBuffer = new StringBuffer();
			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console

				stringBuffer.append(strLine);
				stringBuffer.append("\n");
			}

			//Close the input stream
			br.close();
			
			BufferedImage original = new BufferedImage(IMG_HEIGHT ,IMG_WIDTH , BufferedImage.TYPE_BYTE_INDEXED);
	        Graphics2D graphics = (Graphics2D)original.getGraphics();
	        graphics.setColor(Color.WHITE);
	        graphics.fillRect(0, 0,IMG_HEIGHT  , IMG_WIDTH);
	        graphics.setColor(Color.BLACK);
	        graphics.setFont(new Font("Times New Roman", Font.PLAIN, 14));
	        
	        FontMetrics fm = graphics.getFontMetrics();
	        int characterHeight = fm.getHeight();
	        int maxNoLines = ( IMG_WIDTH - 10 ) / characterHeight;
	        List<String> strings = WrapString.wrap(stringBuffer.toString(), fm, IMG_HEIGHT - 30);
	        for (int i = 0; i < maxNoLines; i++)
	        {
	        	graphics.drawString(strings.get(i), 10, (20 + characterHeight * i) );
	        }
	        
	        graphics.dispose();
	        System.out.println("Image Created");

	        //rotate the picture here
	        // Drawing the rotated image at the required drawing locations
	        BufferedImage img = rotate90ToRight(original);
	        

			out.printf("img.getWidth(): %d img.getHeight(): %d\r\n", img.getWidth(), img.getHeight() );
	        
			// Image conversion code snippet:
		    // Convert to gray
			img = ConvertTools.downsampleTo8bitGrayScale(img);
	
		    int [] rawIntPixelData = ConvertTools.toIntArray(img);
	
		    rawIntPixelData = ConvertTools.downsampleTo1bitGrayScale(rawIntPixelData);
	
		    // convert to byte array
		    byte [] rawBytePixelData = ConvertTools.toByteArray(rawIntPixelData);
		    byte [] convertedBytePixelData = ConvertTools.convertTo1bit_PixelFormatType2(rawBytePixelData, Wp, Hp);
	    	byte [] fullImageData = combine(imageHeader, convertedBytePixelData);

	    	Path p = Paths.get("./testbookPage.epd");

	        try (OutputStream out = new BufferedOutputStream(
	          Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING ))) {
	          out.write(fullImageData, 0, fullImageData.length);
	          out.close();
	        } catch (IOException x) {
	          System.err.println(x);
	        }
	        
			out.printf("Image Header Length: %d\r\n", imageHeader.length );
			out.printf("rawBytePixelData Length: %d\r\n", rawBytePixelData.length );
			out.printf("convertedBytePixelData Length: %d\r\n", convertedBytePixelData.length );
			out.printf("fullImageData Length: %d\r\n", fullImageData.length );
		} catch (Exception e) {
			out.println(e.toString());
		}
	}
	public static BufferedImage rotate90ToLeft( BufferedImage inputImage ){
		//The most of code is same as before
			int width = inputImage.getWidth();
			int height = inputImage.getHeight();
			BufferedImage returnImage = new BufferedImage( height, width , inputImage.getType()  );
		//We have to change the width and height because when you rotate the image by 90 degree, the
		//width is height and height is width <img src='http://forum.codecall.net/public/style_emoticons/<#EMO_DIR#>/smile.png' class='bbc_emoticon' alt=':)' />

			for( int x = 0; x < width; x++ ) {
				for( int y = 0; y < height; y++ ) {
					returnImage.setRGB(y, (width - x - 1), inputImage.getRGB( x, y  )  );
		//Again check the Picture for better understanding
				}
				}
			return returnImage;

		}
	
	public static BufferedImage rotate90ToRight( BufferedImage inputImage ){
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage returnImage = new BufferedImage( height, width , inputImage.getType()  );

		for( int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				returnImage.setRGB( height - y -1, x, inputImage.getRGB( x, y  )  );
	//Again check the Picture for better understanding
			}
		}
		return returnImage;
	}
	private void ConvertSampleImage() {
		byte [] imageHeader = { 0x33, 0x01, (byte)0x90, 0x01, 0x2C, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		
		int Wp, Hp;
		Wp = 400; Hp = 300;

		try {
			BufferedImage originalImg = ImageIO.read(new File("DSC_0053.JPG"));

			int type = originalImg.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImg.getType();
				
			BufferedImage img = resizeImage(originalImg, type);
			
			// Image conversion code snippet:
		    // Convert to gray
			img = ConvertTools.downsampleTo8bitGrayScale(img);
	
		    int [] rawIntPixelData = ConvertTools.toIntArray(img);
	
		    rawIntPixelData = ConvertTools.downsampleTo1bitGrayScale(rawIntPixelData);
	
		    // convert to byte array
		    byte [] rawBytePixelData = ConvertTools.toByteArray(rawIntPixelData);
		    byte [] convertedBytePixelData = ConvertTools.convertTo1bit_PixelFormatType2(rawBytePixelData, Wp, Hp);
	    	byte [] fullImageData = combine(imageHeader, convertedBytePixelData);

	    	Path p = Paths.get("./testpicture.epd");

	        try (OutputStream out = new BufferedOutputStream(
	          Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING ))) {
	          out.write(fullImageData, 0, fullImageData.length);
	          out.close();
	        } catch (IOException x) {
	          System.err.println(x);
	        }
	        
			out.printf("Image Header Length: %d\r\n", imageHeader.length );
			out.printf("rawBytePixelData Length: %d\r\n", rawBytePixelData.length );
			out.printf("convertedBytePixelData Length: %d\r\n", convertedBytePixelData.length );
			out.printf("fullImageData Length: %d\r\n", fullImageData.length );
		} catch (Exception e) {
			out.println(e.toString());
		}
	}
	
	
	private static byte[] combine(byte[] a, byte[] b){
        int length = a.length + b.length;
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
	private static BufferedImage resizeImage(BufferedImage originalImage, int type){
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
			
		return resizedImage;
	    }
}

package imageconversion;
import static java.lang.System.out;
import static java.nio.file.StandardOpenOption.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
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
			String key = "Sample";
	        BufferedImage img = new BufferedImage(IMG_WIDTH ,IMG_HEIGHT , BufferedImage.TYPE_BYTE_INDEXED);
	        Graphics2D graphics = (Graphics2D)img.getGraphics();
	        graphics.setColor(Color.WHITE);
	        graphics.fillRect(0, 0,IMG_WIDTH ,IMG_HEIGHT );
	        graphics.setColor(Color.BLACK);
	        graphics.setFont(new Font("Arial Black", Font.BOLD, 20));
	        graphics.drawString(key, 10, 25);
	        //rotate the picture here
	        // Drawing the rotated image at the required drawing locations
	        graphics.dispose();
	        System.out.println("Image Created");
//	        AffineTransform tx = new AffineTransform();
//	        tx.rotate(Math.toRadians(90), IMG_HEIGHT / 2, IMG_WIDTH / 2);
//	        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
//
//	        img = op.filter(img, null);

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

package imageconversion;
import static java.lang.System.out;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageConversion {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte [] imageHeader = { 0x33, 0x01, (byte)0x90, 0x01, 0x2C, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		
		int Wp, Hp;
		Wp = 400; Hp = 300;

		try {
			BufferedImage img = ImageIO.read(new File("DSC_0053.JPG"));
			// Image conversion code snippet:
		    // Convert to gray
			img = ConvertTools.downsampleTo8bitGrayScale(img);
	
		    int [] rawIntPixelData = ConvertTools.toIntArray(img);
	
		    rawIntPixelData = ConvertTools.downsampleTo1bitGrayScale(rawIntPixelData);
	
		    // convert to byte array
		    byte [] rawBytePixelData = ConvertTools.toByteArray(rawIntPixelData);
		    rawBytePixelData = ConvertTools.convertTo1bit_PixelFormatType2(rawBytePixelData, Wp, Hp);
	
		    byte [] fullImageData = combine(imageHeader, rawBytePixelData);
		    
	
			out.printf("Image Header Length: %d\r\n", imageHeader.length );
			out.printf("rawBytePixelData Length: %d\r\n", rawBytePixelData.length );
		} catch (Exception e) {
			out.println(e.toString());
		}
	}

	public static byte[] combine(byte[] a, byte[] b){
        int length = a.length + b.length;
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}

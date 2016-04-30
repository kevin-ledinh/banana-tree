package imageconversion;
import java.awt.image.*;

public class ImageConversion {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int Wp, Hp;
		int imageHeader_length;
		
		Wp = 0; Hp = 0;
		imageHeader_length = 0;
		
		// Image conversion code snippet:
	    // Convert to gray
		//Read in the image
		BufferedImage img = new BufferedImage(100,100,100);
		img = ConvertTools.downsampleTo8bitGrayScale(img);

	    int [] rawIntPixelData = ConvertTools.toIntArray(img);

	    rawIntPixelData = ConvertTools.downsampleTo1bitGrayScale(rawIntPixelData);

	    // convert to byte array
	    byte [] rawBytePixelData = ConvertTools.toByteArray(rawIntPixelData);
	    rawBytePixelData = ConvertTools.convertTo1bit_PixelFormatType2(rawBytePixelData, Wp, Hp);

	    byte [] fullImageData = new byte[imageHeader_length + rawBytePixelData.length];
	}



}

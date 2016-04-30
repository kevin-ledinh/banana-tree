package imageconversion;
import java.awt.*;
import java.awt.image.*;

public class ConvertTools {

	//Used methods:

	/**
	 * Convert a color image to 8bit grayscale image.
	 * @param bi
	 * @return
	 */
	public static BufferedImage downsampleTo8bitGrayScale(BufferedImage bi) {
	    if (ConvertTools.isGray(bi)) {
	        return bi;
	    }

	    BufferedImage gray = new BufferedImage(bi.getWidth(), bi.getHeight(),
	            BufferedImage.TYPE_BYTE_GRAY);
	    Graphics gr = gray.getGraphics();
	    gr.drawImage(bi, 0, 0, null);
	    gr.dispose();
	    return gray;

	}

	/**
	 * Converts a BufferedImage object to int[] array.
	 * @param bi Any BufferedImage
	 * @return Integer array with pixel data.
	 */
	public static int[] toIntArray(BufferedImage bi) {
	    int[] data = new int[bi.getWidth() * bi.getHeight()];
	    int i = 0;
	    for (int y = 0; y < bi.getHeight(); y++) {
	        for (int x = 0; x < bi.getWidth(); x++) {
	            data[i++] = (0xFF & bi.getRGB(x, y));
	        }
	    }
	    return data;
	}

	/**
	 * Downsample a grayscale image using a threshold value. Threshold is set to 127 value of gray.
	 * @param imageIntArray Integer array with image data.
	 * @return Downsampled 1-bit image data in an integer array. Values inside the array after downsampling are 0 or 255.
	 */
	public static int[] downsampleTo1bitGrayScale(int[] imageIntArray) {
	    for (int i = 0; i < imageIntArray.length; i++) {
	        if (imageIntArray[i] <= 127) {
	            imageIntArray[i] = 255;
	        } else {
	            imageIntArray[i] = 0;
	        }
	    }
	    return imageIntArray;
	}
	/**
	 * Convert integer array to byte array by cast. Lowest byte of integer is copied to byte array.
	 * @param intArray
	 * @return
	 */
	public static byte[] toByteArray(int[] intArray) {
	    byte[] bytes = new byte[intArray.length];
	    for (int i = 0; i < intArray.length; i++) {
	        bytes[i] = (byte) (intArray[i] & 0xFF);
	    }
	    return bytes;
	}

	//This format type is accepted by TCS-P441, TCS-P441-230, TCS2-P441-231
	public static byte[] convertTo1bit_PixelFormatType2(byte[] picData, int w, int h) {

	    byte[] newRow = new byte[picData.length * 1 / 8];

	    // join nibbles so 1 byte is 8 pixels and interlace at the same time
	    int j = 0;
	    for (int i = 0; i < picData.length; i = i + 8) {
	        newRow[j] = (byte) (((picData[i + 0] << 7) & 0x80)
	                | ((picData[i + 4] << 6) & 0x40)
	                | ((picData[i + 1] << 5) & 0x20)
	                | ((picData[i + 5] << 4) & 0x10)
	                | ((picData[i + 2] << 3) & 0x08)
	                | ((picData[i + 6] << 2) & 0x04)
	                | ((picData[i + 3] << 1) & 0x02) 
					| ((picData[i + 7]) 	 & 0x01));

	        j++;
	    }
	    return newRow;
	}

	//This format type is accepted by TCS-P441-230, TCS2-P441-231, TCS-P102-220, TCS2-P102-231
	static byte[] convertTo1bit_PixelFormatType0(byte[] picData, int w, int h) {
	    
	    byte[] newRow = new byte[picData.length * 1 / 8];

	    // join nibbles so 1 byte is 8 pixels
	    int j = 0;
	    for (int i = 0; i < picData.length; i = i + 8) {
	        newRow[j] = (byte) 
	                 (((picData[i + 0] << 7) & 0x80)
	                | ((picData[i + 1] << 6) & 0x40)
	                | ((picData[i + 2] << 5) & 0x20)
	                | ((picData[i + 3] << 4) & 0x10)
	                | ((picData[i + 4] << 3) & 0x08)
	                | ((picData[i + 5] << 2) & 0x04)
	                | ((picData[i + 6] << 1) & 0x02) 
	                | ((picData[i + 7] << 0) & 0x01));

	        j++;
	    }
	    return newRow;
	}

	//This format type is accepted by TCS-P74-110, TCS-P74-220, TCS-P74-230
	static byte[] convertTo1bit_PixelFormatType4(byte[] picData, int w, int h) 	{
		byte[] newPicData = new byte[picData.length / 8];
		int row = 30, s = 1;
		for (int i = 0; i < picData.length; i += 16)
		{
			newPicData[row-s] = (byte)(	((picData[i + 6 ] << 7) & 0x80) |
								((picData[i + 14] << 6) & 0x40) |
								((picData[i + 4 ] << 5) & 0x20) |
								((picData[i + 12] << 4) & 0x10) |
								((picData[i + 2 ] << 3) & 0x08) |
								((picData[i + 10] << 2) & 0x04) |
								((picData[i + 0 ] << 1) & 0x02) |
								((picData[i + 8 ] << 0) & 0x01));

			newPicData[row+30-s] = (byte) ( 	((picData[i + 1 ] << 7) & 0x80) |
								((picData[i + 9 ] << 6) & 0x40) |
								((picData[i + 3 ] << 5) & 0x20) |
								((picData[i + 11] << 4) & 0x10) |
								((picData[i + 5 ] << 3) & 0x08) |
								((picData[i + 13] << 2) & 0x04) |
								((picData[i + 7 ] << 1) & 0x02) |
								((picData[i + 15] << 0) & 0x01));
			s++;
			if(s==31)
			{
				s=1;
				row+=60;
			}
		}
		return newPicData;
	}	
	private static boolean isGray(BufferedImage img) {
		boolean flag = true;
		int width = img.getWidth();
        int height = img.getHeight();

        int pixel,red, green, blue;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //scan through each pixel
                pixel = img.getRGB(i, j);
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = (pixel) & 0xff;

                //check if R=G=B
                if ((red != green) || 
                	(green != blue) || 
                	(red != blue)) 
                {
                    flag = false;
                    break;
                }


            }
        }
        return flag;
	}
}

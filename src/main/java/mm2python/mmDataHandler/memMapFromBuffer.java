package mm2python.mmDataHandler;

import mm2python.UI.reporter;
import mm2python.mmDataHandler.Exceptions.NoImageException;
import org.micromanager.data.Image;
import mm2python.DataStructures.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;


/**
 * contains methods for writing image data to a memory mapped buffer
 */
public class memMapFromBuffer {

    private final Image temp_img;
    private MappedByteBuffer buffer;

    public memMapFromBuffer(Image temp_img_, MappedByteBuffer buffer_) {
        temp_img = temp_img_;
        buffer = buffer_;
    }

    public void writeToMemMap() throws NoImageException, Exception{
        byte[] byteimg;
        byteimg = convertToByte(temp_img);
        if (byteimg == null) {
            throw new NoImageException("image not converted to byte[]");
        }
        try
        {
            buffer.put(byteimg);
            buffer.force();
        } catch (Exception ex) {
            reporter.set_report_area("EXCEPTION DURING PUT OF BUFFER");
            throw ex;
        }
    }

    public void writeToMemMapAt(int position) throws NoImageException {
        byte[] byteimg;
        byteimg = convertToByte(temp_img);
        if (byteimg == null) {
            throw new NoImageException("image not converted to byte[]");
        }
        try
        {
            buffer.position(position);
            buffer.put(byteimg, 0, byteimg.length);
            buffer.force();
        } catch (Exception ex) {
            reporter.set_report_area("!! Exception !! during write to memmap = "+ex);
            throw ex;
        }
    }

    public void verifyMemMapAt(int position) {
        byte[] byteimg;
        byteimg = convertToByte(temp_img);
        byte[] buf_bytes = new byte[buffer.capacity()];
        buffer.position(position);
        buffer.get(buf_bytes, 0, buf_bytes.length);
        if(!Arrays.equals(byteimg,buf_bytes)) {
            reporter.set_report_area("BYTE IMAGE NOT EQUAL");
            Constants.data_mismatches += 1;
            reporter.set_report_area("Constants mismatches = "+Integer.toString(Constants.data_mismatches));
        } else {
            reporter.set_report_area("IMAGE EQUALS BYTE BUFFER!");
        }
    }

    private byte[] convertToByte(Image tempImg_) throws UnsupportedOperationException {
        try
        {
            byte[] bytes;
            Object pixels = tempImg_.getRawPixels();
            if (pixels instanceof byte[]) {
                bytes = (byte[]) pixels;
            }
            else if (pixels instanceof short[]) {
                ShortBuffer shortPixels = ShortBuffer.wrap((short[]) pixels);
                ByteBuffer dest = ByteBuffer.allocate(2 * ((short[]) pixels).length).order(ByteOrder.nativeOrder());
                ShortBuffer shortDest = dest.asShortBuffer();
                shortDest.put(shortPixels);
                bytes = dest.array();
            }
            else {
                throw new UnsupportedOperationException("Unsupported pixel type");
            }
            return bytes;

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

}



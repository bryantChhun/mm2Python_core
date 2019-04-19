/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mm2python.mmDataHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import mm2python.DataStructures.MetaDataStore;
import mm2python.DataStructures.constants;
import mm2python.mmDataHandler.Exceptions.NoImageException;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import mm2python.UI.reporter;

/**
 *
 * @author bryant.chhun
 */
public class memMapImage {
    private final Image temp_img;
    private final String filename;
    private final Coords coord;
    private final String prefix;
    private final String window_name;
    private final String[] channel_names;
    private String channel_name = null;
    private final Studio mm;
    
    public memMapImage(Studio mm_, Image temp_img_, Coords coord_, String filename_, String prefix_, String window_name_, String[] channel_names_) {
        temp_img = temp_img_;
        filename = filename_;
        prefix = prefix_;
        coord = coord_;
        window_name = window_name_;
        channel_names = channel_names_;
        mm = mm_;
        try {
            channel_name = mm.getCMMCore().getCurrentConfig("Channel");
        } catch (Exception ex) {
            reporter.set_report_area(false, false, "exception fetching channel name: "+ex.toString());
        }
    }
    
    public void writeToMemMap() throws NoImageException {
        byte[] byteimg;
                
        File file = new File(filename);
        file.delete();

        // check that parameters are not Nonetype
        // TODO: we get channel names by calling data_.getSummaryMetadata.  This can fail if exposure is too low (<50 ms)
        // TODO: we need to find another way to get channel names, perhaps we check them in mm settings first?
        reporter.set_report_area(false, false, "coord.getchannel = "+coord.getChannel());
        reporter.set_report_area(false, false, "channel_names = "+Arrays.toString(channel_names));
        reporter.set_report_area(false, false, "channel name = "+channel_names[coord.getChannel()]);
        reporter.set_report_area(false, false, "channel name from mmc = "+channel_name);
        reporter.set_report_area(false, false, "writeToMemMap filename = "+filename);

        // write data as memmap to memmap file
        try (   FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel())
        {
            byteimg = convertToByte(temp_img);
            if(byteimg == null) {
                throw new NoImageException("image not converted to byte[]");
            }
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, byteimg.length);
            buffer.put(byteimg);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        // write filename to queue based on Channel
        try {
//            // by summary meta data
//            reporter.set_report_area(false, false, "writing chan to filename map = ("+filename+", "+channel_names[coord.getChannel()]+")" );
//            constants.putChanToFilenameMap(channel_names[coord.getChannel()], filename);

            // by cmm core
            reporter.set_report_area(false, false, "writing chan to filename map = ("+filename+", "+channel_name+")" );
            constants.putChanToFilenameMap(channel_name, filename);
        } catch (Exception ex) {
            reporter.set_report_area(false, false, ex.toString());
        }

        // write metastore to queue based on channel
        // write filename to queue based on metastore
        try {
            MetaDataStore meta = new MetaDataStore(prefix, window_name,
                    coord.getTime(),
                    coord.getStagePosition(),
                    coord.getZ(),
                    coord.getChannel(),
                    temp_img.getWidth(),
                    temp_img.getHeight(),
                    temp_img.getBytesPerPixel(),
                    channel_name);

            reporter.set_report_area(false, false, "writing meta = "+meta.toString());

            constants.putMetaStoreToFilenameMap(meta, filename);

            constants.putChanToMetaStoreMap(channel_name, meta);

            constants.putNextImage(filename);

        } catch (NullPointerException ex) {
            reporter.set_report_area(false, false, "null ptr exception writing to LinkedBlockingQueue");
        } catch (Exception ex) {
            reporter.set_report_area(false, false, ex.toString());
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
    
    private byte[] convertToByteViaOutputStream(Image tempImg_) {
        try (   ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                )
        {
            byte[] bytes;
            bos.reset();
            oos.writeObject(tempImg_.getRawPixels());
            oos.flush();
            bytes = bos.toByteArray();
            return bytes;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
}


    
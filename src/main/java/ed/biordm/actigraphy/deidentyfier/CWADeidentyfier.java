/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ed.biordm.actigraphy.deidentyfier;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 *
 * @author tzielins
 */
public class CWADeidentyfier {

    static final int DEVICE_ID_POS = 5;
    /*
    CWA header definition from: 
        https://github.com/digitalinteraction/openmovement/blob/master/Docs/ax3/ax3-technical.md    
        https://github.com/digitalinteraction/openmovement/blob/master/Docs/ax3/cwa.h    
    
typedef struct
{
    uint16_t packetHeader;                      ///< @ 0  +2   ASCII "MD", little-endian (0x444D)
    uint16_t packetLength;                      ///< @ 2  +2   Packet length (1020 bytes, with header (4) = 1024 bytes total)
    uint8_t  hardwareType;                      ///< @ 4  +1   Hardware type (0x00/0xff/0x17 = AX3, 0x64 = AX6)
    uint16_t deviceId;                          ///< @ 5  +2   Device identifier
    uint32_t sessionId;                         ///< @ 7  +4   Unique session identifier
    uint16_t upperDeviceId;                     ///< @11  +2   Upper word of device id (if 0xffff is read, treat as 0x0000)
    cwa_timestamp_t loggingStartTime;           ///< @13  +4   Start time for delayed logging
    cwa_timestamp_t loggingEndTime;             ///< @17  +4   Stop time for delayed logging
    uint32_t loggingCapacity;                   ///< @21  +4   (Deprecated: preset maximum number of samples to collect, should be 0 = unlimited)
    uint8_t  reserved1[1];                      ///< @25  +1   (1 byte reserved)
    uint8_t  flashLed;                          ///< @26  +1   Flash LED during recording
    uint8_t  reserved2[8];                      ///< @27  +8   (8 bytes reserved)
    uint8_t  sensorConfig;                      ///< @35  +1   Fixed rate sensor configuration (AX6 only), 0x00 or 0xff means accel only, otherwise bottom nibble is gyro range (8000/2^n dps): 2=2000, 3=1000, 4=500, 5=250, 6=125, top nibble non-zero is magnetometer enabled.
    uint8_t  samplingRate;                      ///< @36  +1   Sampling rate code, frequency (3200/(1<<(15-(rate & 0x0f)))) Hz, range (+/-g) (16 >> (rate >> 6)).
    cwa_timestamp_t lastChangeTime;             ///< @37  +4   Last change meta-data time
    uint8_t  firmwareRevision;                  ///< @41  +1   Firmware revision number
    int16_t  timeZone;                          ///< @42  +2   (Unused: originally reserved for a "Time Zone offset from UTC in minutes", 0xffff = -1 = unknown)
    uint8_t  reserved3[20];                     ///< @44  +20  (20 bytes reserved)
    uint8_t  annotation[OM_METADATA_SIZE];      ///< @64  +448 Scratch buffer / meta-data (448 characters, ignore trailing 0x20/0x00/0xff bytes, url-encoded UTF-8 name-value pairs)
    uint8_t  reserved[512];                     ///< @512 +512 (Reserved for device-specific meta-data in the same format as the user meta-data) (512 bytes)
} cwa_header_t;    
    */
    
    
    public void replaceDeviceInFile(Path source, int newId, Path dest) throws IOException {
        replaceDeviceInFile(source, newId, dest, false);
    }

    public void replaceDeviceInFile(Path source, int newId, Path dest, boolean overwrite) throws IOException {
        
        if (Files.exists(dest) && !overwrite) {
            throw new IllegalArgumentException("Destination file already exists: "+dest);
        }
        
        try (InputStream input = Files.newInputStream(source)) {
            
            byte[] frame = readFrame(input);
            byte[] fixed = replaceDeviceInHeader(frame, newId);
            
            try (OutputStream output = Files.newOutputStream(dest)) {
                
                output.write(fixed);
                output.flush();
                
                input.transferTo(output);
            }
        }
    }    
    
    protected byte[] replaceDeviceInHeader(byte[] headerFrame, int newId) {
        
        checkHeader(headerFrame, "MD");
        
        byte[] fixed = Arrays.copyOf(headerFrame, headerFrame.length);
        
        writeU16Int(newId, fixed, DEVICE_ID_POS);
        return fixed;
    }

    public int getDeviceId(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            return getDeviceId(input);
        }
    }
    
    protected int getDeviceId(InputStream input) throws IOException {
    
        byte[] buffer = new byte[1024];        
        int size = input.read(buffer);
        if (size < 1024) throw new IOException("Could not read full 1024 bytes of header but only "+size);
        
        return getDeviceId(buffer);
    }

    protected int getDeviceId(byte[] buffer) {
        checkHeader(buffer, "MD");
        return readU16Int(buffer, DEVICE_ID_POS);
    }    
    
    protected byte[] readFrame(InputStream input) throws IOException {
        byte[] buffer = new byte[1024];        
        int size = input.read(buffer);
        return Arrays.copyOf(buffer, size);
    }
        
    protected int readU16Int(byte[] data, int position) {
        // cause of little endian
        int left = Byte.toUnsignedInt(data[position+1]) << 8;
        int right = Byte.toUnsignedInt(data[position]);
        return left | right;
    }
    
    protected void writeU16Int(int value, byte[] data, int position) {
        //little endian, byt byte buffer is big???
        ByteBuffer tmp = ByteBuffer.allocate(4).putInt(value);
        data[position] = tmp.get(3);
        data[position+1] = tmp.get(2);
    }
        
    protected void checkHeader(byte[] data, String exp) {
        char[] head = new char[exp.length()];
        for (int i = 0;i< head.length; i++) {
            head[i] = (char)data[i];
        }
        String header = String.valueOf(head);
        if (!header.equals(exp)) {
            throw new IllegalArgumentException("Wrong data header, expected "+exp+" got "+header);
        }
    }
    

}

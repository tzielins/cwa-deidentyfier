/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package ed.biordm.actigraphy.deidentyfier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author tzielins
 */
public class CWADeidentyfierTest {
    
    public CWADeidentyfierTest() {
    }
    
    CWADeidentyfier instance;
    Path testFile;
    
    @BeforeEach
    public void setUp() {
        instance = new CWADeidentyfier();
        testFile = testFile("ax3_testfile.cwa");
    }

    Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (Exception e) {
            throw new RuntimeException("Cannot get test file "+name+"; "+e.getMessage(),e);
        }
    }
    
    @Test
    public void testSetup() {
        
        assertTrue(Files.isRegularFile(testFile));
    }
    
    @Test 
    public void replacesIdInFile(@TempDir Path tempDir) throws Exception {
        
        assertTrue(Files.isDirectory(tempDir));
        
        Path source = tempDir.resolve(testFile.getFileName());
        Files.copy(testFile, source);
        Path dest = tempDir.resolve("fixed.cwa");
        
        instance.replaceDeviceInFile(source, 5, dest);
        assertTrue(Files.isRegularFile(dest));
        assertEquals(5, instance.getDeviceId(dest));
        
        try {
            instance.replaceDeviceInFile(source, 5, dest);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {}
    }
    
    @Test
    public void testReadId() throws Exception  {
        
        try(InputStream input = Files.newInputStream(testFile)) {
            int id = instance.getDeviceId(input);
            assertEquals(39434, id);
        }
    }
    
    @Test 
    public void testReadU16Int() {
        // 24, 1, 2
        byte[] data = {0b11000, 0b1, 0b10};
        
        assertEquals(1*256+24, instance.readU16Int(data, 0));
        assertEquals(2*256+1, instance.readU16Int(data, 1));
    }
    
    @Test 
    public void testWriteU16Int() {
        // 24, 1, 2
        // {0b11000, 0b1, 0b10};
        
        byte[] data = {0, 0};
        
        //24 + 256
        byte[] exp1 = {0b11000, 0b1};
        
        instance.writeU16Int(1*256+24, data, 0);
        assertArrayEquals(exp1, data);
        
        //1 + 24*256
        byte[] exp2 = {0b1, 0b11000};
        instance.writeU16Int(1+24*256, data, 0);
        assertArrayEquals(exp2, data);
        
        instance.writeU16Int(20950, data, 0);
        assertEquals(20950, instance.readU16Int(data, 0));
    }
    
    @Test
    public void testHeader() throws Exception  {
        try(InputStream input = Files.newInputStream(testFile)) {
            byte[] data = instance.readFrame(input);
            instance.checkHeader(data, "MD");
            try {
                instance.checkHeader(data, "AX");
                fail("Exception expected");
            } catch (IllegalArgumentException e) {}
            
            data = instance.readFrame(input);
            instance.checkHeader(data, "AX");
        }
    }  
    
    @Test
    public void replacesIdInHeader() throws Exception {
        try(InputStream input = Files.newInputStream(testFile)) {
            byte[] data = instance.readFrame(input);
            
            byte[] fixed = instance.replaceDeviceInHeader(data, 13);
            
            assertEquals(13, instance.getDeviceId(fixed));
            
            assertArrayEquals(Arrays.copyOfRange(data, 0, 5), Arrays.copyOfRange(fixed, 0, 5));
            assertArrayEquals(Arrays.copyOfRange(data, 7, data.length), Arrays.copyOfRange(fixed, 7, data.length));
        }
    }
    
    /*@Test
    public void testReadingBytes() throws Exception  {
        
        try(InputStream input = Files.newInputStream(testFile)) {
            byte[] data = instance.readFrame(input);
            
            System.out.println(Integer.toBinaryString(1));
            System.out.println(Integer.toBinaryString(24));
            System.out.println(((data[5])));
            System.out.println((Byte.toUnsignedInt(data[5])));
            System.out.println(Integer.toBinaryString(Byte.toUnsignedInt(data[5]) << 4));
            System.out.println(((data[6])));
            System.out.println((Byte.toUnsignedInt(data[6])));
            System.out.println(Integer.toBinaryString((data[6])));
            System.out.println(Integer.toBinaryString(Byte.toUnsignedInt(data[5])));
            System.out.println(Integer.toBinaryString(Byte.toUnsignedInt(data[6])));
            System.out.println(Integer.toBinaryString((Byte.toUnsignedInt(data[5]) << 8) | Byte.toUnsignedInt(data[6])));
            System.out.println(((Byte.toUnsignedInt(data[6]) << 8) | Byte.toUnsignedInt(data[5])));
            
            
            assertFalse(true);
        }
    }*/
    
}

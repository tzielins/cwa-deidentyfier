/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package ed.biordm.actigraphy.deidentyfier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 *
 * @author tzielins
 */
public class FilesHandlerTest {
    
    public FilesHandlerTest() {
    }
    
    FilesHandler handler;
    CWADeidentyfier deidentyfier;
    
    @BeforeEach
    public void setUp() {
        deidentyfier = Mockito.mock(CWADeidentyfier.class);
        handler = new FilesHandler(deidentyfier);
        
    }
    
    @Test
    public void handleHandlesOne(@TempDir Path tempDir) throws Exception {

        CommandOptions options = new CommandOptions();
        options.source = tempDir.resolve("test.cwa");
        options.destination = tempDir.resolve("out");
        options.suffix = "_deid";
        
        Files.createFile(options.source);
        handler.handle(options);
        assertTrue(Files.isDirectory(options.destination));
        Mockito.verify(deidentyfier).replaceDeviceInFile(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.any());
    } 
    
    @Test
    public void handleHandlesMultiples(@TempDir Path tempDir) throws Exception {

        CommandOptions options = new CommandOptions();
        options.source = tempDir;
        options.destination = tempDir.resolve("out");
        options.suffix = "_deid";
        
        Files.createFile(tempDir.resolve("test.txt"));
        Files.createFile(tempDir.resolve("test.cwa"));
        Files.createFile(tempDir.resolve("test2.cwa"));
        
        handler.handle(options);
        assertTrue(Files.isDirectory(options.destination));
        Mockito.verify(deidentyfier,Mockito.times(2)).replaceDeviceInFile(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.any());
    }     

    @Test
    public void testHasSupportedExtension() {
        Path file = Paths.get("test.txt");
        assertFalse(handler.hasSupportedExtension(file));
        
        file = Paths.get("test.cwa");
        assertTrue(handler.hasSupportedExtension(file));
    }

    @Test
    public void deidentifyFileCallsDeidentyfier(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("test.cwa");
        Path destination = tempDir.resolve("out");
        String suffix = "";
        int id = 2;
        
        Path out = destination.resolve("test.cwa");
        //when(deidentyfier.replaceDeviceInFile(source, id, destination)).
        handler.deidentifyFile(source, destination, suffix, id);
        Mockito.verify(deidentyfier).replaceDeviceInFile(source, id, out);
    }


    @Test
    public void makesNewName() {
        String name = "";
        String suffix = "_t";
        assertEquals("_t", handler.newName(name, suffix));
        
        name = "file";
        suffix = "_t";
        assertEquals("file_t", handler.newName(name, suffix));
        
        name = "file.cos.cwa";
        suffix = "_t";
        assertEquals("file.cos_t.cwa", handler.newName(name, suffix));
    }

    @Test
    public void resolvesNewOut(@TempDir Path tempDir) {
        Path source = Paths.get("./test.txt");
        Path destination = tempDir;
        String suffix = "_deid";
        
        Path expResult = tempDir.resolve("test_deid.txt");
        Path result = handler.getNewOut(source, destination, suffix);
        assertEquals(expResult, result);
    }
    
}

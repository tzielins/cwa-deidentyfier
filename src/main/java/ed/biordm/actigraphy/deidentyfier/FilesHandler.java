/*

 */
package ed.biordm.actigraphy.deidentyfier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author tzielins
 */
@Service
public class FilesHandler {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    final CWADeidentyfier deidentyfier;
    
    public FilesHandler() {
        this(new CWADeidentyfier());
    }
    
    protected FilesHandler(CWADeidentyfier deidentyfier) {
        this.deidentyfier =  deidentyfier;
    }
    
    protected void handle(CommandOptions options) throws IOException {
        
        
        if (!Files.exists(options.destination)) {
            Files.createDirectories(options.destination);
        }
        
        logger.info("\nTarget directory: {}",options.destination.toAbsolutePath());
        
        if (Files.isRegularFile(options.source)) {
            try {
                deidentifyFile(options.source, options.destination, options.suffix, 1);
            } catch (Exception e) {
                handleException(e, options.source);
            };
        } else {
            
            AtomicInteger count = new AtomicInteger();
            AtomicInteger ids = new AtomicInteger();
            try (Stream<Path> files  = Files.list(options.source)) {
                
                files.parallel()
                     .filter(f -> Files.isRegularFile(f))
                     .filter(f -> hasSupportedExtension(f))
                     .forEach( f -> {
                        try {
                            deidentifyFile(f, options.destination, options.suffix, ids.incrementAndGet());
                            count.incrementAndGet();
                        } catch (Exception e) {
                            handleException(e, f);
                        };
                     });
                
                if (count.get() == 0) {
                    logger.warn("No file was processed. The only supported extensions are: .cwa");
                }    
            }
        }
    }
    
    protected boolean hasSupportedExtension(Path file) {
        String fName = file.getFileName().toString();
        return fName.endsWith(".cwa") || fName.endsWith(".CWA");
    }

    protected void deidentifyFile(Path source, Path destination, String suffix, int id) throws IOException {
        
        Path out = getNewOut(source,destination, suffix);
        deidentyfier.replaceDeviceInFile(source, id, out);
                
        logger.info("Converted "+source.getFileName());
    }

    protected void handleException(Exception e, Path source) {
        logger.error("Could not process: "+source+"; "+e.getMessage(),e);
    }

    protected String newName(String name, String suffix) {
        String ext = "";
        int ix = name.lastIndexOf(".");
        if (ix >= 0) {
            ext = name.substring(ix);
            name = name.substring(0, ix);
        }
        
        return name+suffix+ext;
        
    }
    protected Path getNewOut(Path source, Path destination, String suffix) {
        String name = newName(source.getFileName().toString(), suffix);
        return destination.resolve(name);
    }
    
}

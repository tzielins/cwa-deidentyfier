/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ed.biordm.actigraphy.deidentyfier;

import java.awt.SystemColor;
import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author tzielins
 */
@Component
@Profile("!test")
public class CommandRunner implements ApplicationRunner {
    
    final FilesHandler handler;
    
    @Autowired
    public CommandRunner(FilesHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void run(ApplicationArguments args) throws IOException {

        CommandOptions command = getCommandOptions(args);             
        handler.handle(command);

    }

    protected CommandOptions getCommandOptions(ApplicationArguments args) {
        CommandOptions options = new CommandOptions();
        setPassedOptions(options, args);

        if (options.source == null) {
            options.source = promptSource();
        }
        
        if (!Files.exists(options.source)) throw new IllegalArgumentException("File: "+options.source+" does not exists");
        options.source = options.source.toAbsolutePath();
        
        if (options.destination == null) {
            if (Files.isDirectory(options.source)) {
                options.destination = options.source.resolve("out");
            } else {
                options.destination = options.source.getParent().resolve("out");
            }
        }
        
        if (options.suffix == null) {
            options.suffix = "_noid";
        }
        
        return options;
        
    }
    
    protected void setPassedOptions(CommandOptions options, ApplicationArguments args) {

        System.out.println(args.getOptionNames());
        System.out.println(args.getNonOptionArgs());
        
        if (!args.getNonOptionArgs().isEmpty()) {
            String source = args.getNonOptionArgs().get(0);
            if (!source.startsWith("-"))
                options.source = Paths.get(source);
        }
        
        
        if (args.getOptionNames().contains("source") && !args.getOptionValues("source").isEmpty()) {
            options.source = Paths.get(args.getOptionValues("source").get(0));
        }
        if (args.getOptionNames().contains("s") && !args.getOptionValues("s").isEmpty()) {
            options.source = Paths.get(args.getOptionValues("s").get(0));
        }
        
        if (args.getOptionNames().contains("dest") && !args.getOptionValues("dest").isEmpty()) {
            System.out.println("DEST");
            options.destination = Paths.get(args.getOptionValues("dest").get(0));
        }
        if (args.getOptionNames().contains("d") && !args.getOptionValues("d").isEmpty()) {
            System.out.println("D");
            options.destination = Paths.get(args.getOptionValues("d").get(0));
        }
        
        if (args.getOptionNames().contains("suf") && !args.getOptionValues("suf").isEmpty()) {
            options.suffix = args.getOptionValues("suf").get(0);
        }
        
    }

    protected Path promptSource() {
        
        Console console = System.console();
        
        console.printf("Please enter the path to one file to be de-identified or%n");
        console.printf("to directory with files to be de-identified%n");
        String source = console.readLine("Source path [<ENTER> for current directory]: "); 
        
        if (source.isBlank()) source = ".";
        
        return Paths.get(source);
    }
}

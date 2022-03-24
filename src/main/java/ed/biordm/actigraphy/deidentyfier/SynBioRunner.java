/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SynBioRunner implements ApplicationRunner{

    final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    public SynBioRunner() {
    }

    @Override
    public void run(ApplicationArguments args) {

        try {
            
            System.out.println("Finished");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage(),e);
        } 
    }

}

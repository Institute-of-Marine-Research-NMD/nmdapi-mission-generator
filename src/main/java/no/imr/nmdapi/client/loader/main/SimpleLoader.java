package no.imr.nmdapi.client.loader.main;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import no.imr.nmdapi.client.loader.config.ApplicationConfig;
import no.imr.nmdapi.client.loader.dao.Mission;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class SimpleLoader {
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SimpleLoader.class);
    
    public static void main(String[] argv){
        
        
     AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    ConfigurableEnvironment env = new StandardEnvironment();
         try {
             env.getPropertySources().addFirst(new ResourcePropertySource("file:sea2dataLoader.properties"));
         } catch (IOException ex) {
             LOGGER.error("Load properties",ex);
         }
     applicationContext.register(ApplicationConfig.class);
     applicationContext.setEnvironment(env);
     applicationContext.refresh();
     
     
     Mission missionDAO = applicationContext.getBean(Mission.class);
     MissionXMLWriter missionWriter  = applicationContext.getBean(MissionXMLWriter.class);
     
     
     //Count total mission in case we want to log progress during proccessing
     float totalCount = missionDAO.countAll();
     missionWriter.setTotalCount(totalCount);
        try {
            missionWriter.init(true);
        } catch (JAXBException ex) {
           LOGGER.error("Init mission mapper", ex);
        }
     
     missionDAO.proccessMissions(missionWriter);
      LOGGER.info("Finished writing mission data");

    }
   
  
    
}

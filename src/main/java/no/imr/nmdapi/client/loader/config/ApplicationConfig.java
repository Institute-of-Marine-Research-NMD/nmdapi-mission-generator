package no.imr.nmdapi.client.loader.config;

import java.io.File;
import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.Datatypes;
import no.imr.nmdapi.client.loader.dao.Mission;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Terry Hannant <a5119>
 */
@Configuration
//@PropertySource("{propertySource:db.properties}")
public class ApplicationConfig {
    
    @Autowired
     Environment env;
      
 
     @Bean
     public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();

         dataSource.setDriverClassName(env.getProperty("jdbc.driver"));
         dataSource.setUrl(env.getProperty("jdbc.url"));
         dataSource.setUsername(env.getProperty("jdbc.user"));
         dataSource.setPassword(env.getProperty("jdbc.password"));
         dataSource.setPassword(env.getProperty("jdbc.password"));
         
         return dataSource;
     }

     @Bean
     public String basePath(){
       return env.getProperty("output.path");         
     }

     @Bean
     public String baseErrorPath(){
       return env.getProperty("error.path");         
     }

     
     @Bean
     public Mission mission(){
       return new Mission();         
     }

     @Bean
     public MissionXMLWriter missionXMLWriter(){
       return new MissionXMLWriter();         
     }
    
     @Bean
     public Cruise cruise(){
       return new Cruise();         
     }

     @Bean
     public PlatformCodes platormCodes(){
       return new PlatformCodes();         
     }

     @Bean
     public Platform platorm(){
       return new Platform();         
     }

     
     @Bean
     public Datatypes datatypes(){
       return new Datatypes();         
     }


     
}

package no.imr.nmdapi.client.loader.config;

import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.CruiseStatusDAO;
import no.imr.nmdapi.client.loader.dao.Datatypes;
import no.imr.nmdapi.client.loader.dao.Mission;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author sjurl
 */
@Configuration
public class PersistenceConfig {

    @Autowired
    @Qualifier("persistanceConfig")
    private PropertiesConfiguration configuration;

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(configuration.getString("jdbc.driver"));
        dataSource.setUrl(configuration.getString("jdbc.url"));
        dataSource.setUsername(configuration.getString("jdbc.user"));
        dataSource.setPassword(configuration.getString("jdbc.password"));
        dataSource.setPassword(configuration.getString("jdbc.password"));

        return dataSource;
    }

    @Bean
    public Mission mission() {
        return new Mission();
    }

    @Bean
    public MissionXMLWriter missionXMLWriter() {
        return new MissionXMLWriter();
    }

    @Bean
    public Cruise cruise() {
        return new Cruise();
    }

    @Bean
    public PlatformCodes platormCodes() {
        return new PlatformCodes();
    }

    @Bean
    public Platform platorm() {
        return new Platform();
    }

    @Bean
    public Datatypes datatypes() {
        return new Datatypes();
    }
    
    @Bean
    public CruiseStatusDAO cruisestatus(){
        return new CruiseStatusDAO();
    }
}

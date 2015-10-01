package no.imr.nmdapi.client.loader.config;

import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.dao.CruiseInformationDAO;
import no.imr.nmdapi.client.loader.dao.CruiseStatusDAO;
import no.imr.nmdapi.client.loader.dao.DatatypesDAO;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import no.imr.nmdapi.client.loader.dao.PlatformDAO;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.SeaAreaDAO;
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
    public CruiseDAO mission() {
        return new CruiseDAO();
    }

    @Bean
    public CruiseInformationDAO cruise() {
        return new CruiseInformationDAO();
    }

    @Bean
    public PlatformCodesDAO platormCodes() {
        return new PlatformCodesDAO();
    }

    @Bean
    public PlatformDAO platorm() {
        return new PlatformDAO();
    }

    @Bean
    public DatatypesDAO datatypes() {
        return new DatatypesDAO();
    }

    @Bean
    public CruiseStatusDAO cruisestatus() {
        return new CruiseStatusDAO();
    }

    @Bean
    public SeaAreaDAO seaareadao() {
        return new SeaAreaDAO();
    }
}

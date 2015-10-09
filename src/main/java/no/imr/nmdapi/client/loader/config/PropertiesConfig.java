package no.imr.nmdapi.client.loader.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import no.imr.nmdapi.client.loader.routes.InitRoute;
import no.imr.nmdapi.client.loader.routes.RunRoute;
import no.imr.nmdapi.dao.file.NMDDatasetDao;
import no.imr.nmdapi.dao.file.NMDDatasetDaoImpl;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author sjurl
 */
@Configuration
public class PropertiesConfig {

    private static final String CATALINA_BASE = "catalina.base";
    private static final String CRUISE_JAXB_PATH = "no.imr.nmd.commons.cruise.jaxb";
    @Autowired
    @Qualifier("configuration")
    private PropertiesConfiguration configuration;

    /**
     * Persistance configuration
     *
     * @return
     * @throws ConfigurationException
     */
    @Bean(name = "persistanceConfig")
    public PropertiesConfiguration persistanceConfig() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty(CATALINA_BASE) + "/conf/" + configuration.getString("file.configuration.persistance"));
        conf.setReloadingStrategy(new FileChangedReloadingStrategy());
        return conf;
    }

    /**
     * Active mq configuration
     *
     * @return
     * @throws ConfigurationException
     */
    @Bean(name = "activeMQConf")
    public PropertiesConfiguration getActiveMQConfiguration() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty(CATALINA_BASE) + "/conf/" + configuration.getString("file.configuration.activemq"));
        conf.setReloadingStrategy(new FileChangedReloadingStrategy());
        return conf;
    }

    @Bean
    public NMDDatasetDao getNMDDatasetDao() {
        return new NMDDatasetDaoImpl();
    }

    /**
     * Init route
     *
     * @return
     */
    @Bean
    public InitRoute initRoute() {
        return new InitRoute();
    }

    /**
     * Run Route
     *
     * @return
     */
    @Bean
    public RunRoute runRoute() {
        return new RunRoute();
    }

    @Bean
    public Marshaller cruiseMarshaller() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(CRUISE_JAXB_PATH);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return marshaller;
    }
}

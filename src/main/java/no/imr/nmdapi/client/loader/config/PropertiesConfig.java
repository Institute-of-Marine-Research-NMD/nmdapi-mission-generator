package no.imr.nmdapi.client.loader.config;

import no.imr.nmdapi.client.loader.routes.InitRoute;
import no.imr.nmdapi.client.loader.routes.RunRoute;
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

    @Autowired
    @Qualifier("cruiseloaderConfig")
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
}

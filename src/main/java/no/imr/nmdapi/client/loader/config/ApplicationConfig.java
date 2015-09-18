package no.imr.nmdapi.client.loader.config;

import no.imr.nmdapi.client.loader.routes.InitRoute;
import no.imr.nmdapi.client.loader.routes.RunRoute;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.reloading.ReloadingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Terry Hannant
 */
@Configuration
public class ApplicationConfig {

    private static final String CATALINA_BASE = "catalina.base";

    /**
     * Persistance configuration
     *
     * @return
     * @throws ConfigurationException
     */
    @Bean(name = "persistanceConfig")
    public PropertiesConfiguration persistanceConfig() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty(CATALINA_BASE) + "/conf/s2d_connection.properties");
        conf.setReloadingStrategy(new FileChangedReloadingStrategy());
        return conf;
    }

    /**
     * Configuration object for communicating with property data.
     *
     * @return Configuration object containg properties.
     * @throws ConfigurationException Error during instansiation.
     */
    @Bean(name = "cruiseloaderConfig")
    public PropertiesConfiguration configuration() throws ConfigurationException {
        PropertiesConfiguration configuration = new PropertiesConfiguration(System.getProperty(CATALINA_BASE) + "/conf/nmd_cruise_loader.properties");
        ReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
        configuration.setReloadingStrategy(reloadingStrategy);
        return configuration;
    }

    /**
     * Active mq configuration
     *
     * @return
     * @throws ConfigurationException
     */
    @Bean(name = "activeMQConf")
    public PropertiesConfiguration getActiveMQConfiguration() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty(CATALINA_BASE) + "/conf/activemq.properties");
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

    @Bean
    public RunRoute runRoute() {
        return new RunRoute();
    }
}

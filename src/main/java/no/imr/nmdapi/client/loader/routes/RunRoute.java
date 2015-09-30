package no.imr.nmdapi.client.loader.routes;

import no.imr.nmdapi.client.loader.processor.ExceptionProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Listens to export-nmdmission and sends any messages onwards to missionLoader
 *
 * @author sjurl
 */
public class RunRoute extends RouteBuilder {

    @Autowired
    @Qualifier("cruiseloaderConfig")
    private PropertiesConfiguration configuration;

    @Override
    public void configure() throws Exception {
        onException(Exception.class).continued(true).process(new ExceptionProcessor(configuration.getString("application.name"))).to("jms:queue:error");
        from("jms:queue:export-nmdmission").to("missionLoader");
    }

}
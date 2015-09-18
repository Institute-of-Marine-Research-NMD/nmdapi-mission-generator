package no.imr.nmdapi.client.loader.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author sjurl
 */
public class InitRoute extends RouteBuilder {
//
//    @Autowired
//    @Qualifier("referenceConfig")
//    private PropertiesConfiguration configuration;

    @Override
    public void configure() throws Exception {
//        onException(Exception.class).continued(true).process(new ExceptionProcessor(configuration.getString("application.name"))).to("jms:queue:error");
        from("timer://runOnce?repeatCount=1&delay=5000").to("exportallCruises");
    }

}

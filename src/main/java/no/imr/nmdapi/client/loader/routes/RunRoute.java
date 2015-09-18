package no.imr.nmdapi.client.loader.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author sjurl
 */
public class RunRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jms:queue:export-nmdmission").to("missionLoader");
    }

}

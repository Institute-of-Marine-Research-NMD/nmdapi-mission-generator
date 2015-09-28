package no.imr.nmdapi.client.loader.processor;

import java.io.StringWriter;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import no.imr.messaging.errorresponse.domain.v1.ErrorResponseType;
import no.imr.messaging.errorresponse.domain.v1.generation.ErrorResponseGenerator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author sjurl
 */
public class ExceptionProcessor implements Processor {

    private final String name;

    /**
     * Constructor that takes the name of the application
     *
     * @param name
     */
    public ExceptionProcessor(String name) {
        this.name = name;
    }

    /**
     * Generates an exception message that can be sent to the error queue
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        Throwable cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        ErrorResponseType response = ErrorResponseGenerator.getErrorResponse(name, exchange.getIn().getBody(String.class), new Date(), cause);
        Marshaller marshaller = JAXBContext.newInstance("no.imr.messaging.errorresponse.domain.v1").createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        exchange.getOut().setBody(sw.toString());
    }

}

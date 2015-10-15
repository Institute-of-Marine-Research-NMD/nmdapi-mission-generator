package no.imr.nmdapi.client.loader.service;

import java.util.List;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("singleCruiseExporterService")
public class SingleCruiseExporterService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SingleCruiseExporterService.class);
    private static final int MISSION_ID_LOCATION = 2;

    @Autowired
    private CruiseDAO missionDAO;

    @Autowired
    private CreateCruise createCruise;

    /**
     * Loads a single cruise from db to xml based on input, third element of
     * input (comma delimited) must be a cruise id
     *
     * @param exchange
     * @return
     */
    public CruiseType loadData(Exchange exchange) {
        String messageBody = exchange.getIn().getBody(String.class);
        LOGGER.info(messageBody.split(",")[MISSION_ID_LOCATION]);
        List<String> cruises = missionDAO.getAllCruiseId();
        if (cruises.contains(messageBody.split(",")[MISSION_ID_LOCATION])) {
            return createCruise.createCruise(messageBody.split(",")[MISSION_ID_LOCATION], missionDAO);
        } else {
            return null;
        }
    }
}

package no.imr.nmdapi.client.loader.service;

import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("missionLoader")
public class MissionLoader extends Exporter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MissionLoader.class);
    private static final int MISSION_ID_LOCATION = 2;

    @Autowired
    private CruiseDAO missionDAO;

    /**
     * Loads a single cruise from db to xml based on input, third element of
     * input (comma delimited) must be a cruise id
     *
     * @param excahange
     */
    @Override
    public void loadData(Exchange excahange) {
        String messageBody = excahange.getIn().getBody(String.class);
        LOGGER.info(messageBody.split(",")[MISSION_ID_LOCATION]);
        exportSingleCruise(messageBody.split(",")[MISSION_ID_LOCATION], missionDAO);
    }
}

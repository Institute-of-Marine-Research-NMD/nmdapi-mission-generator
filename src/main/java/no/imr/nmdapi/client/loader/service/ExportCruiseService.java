package no.imr.nmdapi.client.loader.service;

import java.util.List;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("exportallCruises")
public class ExportCruiseService extends Exporter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExportCruiseService.class);
    @Autowired
    private CruiseDAO missionDAO;

    /**
     * Exports all cruises from nmdmission.mission
     *
     * @param exchange
     */
    @Override
    public void loadData(Exchange exchange) {
        List<String> cruises = missionDAO.getAllCruiseId();
        LOGGER.info("Export size: " + cruises.size());
        for (String cruise : cruises) {
            LOGGER.info("Exporting: " + cruise);
            exportSingleCruise(cruise, missionDAO);
        }
    }
}

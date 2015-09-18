package no.imr.nmdapi.client.loader.service;

import javax.xml.bind.JAXBException;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import no.imr.nmdapi.client.loader.dao.Mission;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("exportallCruises")
public class ExportCruiseService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExportCruiseService.class);
    @Autowired
    private Mission missionDAO;

    @Autowired
    private MissionXMLWriter missionWriter;

    public void exportCruise() {
        float totalCount = missionDAO.countAll();
        missionWriter.setTotalCount(totalCount);
        try {
            missionWriter.init(true);
        } catch (JAXBException ex) {
            LOGGER.error("Init mission mapper", ex);
        }

        missionDAO.proccessMissions(missionWriter);
    }
}

package no.imr.nmdapi.client.loader.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.dataset.jaxb.DataTypeEnum;
import no.imr.nmd.commons.dataset.jaxb.DatasetType;
import no.imr.nmd.commons.dataset.jaxb.DatasetsType;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.PlatformDAO;
import no.imr.nmdapi.exceptions.S2DException;
import no.imr.nmdapi.lib.nmdapipathgenerator.PathGenerator;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("exportAllCruiseService")
public class ExportAllCruiseService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExportAllCruiseService.class);
    private static final String DATASET_JAXB_PATH = "no.imr.nmd.commons.dataset.jaxb";

    @Autowired
    private CruiseDAO cruiseDAO;

    @Autowired
    private PlatformInformationService platservice;

    @Autowired
    private PlatformCodesDAO platformcodeDAO;

    @Autowired
    @Qualifier("cruiseloaderConfig")
    private PropertiesConfiguration config;

    @Autowired
    private PlatformDAO platformDAO;

    @Autowired
    private CreateCruise createCruise;

    /**
     * Exports all cruises from nmdmission.mission
     *
     * @return
     */
    public List<CruiseType> loadData() {

        List<String> missions = cruiseDAO.getAllCruiseId();
        PathGenerator pathgen = new PathGenerator();
        List<CruiseType> updatedMissions = new ArrayList<>();
        for (String mission : missions) {
            LOGGER.info("checking mission: " + mission);
            Date lastUpdated = cruiseDAO.getLastUpdated(mission);

            CruiseType cruise = createCruise.createCruise(mission, cruiseDAO);

            Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = platservice.getPlatformcodesForURICode(cruise.getId(), platformcodeDAO);
            File datasetFile = pathgen.generatePath(config.getString("output.path"), cruiseDAO.getMissionTypeDescription(cruise.getCruisetype().intValue()),
                    cruise.getStartyear().toString(), pathgen.createPlatformURICode(platMap), platservice.generateCruiseCode(cruise, platformDAO), null);
            if (datasetFile.exists()) {
                DatasetsType dataset = unmarshallDataset(datasetFile);

                boolean found = false;
                for (DatasetType datasetType : dataset.getDataset()) {
                    if (datasetType.getDataType().equals(DataTypeEnum.CRUISE) && lastUpdated.after(datasetType.getUpdated().toGregorianCalendar().getTime())) {
                        LOGGER.info("found dataset, time on dataset: " + datasetType.getUpdated().toString() + "  time on db: " + lastUpdated.toString());
                        updatedMissions.add(cruise);
                        found = true;
                    } else if (datasetType.getDataType().equals(DataTypeEnum.CRUISE) && !lastUpdated.after(datasetType.getUpdated().toGregorianCalendar().getTime())) {
                        found = true;
                    }
                }

                if (!found) {
                    updatedMissions.add(cruise);
                }
            } else {
                updatedMissions.add(cruise);
            }
        }
        return updatedMissions;
    }

    private DatasetsType unmarshallDataset(File dataseFile) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(DATASET_JAXB_PATH);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(dataseFile);
            if (obj instanceof JAXBElement) {
                return (DatasetsType) ((JAXBElement) obj).getValue();
            } else {
                return (DatasetsType) obj;
            }
        } catch (JAXBException ex) {
            LOGGER.info(null, ex);
            throw new S2DException("Unable to unmarshall dataset file " + dataseFile.getAbsolutePath(), ex);
        }
    }
}

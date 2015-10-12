package no.imr.nmdapi.client.loader.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.dataset.jaxb.DataTypeEnum;
import no.imr.nmd.commons.dataset.jaxb.DatasetType;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.PlatformDAO;
import no.imr.nmdapi.dao.file.NMDDatasetDao;
import no.imr.nmdapi.lib.nmdapipathgenerator.PathGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("exportAllCruiseService")
public class ExportAllCruiseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportAllCruiseService.class);

    @Autowired
    private CruiseDAO cruiseDAO;

    @Autowired
    private PlatformInformationService platservice;

    @Autowired
    private PlatformCodesDAO platformcodeDAO;

    @Autowired
    private PlatformDAO platformDAO;

    @Autowired
    private CreateCruise createCruise;

    @Autowired
    private NMDDatasetDao nmdDatasetDAO;

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
            DatasetType datasetType = nmdDatasetDAO.getDatasetByName(DataTypeEnum.CRUISE, "dataset", cruiseDAO.getMissionTypeDescription(cruise.getCruisetype().intValue()),
                    cruise.getStartyear().toString(), pathgen.createPlatformURICode(platMap), platservice.generateCruiseCode(cruise, platformDAO));
            if (datasetType != null) {
                if (lastUpdated.after(datasetType.getUpdated().toGregorianCalendar().getTime())) {
                    LOGGER.info("found dataset, time on dataset: " + datasetType.getUpdated().toString() + "  time on db: " + lastUpdated.toString());
                    updatedMissions.add(cruise);
                }
            } else {
                updatedMissions.add(cruise);
            }
        }
        return updatedMissions;
    }

}

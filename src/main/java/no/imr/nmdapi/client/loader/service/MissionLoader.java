package no.imr.nmdapi.client.loader.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.DatasetType;
import no.imr.nmd.commons.cruise.jaxb.DatasetsType;
import no.imr.nmd.commons.cruise.jaxb.ExistsEnum;
import no.imr.nmd.commons.cruise.jaxb.PlatformInfoType;
import no.imr.nmd.commons.cruise.jaxb.PlatformType;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.CruiseStatusDAO;
import no.imr.nmdapi.client.loader.dao.Datatypes;
import no.imr.nmdapi.client.loader.dao.Mission;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import no.imr.nmdapi.client.loader.pojo.TypeValue;
import no.imr.nmdapi.lib.nmdapipathgenerator.PathGenerator;
import org.apache.camel.Exchange;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("missionLoader")
public class MissionLoader {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MissionLoader.class);
    @Autowired
    private Mission missionDAO;

    @Autowired
    private Cruise cruiseMissionDAO;

    @Autowired
    private PlatformCodes platformCodeDAO;

    @Autowired
    private Datatypes datatypeDAO;

    @Autowired
    private Platform platformDAO;

    @Autowired
    private CruiseStatusDAO cruisestatus;

    @Autowired
    @Qualifier("cruiseloaderConfig")
    private PropertiesConfiguration config;

    public void loadData(Exchange excahange) {
        String messageBody = excahange.getIn().getBody(String.class);
        LOGGER.info(messageBody);
        LOGGER.info(messageBody.split(",")[2]);
        CruiseType cruise = missionDAO.getCruise(messageBody.split(",")[2]);
//        CruiseType cruise = missionDAO.getCruise("27CD6472951AF3A79A30A563DFEC0912");

        //Cruise info
        CruiseInfo cruiseinfo = null;
        try {
            cruiseinfo = cruiseMissionDAO.getMissionCruise(cruise.getId());
        } catch (EmptyResultDataAccessException erdae) {
            // Can discard exception as empty set is ok to return
        }

        if (cruiseinfo != null) {
            cruise.setArrivalPort(cruiseinfo.getArrivalPort());
            cruise.setDeparturePort(cruiseinfo.getDepartPort());
            cruise.setCruiseCode(cruiseinfo.getCruiseCode());
            cruise.setCruiseLeader(cruiseinfo.getFullName());
            cruise.setBeiCruiseNo(cruiseinfo.getBeicruiseno());
            cruise.setOriginalSurveyNo(cruiseinfo.getOrignalsurveyno());
            //sea areas
            //rented
        }

        cruise.setMissionStatus(cruisestatus.getCruiseStatus(cruise.getId()));
        //Platform info
        Map<String, TypeValue> platformMap = platformCodeDAO.getMissionPlatformCodes(cruise.getId());
        Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = new HashMap<>();
        if (platformMap.size() > 0) {
            PlatformInfoType platformInfo = new PlatformInfoType();
            for (String platform : platformMap.keySet()) {
                PlatformType platType = new PlatformType();
                platType.setType(platformMap.get(platform).getType());
                platType.setValue(platformMap.get(platform).getValue());
                platformInfo.getPlatform().add(platType);
                no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue tv = new no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue();
                tv.setType(platType.getType());
                tv.setValue(platType.getValue());
                platMap.put(tv.getType(), tv);
            }
            cruise.setPlatformInfo(platformInfo);
        }

        //Data types
        DatasetsType types = new DatasetsType();

        boolean hasBiotic = (datatypeDAO.countBiotic(cruise.getId()) > 0);
        boolean hasEchosouder = (datatypeDAO.countEchoSounder(cruise.getId()) > 0);

        types.getDataset().add(getDataType("biotic", "", hasBiotic ? ExistsEnum.YES : ExistsEnum.NO));
        types.getDataset().add(getDataType("echosounder", "", hasEchosouder ? ExistsEnum.YES : ExistsEnum.NO));

        cruise.setDatasets(types);

        PathGenerator pathgen = new PathGenerator();
        File path = pathgen.generatePath(config.getString("output.path"), missionDAO.getMissionTypeDescription(cruise.getMissiontype().intValue()),
                cruise.getStartyear().toString(), pathgen.createPlatformURICode(platMap), getDeliver(cruise), "cruise");
        writeMission(path, cruise);
    }

    private DatasetType getDataType(String type, String desc, ExistsEnum ex) {
        DatasetType datatypeElementType = new DatasetType();
        datatypeElementType.setDataType(type);
        datatypeElementType.setCollected(ex);
        return datatypeElementType;
    }

    private String getDeliver(CruiseType cruise) {
        if (cruise.getCruiseCode() == null || cruise.getCruiseCode().trim().length() == 0) {
            return cruise.getMissiontype().toString() + "-"
                    + cruise.getStartyear() + "-"
                    + platformDAO.getMissionPlatform(cruise.getId()) + "-"
                    + cruise.getMissionNumber().toString();
        }
        return cruise.getCruiseCode();

    }

    private void writeMission(File file, CruiseType mission) {
        try {
            JAXBContext ctx = JAXBContext.newInstance("no.imr.nmd.commons.cruise.jaxb");
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.marshal(mission, file);
        } catch (JAXBException ex) {
            Logger.getLogger(MissionXMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

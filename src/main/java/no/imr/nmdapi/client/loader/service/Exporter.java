package no.imr.nmdapi.client.loader.service;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.DatasetType;
import no.imr.nmd.commons.cruise.jaxb.DatasetsType;
import no.imr.nmd.commons.cruise.jaxb.ExistsEnum;
import no.imr.nmd.commons.cruise.jaxb.PlatformInfoType;
import no.imr.nmd.commons.cruise.jaxb.PlatformType;
import no.imr.nmd.commons.cruise.jaxb.DataTypeEnum;
import no.imr.nmd.commons.cruise.jaxb.PersonCruiseType;
import no.imr.nmd.commons.cruise.jaxb.PersonsType;
import no.imr.nmd.commons.cruise.jaxb.PlatformTypeEnum;
import no.imr.nmd.commons.cruise.jaxb.RoleEnum;
import no.imr.nmd.commons.cruise.jaxb.RolesType;
import no.imr.nmd.commons.cruise.jaxb.SeaAreaType;
import no.imr.nmd.commons.cruise.jaxb.SeaAreasType;
import no.imr.nmd.commons.dataset.jaxb.QualityEnum;
import no.imr.nmd.commons.dataset.jaxb.RestrictionsType;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.CruiseStatusDAO;
import no.imr.nmdapi.client.loader.dao.DatatypesDAO;
import no.imr.nmdapi.client.loader.dao.Mission;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import no.imr.nmdapi.client.loader.dao.SeaAreaDAO;
import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import no.imr.nmdapi.client.loader.pojo.TypeValue;
import no.imr.nmdapi.lib.nmdapipathgenerator.PathGenerator;
import org.apache.camel.Exchange;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * Abstract utility class for exporting missions to file
 *
 * @author sjurl
 */
public abstract class Exporter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Exporter.class);

    private static final String DATASET_JAXB_PATH = "no.imr.nmd.commons.dataset.jaxb";
    private static final String CRUISE_JAXB_PATH = "no.imr.nmd.commons.cruise.jaxb";

    @Autowired
    private Cruise cruiseMissionDAO;

    @Autowired
    private PlatformCodes platformCodeDAO;

    @Autowired
    private DatatypesDAO datatypeDAO;

    @Autowired
    private Platform platformDAO;

    @Autowired
    private CruiseStatusDAO cruisestatus;

    @Autowired
    private SeaAreaDAO seaAreaDao;

    @Autowired
    @Qualifier("cruiseloaderConfig")
    private PropertiesConfiguration config;

    /**
     * Export cruises
     *
     * @param exchange
     */
    public abstract void loadData(Exchange exchange);

    /**
     * Exports a single cruise
     *
     * @param cruiseID
     * @param cruiseDAO
     */
    protected void exportSingleCruise(String cruiseID, Mission cruiseDAO) {
        CruiseType cruise = cruiseDAO.getCruise(cruiseID);
        PersonsType pt = new PersonsType();
        pt.getPerson().add(cruiseDAO.getCoordinator(cruiseID));

        if (cruise.getCruisetype().intValue() == 4) {
            cruise.setRented(Boolean.FALSE);
        } else if (cruise.getCruisetype().intValue() == 5) {
            cruise.setRented(Boolean.TRUE);
        }

        addCruiseInformation(cruise, pt);

        cruise.setPersons(pt);

        List<SeaAreaType> seaareas = seaAreaDao.getSeaareas(cruiseID);
        SeaAreasType seaareasType = new SeaAreasType();
        seaareasType.getSeaArea().addAll(seaareas);
        cruise.setSeaAreas(seaareasType);

        cruise.setCruiseStatus(cruisestatus.getCruiseStatus(cruise.getId()));

        //Platform info
        Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = addPlatformInformation(cruise);

        //Data types
        DatasetsType types = new DatasetsType();
        for (DatatypesDAO.Datatypes datatype : DatatypesDAO.Datatypes.values()) {
            types.getDataset().add(getDataType(datatype.getDTEnum(), datatypeDAO.hasDatatype(cruiseID, datatype.getName())));
        }
        cruise.setDatasets(types);

        PathGenerator pathgen = new PathGenerator();
        File path = pathgen.generatePath(config.getString("output.path"), cruiseDAO.getMissionTypeDescription(cruise.getCruisetype().intValue()),
                cruise.getStartyear().toString(), pathgen.createPlatformURICode(platMap), generateCruiseCode(cruise, platformDAO), "cruise");
        export(path, cruise, pathgen.createPlatformURICode(platMap));
    }

    private void addCruiseInformation(CruiseType cruise, PersonsType pt) {
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
            PersonCruiseType person = new PersonCruiseType();
            person.setFirstname(cruiseinfo.getFirstName());
            person.setLastname(cruiseinfo.getLastName());
            RolesType role = new RolesType();
            role.getRole().add(RoleEnum.CRUISELEADER);
            person.setRoles(role);
            pt.getPerson().add(person);
            cruise.setBeiCruiseNo(cruiseinfo.getBeicruiseno() == 0 ? null : cruiseinfo.getBeicruiseno());
            cruise.setOriginalSurveyNo(cruiseinfo.getOrignalsurveyno());
        }
    }

    private Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> addPlatformInformation(CruiseType cruise) {
        Map<String, TypeValue> platformMap = platformCodeDAO.getMissionPlatformCodes(cruise.getId());
        Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = new HashMap<>();
        if (!platformMap.isEmpty()) {
            PlatformInfoType platformInfo = new PlatformInfoType();
            for (String platform : platformMap.keySet()) {
                PlatformType platType = new PlatformType();
                if (null != platformMap.get(platform).getValue()) {
                    switch (platformMap.get(platform).getType()) {
                        case "Ship Name":
                            platType.setType(PlatformTypeEnum.SHIP_NAME);
                            break;
                        case "ITU Call Sign":
                            platType.setType(PlatformTypeEnum.ITU_CALL_SIGN);
                            break;
                        case "ICES Ship Code":
                            platType.setType(PlatformTypeEnum.ICES_SHIP_CODE);
                            break;
                        case "IOC/NODC Ship Code":
                            platType.setType(PlatformTypeEnum.IOC_NODC_SHIP_CODE);
                            break;
                        case "WMO Buoy Identifier":
                            platType.setType(PlatformTypeEnum.WMO_BUOY_IDENTIFIER);
                            break;
                        case "National/local Identifier":
                            platType.setType(PlatformTypeEnum.NATIONAL_LOCAL_IDENTIFIER);
                            break;
                        case "Norwegian Fisheries Register":
                            platType.setType(PlatformTypeEnum.NORWEGIAN_FISHERIES_REGISTER);
                            break;
                        case "ARGO Profiler":
                            platType.setType(PlatformTypeEnum.ARGO_PROFILER);
                            break;
                    }
                }
                platType.setValue(platformMap.get(platform).getValue());
                platformInfo.getPlatform().add(platType);
                no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue tv = new no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue();
                tv.setType(platformMap.get(platform).getType());
                tv.setValue(platType.getValue());
                platMap.put(tv.getType(), tv);
            }
            cruise.setPlatformInfo(platformInfo);
        }
        return platMap;
    }

    /**
     * Generates a cruise code if one isn't present in the cruise
     *
     * @param cruise
     * @param platformDAO
     * @return
     */
    private String generateCruiseCode(CruiseType cruise, Platform platformDAO) {
        if (cruise.getCruiseCode() == null || cruise.getCruiseCode().trim().length() == 0) {
            return cruise.getCruisetype().toString() + "-"
                    + cruise.getStartyear() + "-"
                    + platformDAO.getMissionPlatform(cruise.getId()) + "-"
                    + cruise.getCruiseNumber().toString();
        }
        return cruise.getCruiseCode();

    }

    /**
     * Exports a single file and generates dataset xml file
     *
     * @param path
     * @param dataset
     * @param cruiseName
     */
    private void export(File path, CruiseType dataset, String cruiseName) {
        String code = generateCruiseCode(dataset, platformDAO);
        File newFile = new File(FileUtils.getTempDirectory().getAbsolutePath().concat(File.separator).concat(code));
        File oldFile = new File(path.getAbsolutePath());
        writeCruise(newFile, dataset);
        if (newFile.exists() && oldFile.exists()) {
            try {
                if (FileUtils.checksumCRC32(oldFile) != FileUtils.checksumCRC32(newFile)) {
                    FileUtils.copyFile(newFile, oldFile);
                    updateUpdatedTime(path, cruiseName);
                }
            } catch (IOException ex) {
                LOGGER.error("Error working on table ".concat(dataset.getCruiseCode()), ex);
            }
        } else if (newFile.exists() && !oldFile.exists()) {
            try {
                FileUtils.copyFile(newFile, oldFile);
                updateUpdatedTime(path, cruiseName);
            } catch (IOException ex) {
                LOGGER.error("Unable to write file " + oldFile.getAbsolutePath(), ex);
            }
        }
        newFile.delete();
        LOGGER.info("FINISHED with ".concat(code));
    }

    /**
     * Write the cruise file
     *
     * @param file
     * @param mission
     */
    private void writeCruise(File file, CruiseType mission) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(CRUISE_JAXB_PATH);
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(mission, file);
        } catch (JAXBException ex) {
            LOGGER.info(null, ex);
        }
    }

    private DatasetType getDataType(DataTypeEnum type, ExistsEnum ex) {
        DatasetType datatypeElementType = new DatasetType();
        datatypeElementType.setDataType(type);
        datatypeElementType.setCollected(ex);
        return datatypeElementType;
    }

    /**
     * Generates / updates the dataset xml file
     *
     * @param path
     * @param name
     */
    private void updateUpdatedTime(File path, String name) {
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(Calendar.getInstance().getTime());
            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            File dataseFile = new File(path.getParentFile().getParentFile().getAbsolutePath().concat(File.separator).concat("data.xml"));
            if (dataseFile.exists()) {
                no.imr.nmd.commons.dataset.jaxb.DatasetType dataset = unmarshallDataset(dataseFile);
                dataset.setUpdated(date);
                marshallDatasets(dataseFile, dataset);
            } else {
                no.imr.nmd.commons.dataset.jaxb.DatasetType dataset = new no.imr.nmd.commons.dataset.jaxb.DatasetType();
                dataset.setId("no:imr:cruise:".concat(java.util.UUID.randomUUID().toString()));
                dataset.setDataType(no.imr.nmd.commons.dataset.jaxb.DataTypeEnum.CRUISE);
                dataset.setDatasetName(name);
                dataset.setOwner("imr");
                RestrictionsType restrictionsType = new RestrictionsType();
                restrictionsType.setRead("unrestricted");
                restrictionsType.setWrite("SG-NMDCRUISE-WRITE");
                dataset.setRestrictions(restrictionsType);
                dataset.setQualityAssured(QualityEnum.NONE);
                dataset.setUpdated(date);
                dataset.setCreated(date);
                marshallDatasets(dataseFile, dataset);
            }
        } catch (DatatypeConfigurationException ex) {
            LOGGER.info(null, ex);
        }
    }

    /**
     * Unmarshalls a single dataset
     *
     * @param dataseFile
     * @return
     */
    private no.imr.nmd.commons.dataset.jaxb.DatasetType unmarshallDataset(File dataseFile) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(DATASET_JAXB_PATH);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(dataseFile);
            if (obj instanceof JAXBElement) {
                return (no.imr.nmd.commons.dataset.jaxb.DatasetType) ((JAXBElement) obj).getValue();
            } else {
                return (no.imr.nmd.commons.dataset.jaxb.DatasetType) obj;
            }
        } catch (JAXBException ex) {
            LOGGER.info(null, ex);
        }
        return null;
    }

    /**
     * Marshalls a single dataset
     *
     * @param file
     * @param datasets
     */
    private void marshallDatasets(File file, no.imr.nmd.commons.dataset.jaxb.DatasetType datasets) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(DATASET_JAXB_PATH);
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(datasets, file);
        } catch (JAXBException ex) {
            LOGGER.info(null, ex);
        }
    }

}

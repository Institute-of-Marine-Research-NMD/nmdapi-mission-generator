package no.imr.nmdapi.client.loader.convert;

import java.io.File;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import no.imr.nmd.commons.dataset.jaxb.DatasetType;
import no.imr.nmd.commons.dataset.jaxb.DatasetsType;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.PlatformInfoType;
import no.imr.nmd.commons.cruise.jaxb.PlatformType;

import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.Datatypes;
import no.imr.nmdapi.client.loader.pojo.TypeValue;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.FileSystemUtils;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class DatasetXMLWriter implements RowCallbackHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DatasetXMLWriter.class);

    private static final String NOPLATFORM = "NOPLATFORMCODES";

    @Autowired
    PlatformCodes platformCodeDAO;
    @Autowired
    Cruise cruiseDAO;
    @Autowired
    Datatypes datatypeDAO;
    @Autowired
    Platform platformDAO;
    @Autowired
    String basePath;
    @Autowired
    String baseErrorPath;

    int totalCount;
    private int percentStep;
    XMLTypeConverter xmlTypeConverter;
    private int rowCount;
    private Marshaller cruiseMarshaller;
    private Marshaller datasetsMarshaller;

    public DatasetXMLWriter() {
        xmlTypeConverter = new XMLTypeConverter();
        rowCount = 0;
    }

    public void init(boolean deleteErrors) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance("no.imr.nmd.commons.cruise.jaxb");
        cruiseMarshaller = ctx.createMarshaller();

        ctx = JAXBContext.newInstance("no.imr.nmd.commons.dataset.jaxb");
        datasetsMarshaller = ctx.createMarshaller();

        FileSystemUtils.deleteRecursively(new File(basePath));
        if (deleteErrors) {
            FileSystemUtils.deleteRecursively(new File(baseErrorPath));
        }
    }

    public void setTotalCount(float totalCount) {
        this.totalCount = (int) totalCount;
        this.percentStep = (int) Math.round(5.0 * totalCount / 100.0);
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {

        String missionID = rs.getString("id");
        String missiontType = rs.getString("missiontype");
        String missionTypeCode = rs.getString("missionTypeCode");

        rowCount++;

//        MissionType mission = new MissionType();
        CruiseType cruise = new CruiseType();

        cruise.setMissionNumber(BigInteger.valueOf(rs.getInt("missionnumber")));
        //  cruise.setd.setDatapath(rs.getString("datapath"));
        cruise.setStartyear(BigInteger.valueOf(rs.getInt("startyear")));
        cruise.setStartTime(xmlTypeConverter.convertDate(rs.getDate("start_time")));
        cruise.setStopTime(xmlTypeConverter.convertDate(rs.getDate("stop_time")));

        //Create purpose 
        CruiseType.Purpose purpose = new CruiseType.Purpose();
        purpose.setLang("no");  //TODO How should this be really set? Parse for norsk special chars?
        purpose.setValue(rs.getString("purpose"));
        cruise.getPurpose().add(purpose);
        //Cruise info
        CruiseInfo cruiseInfo = null;
        try {
            cruiseInfo = cruiseDAO.getMissionCruise(missionID);
        } catch (EmptyResultDataAccessException erdae) {
            // Can discard exception as empty set is ok to return
        }

        if (cruiseInfo != null) {
            cruise.setArrivalPort(cruiseInfo.getArrivalPort());
            cruise.setDeparturePort(cruiseInfo.getDepartPort());
            cruise.setCruiseCode(cruiseInfo.getCruiseCode());
            cruise.setCruiseLeader(cruiseInfo.getFullName());
        }

        //Platform info
        Map<String, TypeValue> platformMap = platformCodeDAO.getMissionPlatformCodes(missionID);
        if (platformMap.size() > 0) {
            PlatformInfoType platformInfo = new PlatformInfoType();
            for (String platform : platformMap.keySet()) {
                PlatformType platformType = new PlatformType();
                platformType.setType(platformMap.get(platform).getType());
                platformType.setValue(platformMap.get(platform).getValue());

                platformInfo.getPlatform().add(platformType);
            }
            cruise.setPlatformInfo(platformInfo);
        }
        String platformPath = createPlatformURICode(platformMap);

        boolean hasBiotic = (datatypeDAO.countBiotic(missionID) > 0);
        boolean hasEchosouder = (datatypeDAO.countEchoSounder(missionID) > 0);

        String delivery;
        //Map delivery ident
        if (cruise.getCruiseCode() == null) {

            delivery = missionTypeCode + "-"
                    + cruise.getStartyear() + "-"
                    + platformDAO.getMissionPlatform(missionID) + "-"
                    + cruise.getMissionNumber().toString();

        } else if (cruise.getCruiseCode().trim().length() == 0) {
            delivery = missionTypeCode + "-"
                    + cruise.getStartyear() + "-"
                    + platformDAO.getMissionPlatform(missionID) + "-"
                    + cruise.getMissionNumber().toString();

        } else {
            delivery = cruise.getCruiseCode();
        }

        //Check for missing manadatory data
        if (platformPath.equals("NOPLATFORMCODES")) {
            String altPlatformPath = createPlatformURICode(platformCodeDAO.getMissionPlatformAfterStart(missionID));
            if (altPlatformPath.equals("NOPLATFORMCODES")) {
                writeToProblemFile(cruise, missiontType, platformPath, delivery, missionID, "No Platform codes");
            } else {
                writeToProblemFile(cruise, missiontType, platformPath, delivery, missionID, "Platform code only after start");
            }
        }

        //Write files out
        //First write cruise xml
        DatasetsType datasets = new DatasetsType();

        DatasetType cruiseDatatype = new DatasetType();
        cruiseDatatype.setDataType("CRUISE");
        cruiseDatatype.setQualityAssured(no.imr.nmd.commons.dataset.jaxb.QualityEnum.NONE);
        cruiseDatatype.setId("AN ID");
        cruiseDatatype.setDescription("Nice description for cruise");
        datasets.getDataset().add(cruiseDatatype);

        if (hasBiotic) {
            DatasetType biotic = new DatasetType();
            biotic.setDataType("BIOTIC");
            biotic.setQualityAssured(no.imr.nmd.commons.dataset.jaxb.QualityEnum.NONE);
            biotic.setId("AN ID");
            biotic.setDescription("");
//          biotic.setCreated(xmlTypeConverter.convertDate(new Date()));
            datasets.getDataset().add(biotic);
        }

        if (hasEchosouder) {
            DatasetType biotic = new DatasetType();
            biotic.setDataType("BIOTIC");
            biotic.setQualityAssured(no.imr.nmd.commons.dataset.jaxb.QualityEnum.NONE);
            biotic.setId("AN ID");
            biotic.setDescription("");
//          biotic.setCreated(xmlTypeConverter.convertDate(new Date()));
            datasets.getDataset().add(biotic);
        }
        File datasetFile = mapFilename(missiontType, cruise.getStartyear().toString(), platformPath, delivery, null);
        writeDatasets(datasetFile, datasets);

        File file = mapFilename(missiontType, cruise.getStartyear().toString(), platformPath, delivery, "cruise");
        if (file.exists()) {
            writeToProblemFile(cruise, missiontType, platformPath, delivery, missionID, "Duplicate");
        }
        writeCruise(file, cruise);

        rowCount++;
        if ((rowCount % percentStep) == 0) {
            LOG.info((int) Math.round(rowCount / totalCount * 100) + "%");
        }

        LOG.info("Total missions proccessed ", rowCount);
    }

    private File mapFilename(String missionType, String year, String platformCode, String delivery, String dataType) {

        String path = basePath + File.separator
                + missionType + File.separator
                + year + File.separator
                + platformCode + File.separator
                + delivery;

        if (dataType != null) {
            path = path + File.separator + dataType;
        }
        File directory = new File(path);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(directory, "data.xml");
    }

    private void writeToProblemFile(CruiseType cruise, String missionType, String platformCode, String delivery, String missionID, String problem) {
        File file;

        File fullPath = new File(baseErrorPath + File.separator + problem);

        if (!fullPath.exists()) {
            fullPath.mkdirs();
        }
        file = new File(fullPath, missionType + "_"
                + cruise.getStartyear().toString() + "_"
                + platformCode + "_"
                + delivery + "_"
                + missionID + ".xml");
        writeCruise(file, cruise);
    }

    /**
     *
     * Attempt to create Platform part of URL
     *
     * @param platformMap
     * @return
     */
    private String createPlatformURICode(Map<String, TypeValue> platformMap) {
        String shipName = null;
        String callSign = null;
        String result;

        if (platformMap.containsKey("Ship Name")) {
            shipName = platformMap.get("Ship Name").getValue();
            shipName = shipName.replace('.', ' ').replace(File.separator, " ");
        }
        if (platformMap.containsKey("ITU Call Sign")) {
            callSign = platformMap.get("ITU Call Sign").getValue();
            callSign = callSign.replace('.', ' ').replace(File.separator, " ");
        }
        if (shipName != null) {
            if (callSign != null) {
                result = shipName + "-" + callSign;
            } else {
                result = shipName;
            }
        } else if (callSign != null) {
            result = callSign;
        } else {
            result = NOPLATFORM;
        }

        return result;

    }

    private void writeCruise(File file, CruiseType cruise) {
        try {
            cruiseMarshaller.marshal(cruise, file);
        } catch (JAXBException ex) {
            Logger.getLogger(DatasetXMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeDatasets(File file, DatasetsType datasets) {
        try {
            datasetsMarshaller.marshal(datasets, file);
        } catch (JAXBException ex) {
            Logger.getLogger(DatasetXMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

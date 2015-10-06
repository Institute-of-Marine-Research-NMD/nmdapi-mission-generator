package no.imr.nmdapi.client.loader.service;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.dataset.jaxb.QualityEnum;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.PlatformDAO;
import no.imr.nmdapi.lib.nmdapipathgenerator.PathGenerator;
import org.apache.camel.Exchange;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Abstract utility class for exporting missions to file
 *
 * @author sjurl
 */
@Service("cruiseXMLWriterService")
public class CruiseXMLWriterService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CruiseXMLWriterService.class);

    private static final String CRUISE_JAXB_PATH = "no.imr.nmd.commons.cruise.jaxb";
    private static final String DATASET_CONTAINER_DELIMITER = "/";
    
    @Autowired
    private PlatformInformationService platformInformationService;

    @Autowired
    private PlatformCodesDAO platformcodeDAO;

    @Autowired
    private PlatformDAO platformDAO;

    @Autowired
    private CruiseDAO cruiseDAO;

    @Autowired
    @Qualifier("cruiseloaderConfig")
    private PropertiesConfiguration config;

    /**
     * Exports a single cruise
     *
     * @param exchange
     */
    public void writeCruiseToXMLFile(Exchange exchange) {
        CruiseType cruise = exchange.getIn().getBody(CruiseType.class);
//        //Platform info
        Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = platformInformationService.getPlatformcodesForURICode(cruise.getId(), platformcodeDAO);

        PathGenerator pathgen = new PathGenerator();
        File path = pathgen.generatePath(config.getString("output.path"), cruiseDAO.getMissionTypeDescription(cruise.getCruisetype().intValue()),
                cruise.getStartyear().toString(), pathgen.createPlatformURICode(platMap), platformInformationService.generateCruiseCode(cruise, platformDAO), "cruise");
        export(path, cruise);

        exchange.getOut().setHeader("imr:datatype", no.imr.nmd.commons.dataset.jaxb.DataTypeEnum.CRUISE.toString());
        exchange.getOut().setHeader("imr:datasetname", "data");
        exchange.getOut().setHeader("imr:owner", "imr");
        exchange.getOut().setHeader("imr:read", "unrestricted");
        exchange.getOut().setHeader("imr:write", "SG-NMDCRUISE-WRITE");
        exchange.getOut().setHeader("imr:qualityassured", QualityEnum.NONE.toString());
        exchange.getOut().setHeader("imr:description", "");
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(Calendar.getInstance().getTime());
            exchange.getOut().setHeader("imr:updated", DatatypeFactory.newInstance().newXMLGregorianCalendar(c).toString());
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("Unable to set updated time on result header", ex);
        }
        exchange.getOut().setHeader("imr:datasetscontainer", cruiseDAO.getMissionTypeDescription(cruise.getCruisetype().intValue()).concat(DATASET_CONTAINER_DELIMITER).
                concat(cruise.getStartyear().toString()).concat(DATASET_CONTAINER_DELIMITER).concat(pathgen.createPlatformURICode(platMap)).
                concat(DATASET_CONTAINER_DELIMITER).concat(platformInformationService.generateCruiseCode(cruise, platformDAO)));
    }

    /**
     * Exports a single file and generates dataset xml file
     *
     * @param path
     * @param dataset
     */
    private void export(File path, CruiseType dataset) {
        String code = platformInformationService.generateCruiseCode(dataset, platformDAO);
        File newFile = new File(FileUtils.getTempDirectory().getAbsolutePath().concat(File.separator).concat(code));
        File oldFile = new File(path.getAbsolutePath());
        writeCruise(newFile, dataset);
        if (newFile.exists() && oldFile.exists()) {
            try {
                if (FileUtils.checksumCRC32(oldFile) != FileUtils.checksumCRC32(newFile)) {
                    FileUtils.copyFile(newFile, oldFile);
                }
            } catch (IOException ex) {
                LOGGER.error("Error working on table ".concat(dataset.getCruiseCode()), ex);
            }
        } else if (newFile.exists() && !oldFile.exists()) {
            try {
                FileUtils.copyFile(newFile, oldFile);
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

}

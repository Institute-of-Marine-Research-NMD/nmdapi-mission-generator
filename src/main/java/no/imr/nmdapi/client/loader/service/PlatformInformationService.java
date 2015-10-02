package no.imr.nmdapi.client.loader.service;

import java.util.HashMap;
import java.util.Map;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.PlatformInfoType;
import no.imr.nmd.commons.cruise.jaxb.PlatformType;
import no.imr.nmd.commons.cruise.jaxb.PlatformTypeEnum;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.PlatformDAO;
import no.imr.nmdapi.client.loader.pojo.TypeValue;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("platformInformationService")
public class PlatformInformationService {

    /**
     * Create the platform information object for the cruise xmlf ile
     *
     * @param cruiseID
     * @param platformCodeDAO
     * @return
     */
    public PlatformInfoType addPlatformInformation(String cruiseID, PlatformCodesDAO platformCodeDAO) {
        Map<String, TypeValue> platformMap = platformCodeDAO.getMissionPlatformCodes(cruiseID);
        PlatformInfoType platformInfo = new PlatformInfoType();
        if (!platformMap.isEmpty()) {

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
            }
        }
        return platformInfo;
    }

    /**
     * Get a map containing the information needed for generating the uri
     * information for platform that is needed when writing files to the
     * filesystem
     *
     * @param cruiseID
     * @param platformCodeDAO
     * @return
     */
    public Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> getPlatformcodesForURICode(String cruiseID, PlatformCodesDAO platformCodeDAO) {
        Map<String, no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue> platMap = new HashMap<>();
        Map<String, TypeValue> platformMap = platformCodeDAO.getMissionPlatformCodes(cruiseID);

        if (!platformMap.isEmpty()) {

            for (String platform : platformMap.keySet()) {
                no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue tv = new no.imr.nmdapi.lib.nmdapipathgenerator.TypeValue();
                tv.setType(platformMap.get(platform).getType());
                tv.setValue(platformMap.get(platform).getValue());
                platMap.put(tv.getType(), tv);
            }
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
    public String generateCruiseCode(CruiseType cruise, PlatformDAO platformDAO) {
        if (cruise.getCruiseCode() == null || cruise.getCruiseCode().trim().length() == 0) {
            return cruise.getCruisetype().toString() + "-"
                    + cruise.getStartyear() + "-"
                    + platformDAO.getMissionPlatform(cruise.getId()) + "-"
                    + cruise.getCruiseNumber().toString();
        }
        return cruise.getCruiseCode();

    }
}

package no.imr.nmdapi.client.loader.service;

import java.util.List;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.DataTypeEnum;
import no.imr.nmd.commons.cruise.jaxb.DatasetType;
import no.imr.nmd.commons.cruise.jaxb.DatasetsType;
import no.imr.nmd.commons.cruise.jaxb.ExistsEnum;
import no.imr.nmd.commons.cruise.jaxb.PersonCruiseType;
import no.imr.nmd.commons.cruise.jaxb.PersonsType;
import no.imr.nmd.commons.cruise.jaxb.PlatformInfoType;
import no.imr.nmd.commons.cruise.jaxb.RoleEnum;
import no.imr.nmd.commons.cruise.jaxb.RoleType;
import no.imr.nmd.commons.cruise.jaxb.RolesType;
import no.imr.nmd.commons.cruise.jaxb.SeaAreaType;
import no.imr.nmd.commons.cruise.jaxb.SeaAreasType;
import no.imr.nmdapi.client.loader.dao.CruiseDAO;
import no.imr.nmdapi.client.loader.dao.CruiseInformationDAO;
import no.imr.nmdapi.client.loader.dao.CruiseStatusDAO;
import no.imr.nmdapi.client.loader.dao.DatatypesDAO;
import no.imr.nmdapi.client.loader.dao.PlatformCodesDAO;
import no.imr.nmdapi.client.loader.dao.SeaAreaDAO;
import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service("createCruiseService")
public class CreateCruise {

    private static final int RENTED_VESSEL_CRUISE = 5;
    private static final int RESEARCH_VESSEL_CRUISE = 4;

    @Autowired
    private SeaAreaDAO seaAreaDao;

    @Autowired
    private CruiseStatusDAO cruisestatus;

    @Autowired
    private PlatformCodesDAO platformcodeDAO;

    @Autowired
    private PlatformInformationService platformInformationService;

    @Autowired
    private DatatypesDAO datatypeDAO;

    @Autowired
    private CruiseInformationDAO cruiseMissionDAO;

    /**
     * Create a cruiseType object for the given cruise ID
     *
     * @param cruiseID
     * @param cruiseDAO
     * @return
     */
    public CruiseType createCruise(String cruiseID, CruiseDAO cruiseDAO) {
        CruiseType cruise = cruiseDAO.getCruise(cruiseID);
        PersonsType pt = new PersonsType();
        pt.getPerson().add(cruiseDAO.getCoordinator(cruiseID));

        if (cruise.getCruisetype().intValue() == RESEARCH_VESSEL_CRUISE) {
            cruise.setRented(Boolean.FALSE);
        } else if (cruise.getCruisetype().intValue() == RENTED_VESSEL_CRUISE) {
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
        PlatformInfoType platformInfo = platformInformationService.addPlatformInformation(cruise.getId(), platformcodeDAO);
        cruise.setPlatformInfo(platformInfo);

        //Data types
        DatasetsType types = new DatasetsType();
        for (DatatypesDAO.Datatypes datatype : DatatypesDAO.Datatypes.values()) {
            types.getDataset().add(getDataType(datatype.getDTEnum(), datatypeDAO.hasDatatype(cruiseID, datatype.getName())));
        }
        cruise.setDatasets(types);

        return cruise;
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
            if (cruiseinfo.getFirstName() != null || cruiseinfo.getLastName() != null) {
                PersonCruiseType person = new PersonCruiseType();
                person.setFirstname(cruiseinfo.getFirstName());
                person.setLastname(cruiseinfo.getLastName());
                RolesType role = new RolesType();
                RoleType roleType = new RoleType();
                roleType.setRolename(RoleEnum.CRUISELEADER);
                role.getRole().add(roleType);
                person.setRoles(role);
                pt.getPerson().add(person);
            }
            cruise.setBeiCruiseNo(cruiseinfo.getBeicruiseno() == 0 ? null : cruiseinfo.getBeicruiseno());
            cruise.setOriginalSurveyNo(cruiseinfo.getOrignalsurveyno());
        }
    }

    private DatasetType getDataType(DataTypeEnum type, ExistsEnum ex) {
        DatasetType datatypeElementType = new DatasetType();
        datatypeElementType.setDataType(type);
        datatypeElementType.setCollected(ex);
        return datatypeElementType;
    }

}

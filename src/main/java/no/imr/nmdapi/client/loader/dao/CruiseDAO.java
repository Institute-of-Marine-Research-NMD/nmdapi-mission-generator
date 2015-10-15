package no.imr.nmdapi.client.loader.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmd.commons.cruise.jaxb.PersonCruiseType;
import no.imr.nmd.commons.cruise.jaxb.RoleEnum;
import no.imr.nmd.commons.cruise.jaxb.RoleType;
import no.imr.nmd.commons.cruise.jaxb.RolesType;
import no.imr.nmdapi.client.loader.convert.MissionTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Terry Hannant
 */
public class CruiseDAO {

    private JdbcTemplate jdbcTemplate;

    private static final String GET_CRUISE = "SELECT m.id, m.missionnumber, m.start_time, m.stop_time, "
            + "m.startyear, m.purpose, m.datapath, mt.code as missiontypecode, "
            + "mt.description as missiontype, m.specificarea, m.comments, m.reporturl from "
            + "nmdmission.mission m, "
            + "nmdreference.missiontype mt "
            + "where mt.id = m.id_r_missiontype "
            + "and m.id = ?";

    private static final String GET_MISSION_TYPE_NAME = "select mt.description from "
            + "nmdreference.missiontype mt where mt.code = ? ";

//    private static final String GET_LAST_UPDATED = "select max(greatest(m.last_edited, cm.last_edited, ds.last_edited)) from nmdmission.mission m, nmdmission.cruisemission cm "
//            + " ,nmdmission.mission_database_status ds "
//            + "where m.id = ? and cm.id_mission = m.id and ds.id_mission = m.id";
    private static final String GET_LAST_UPDATED = "select max(last_edited) from ((select m.last_edited from nmdmission.mission m where id = ?) union "
            + "(select cm.last_edited from nmdmission.cruisemission cm where id_mission = ?) union "
            + "(select ds.last_edited from nmdmission.mission_database_status ds where id_mission = ?)) a";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get all cruises
     *
     * @return
     */
    public List<String> getAllCruiseId() {
        return jdbcTemplate.queryForList("SELECT id FROM nmdmission.mission where id_r_missiontype in ('CBA55215BBEE94E4F0FFB413ECE3D638', '676A700418F156DAA7BE75C8A280E3A3')", String.class);
    }

    /**
     * Get a cruise
     *
     * @param id
     * @return
     */
    public CruiseType getCruise(String id) {
        return jdbcTemplate.queryForObject(GET_CRUISE, new MissionTypeMapper(), id);
    }

    /**
     * Get mission type description
     *
     * @param type
     * @return
     */
    public String getMissionTypeDescription(Integer type) {
        return jdbcTemplate.queryForObject(GET_MISSION_TYPE_NAME, String.class, type);
    }

    /**
     * Get cruise coordinator for a given cruise
     *
     * @param missionID
     * @return
     */
    public PersonCruiseType getCoordinator(String missionID) {
        List<PersonCruiseType> coordinators = jdbcTemplate.query("select firstname, familyname from nmdreference.person where id = (select id_r_responsibleperson from"
                + " nmdmission.mission where id = ?)", new RowMapper<PersonCruiseType>() {

                    @Override
                    public PersonCruiseType mapRow(ResultSet rs, int rowNum) throws SQLException {
                        PersonCruiseType person = new PersonCruiseType();
                        person.setFirstname(rs.getString("firstname"));
                        person.setLastname(rs.getString("familyname"));
                        RolesType role = new RolesType();
                        RoleType roletype = new RoleType();
                        roletype.setRolename(RoleEnum.CRUISECOORDINATOR);
                        role.getRole().add(roletype);
                        person.setRoles(role);
                        return person;
                    }

                }, missionID);
        return coordinators.isEmpty() ? null : coordinators.get(0);
    }

    public Date getLastUpdated(String cruiseID) {
        return jdbcTemplate.queryForObject(GET_LAST_UPDATED, Date.class, cruiseID, cruiseID, cruiseID);
    }
}

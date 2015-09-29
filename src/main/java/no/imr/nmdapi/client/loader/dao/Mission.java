package no.imr.nmdapi.client.loader.dao;

import java.util.List;
import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmdapi.client.loader.convert.MissionTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class Mission {

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

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> getAllCruiseId() {
        return jdbcTemplate.queryForList("SELECT id FROM nmdmission.mission", String.class);
    }

    public CruiseType getCruise(String id) {
        return jdbcTemplate.queryForObject(GET_CRUISE, new MissionTypeMapper(), id);
    }

    public String getMissionTypeDescription(Integer type) {
        return jdbcTemplate.queryForObject(GET_MISSION_TYPE_NAME, String.class, type);
    }

}

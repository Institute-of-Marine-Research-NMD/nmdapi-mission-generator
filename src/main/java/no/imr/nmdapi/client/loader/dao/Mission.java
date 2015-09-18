package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import no.imr.nmdapi.client.loader.convert.MissionTypeMapper;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class Mission {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final String baseQueryString = " select m.id as id,"
            + " missionnumber, "
            + " start_time,"
            + " stop_time,"
            + " startyear,"
            + " purpose,"
            + " datapath,"
            + " mt.code as missiontypecode,"
            + " mt.description as missiontype "
            + " from nmdmission.mission m,"
            + " nmdreference.missiontype mt "
            + "  where m.id_r_missiontype  = mt.id";

    private final String GET_CRUISE = "SELECT m.id, m.missionnumber, m.start_time, m.stop_time, "
            + "m.startyear, m.purpose, m.datapath, mt.code as missiontypecode, "
            + "mt.description as missiontype, m.specificarea, m.comments, m.reporturl from "
            + "nmdmission.mission m, "
            + "nmdreference.missiontype mt "
            + "where mt.id = m.id_r_missiontype "
            + "and m.id = ?";

    private final String GET_MISSION_TYPE_NAME = "select mt.description from "
            + "nmdreference.missiontype mt where mt.code = ? ";

    public int countAll() {
        return jdbcTemplate.queryForObject("select count(*) from nmdmission.mission", Integer.class);
    }

    public int countYear(Integer year) {
        return jdbcTemplate.queryForObject("select count(*) from nmdmission.mission where startyear = ?", Integer.class, year);
    }

    public void proccessMissions(MissionXMLWriter missionXMLWriter) {
        String queryString = baseQueryString;

        jdbcTemplate.query(queryString, missionXMLWriter);
    }

    public CruiseType getCruise(String id) {
        return jdbcTemplate.queryForObject(GET_CRUISE, new MissionTypeMapper(), id);
    }

    public String getMissionTypeDescription(Integer type) {
        return jdbcTemplate.queryForObject(GET_MISSION_TYPE_NAME, String.class, type);
    }

}

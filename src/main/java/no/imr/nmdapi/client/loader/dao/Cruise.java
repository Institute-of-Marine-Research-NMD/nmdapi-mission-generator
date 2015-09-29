package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import no.imr.nmdapi.client.loader.convert.CruiseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class Cruise {

    private JdbcTemplate jdbcTemplate;
    private static final String BASE_QUERY_STRING = " select cruisecode, "
            + " departureport,"
            + " arrivalport,"
            + " beicruiseno,"
            + " originalsurveyno,"
            + "  firstname||' '||familyname as fullname"
            + " from ("
            + "    select  * "
            + "    from nmdmission.cruisemission"
            + "   where id_mission = ?"
            + ") cm "
            + " left outer join nmdreference.person p on cm.id_r_cruiseleader = p.id";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public CruiseInfo getMissionCruise(String missionID) {
        return jdbcTemplate.queryForObject(BASE_QUERY_STRING, new CruiseMapper(), missionID);
    }

}

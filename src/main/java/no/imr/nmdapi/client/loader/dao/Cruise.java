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

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final String baseQueryString = " select cruisecode, "
            + " departureport,"
            + " arrivalport,"
            + " beicruiseno,"
            + "  firstname||' '||familyname as fullname"
            + " from ("
            + "    select  * "
            + "    from nmdmission.cruisemission"
            + "   where id_mission = ?"
            + ") cm "
            + " left outer join nmdreference.person p on cm.id_r_cruiseleader = p.id";

    public CruiseInfo getMissionCruise(String missionID) {
        return jdbcTemplate.queryForObject(baseQueryString, new CruiseMapper(), missionID);
    }

}

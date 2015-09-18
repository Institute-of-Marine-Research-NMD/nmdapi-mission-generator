package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class Platform {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final String baseQueryString = " select platform  "
            + "from nmdreference.platform p,"
            + " nmdmission.mission m "
            + " where p.id = m.id_r_platform"
            + " and m.id =? ";

    public String getMissionPlatform(String missionID) {
        String queryString = baseQueryString;

        return jdbcTemplate.queryForObject(queryString, String.class, missionID);
    }

}

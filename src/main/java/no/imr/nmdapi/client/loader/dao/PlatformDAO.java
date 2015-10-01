package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class PlatformDAO {

    private JdbcTemplate jdbcTemplate;
    private static final String BASE_QUERY_STRING = " select platform  "
            + "from nmdreference.platform p,"
            + " nmdmission.mission m "
            + " where p.id = m.id_r_platform"
            + " and m.id =? ";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String getMissionPlatform(String missionID) {
        String queryString = BASE_QUERY_STRING;

        return jdbcTemplate.queryForObject(queryString, String.class, missionID);
    }

}

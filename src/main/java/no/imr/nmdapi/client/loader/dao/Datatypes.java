package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant 
 */
public class Datatypes {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int countBiotic(String missionID) {
        return jdbcTemplate.queryForObject("select count(*) from"
                + " nmdbiotic.fishstation where id_m_mission = ?", Integer.class, missionID);
    }

    public int countEchoSounder(String missionID) {
        return jdbcTemplate.queryForObject("select count(*) from"
                + " nmdechosounder.echosounder_dataset where id_m_mission = ?", Integer.class, missionID);
    }

}

package no.imr.nmdapi.client.loader.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.SeaAreaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author sjurl
 */
public class SeaAreaDAO {

    private JdbcTemplate jdbcTemplate;

    private static final String GET_SEAAREA_FOR_CRUISE = "select sa.name, sa.description from nmdreference.seaarea sa where id in ("
            + "select id_r_seaarea from nmdmission.mission_seaarea where id_mission = ?)";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get sea areas for a given cruise
     *
     * @param cruiseid
     * @return
     */
    public List<SeaAreaType> getSeaareas(String cruiseid) {
        return jdbcTemplate.query(GET_SEAAREA_FOR_CRUISE, new RowMapper<SeaAreaType>() {

            @Override
            public SeaAreaType mapRow(ResultSet rs, int rowNum) throws SQLException {
                SeaAreaType seaarea = new SeaAreaType();
                seaarea.setCode(rs.getString("name"));
                seaarea.setName(rs.getString("description"));
                return seaarea;
            }

        }, cruiseid);
    }

}

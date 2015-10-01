package no.imr.nmdapi.client.loader.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.CruiseStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author sjurl
 */
public class CruiseStatusDAO {

    private JdbcTemplate jdbcTemplate;
    private static final String QUERY = "select name from nmdreference.u_udplist "
            + "where id = (select id_r_udplist_missionstatus from nmdmission.mission where id = ?)";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public CruiseStatusEnum getCruiseStatus(String missionid) {
        List<String> result = jdbcTemplate.query(QUERY, new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("name");
            }
        }, missionid);
        
        if (result != null && !result.isEmpty()) {
             switch (result.get(0)) {
                case "1":
                    return CruiseStatusEnum.PLANNED;
                case "2":
                    return CruiseStatusEnum.PERFORMED;
                case "3":
                    return CruiseStatusEnum.CANCELLED;
                case "4":
                    return CruiseStatusEnum.CREATED_IN_IMPORT;
            }
        }
        return null;
    }
}

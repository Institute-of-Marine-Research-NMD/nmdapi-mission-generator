package no.imr.nmdapi.client.loader.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import no.imr.nmd.commons.cruise.jaxb.DataTypeEnum;
import no.imr.nmd.commons.cruise.jaxb.ExistsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Terry Hannant
 */
public class DatatypesDAO {

    public enum Datatypes {

        BIOTIC(DataTypeEnum.BIOTIC, "Biotic"),
        ECHOSOUNDER(DataTypeEnum.ECHOSOUNDER, "Echosounder");

        private final String datatype;
        private final DataTypeEnum dtenum;

        private Datatypes(final DataTypeEnum dtenum, final String datatype) {
            this.dtenum = dtenum;
            this.datatype = datatype;
        }

        public DataTypeEnum getDTEnum() {
            return dtenum;
        }

        public String getName() {
            return datatype;
        }
    }

    private JdbcTemplate jdbcTemplate;
    private static final String HAS_DATATYPE_QUERY = "select m.name from nmdmission.mission_database_status st, nmdreference.u_udplist m, nmdreference.u_database db where"
            + " st.id_mission = ? and db.name = ? and st.id_r_database = db.id and m.id = st.id_r_udplist_missionstatus";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Check if a given cruise has a given datatype.
     *
     * @param idCruise
     * @param datatype
     * @return
     */
    public ExistsEnum hasDatatype(final String idCruise, final String datatype) {
        PreparedStatementCreator psc = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(HAS_DATATYPE_QUERY);
                ps.setString(1, idCruise);
                ps.setString(2, datatype);
                return ps;
            }
        };
        List<String> status = jdbcTemplate.query(psc, new RowMapper<String>() {

            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("name");
            }
        });

        if (status.isEmpty()) {
            return ExistsEnum.UNKNOWN;
        } else if ("1".equals(status.get(0)) || "2".equals(status.get(0)) || "4".equals(status.get(0)) || "5".equals(status.get(0))) {
            return ExistsEnum.YES;
        } else if ("3".equals(status.get(0))) {
            return ExistsEnum.NO;
        }
        return ExistsEnum.UNKNOWN;
    }

}

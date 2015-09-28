package no.imr.nmdapi.client.loader.convert;

import java.sql.ResultSet;
import java.sql.SQLException;
import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import org.springframework.jdbc.core.RowMapper;

/**
 * Map cruise info
 *
 * @author Terry Hannant
 */
public class CruiseMapper implements RowMapper<CruiseInfo> {

    @Override
    public CruiseInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        CruiseInfo cruise = new CruiseInfo();

        cruise.setArrivalPort(rs.getString("arrivalport"));
        cruise.setDepartPort(rs.getString("departureport"));
        cruise.setCruiseCode(rs.getString("cruisecode"));
        cruise.setFullName(rs.getString("fullname"));
        cruise.setBeicruiseno(rs.getInt("beicruiseno"));
        cruise.setOrignalsurveyno(rs.getString("originalsurveyno"));

        return cruise;
    }

}

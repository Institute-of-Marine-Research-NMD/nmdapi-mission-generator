package no.imr.nmdapi.client.loader.convert;

import java.sql.ResultSet;
import java.sql.SQLException;
import no.imr.nmdapi.generic.nmdmission.domain.v1.PlatformType;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class PlatformMapper  implements RowMapper<PlatformType> {
    
    public PlatformType  mapRow(ResultSet rs, int rowNum) throws SQLException {
        PlatformType platform = new PlatformType();

        platform.setType(rs.getString("platformcodesysname"));
        platform.setValue(rs.getString("platformcode"));
           
        return platform;
    }
    
}

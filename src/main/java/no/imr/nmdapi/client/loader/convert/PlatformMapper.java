package no.imr.nmdapi.client.loader.convert;

import java.sql.ResultSet;
import java.sql.SQLException;
import no.imr.nmdapi.client.loader.pojo.TypeValue;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class PlatformMapper implements RowMapper<TypeValue> {

    public TypeValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        TypeValue platform = new TypeValue();

        platform.setType(rs.getString("platformcodesysname"));
        platform.setValue(rs.getString("platformcode"));

        return platform;
    }

}

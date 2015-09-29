package no.imr.nmdapi.client.loader.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.convert.PlatformMapper;
import no.imr.nmdapi.client.loader.pojo.TypeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant
 */
public class PlatformCodes {

    private JdbcTemplate jdbcTemplate;
    private final static String BASE_QUERY_STRING = " select platformcode , "
            + "pcs.platformcodesysname as platformcodesysname  "
            + "from nmdreference.platformcode pc,"
            + " nmdreference.platformcodesys pcs,"
            + " nmdmission.mission m "
            + " where pc.id_platformcodesys = pcs.id"
            + " and  pc.id_platform = m.id_r_platform"
            + " and m.id = ? "
            + " and m.start_time >= pc.firstvaliddate  "
            + " order by   pc.firstvaliddate ";

    private final static String INVERTED_BASE_QUERY_STRING = " select platformcode , "
            + "pcs.platformcodesysname as platformcodesysname  "
            + "from nmdreference.platformcode pc,"
            + " nmdreference.platformcodesys pcs,"
            + " nmdmission.mission m "
            + " where pc.id_platformcodesys = pcs.id"
            + " and  pc.id_platform = m.id_r_platform"
            + " and m.id = ? "
            + " and m.start_time < pc.firstvaliddate  "
            + " order by   pc.firstvaliddate ";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Map<String, TypeValue> getMissionPlatformCodes(String missionID) {
        String queryString = BASE_QUERY_STRING;
        Map<String, TypeValue> result = new HashMap<String, TypeValue>();

        //The query will return all platform codes that were valid before mission start.
        //Since we are ordering by firstvaliddate the last platform code of each type found wil be
        // the valid one at mission start. 
        //Using a hash map so previous values will be discarded and only last added (for eacg type) will be kept
        List<TypeValue> platformList = jdbcTemplate.query(queryString, new PlatformMapper(), missionID);
        for (TypeValue platform : platformList) {
            result.put(platform.getType(), platform);
        }

        return result;
    }

    public Map<String, TypeValue> getMissionPlatformAfterStart(String missionID) {
        String queryString = INVERTED_BASE_QUERY_STRING;
        Map<String, TypeValue> result = new HashMap<String, TypeValue>();

        List<TypeValue> platformList = jdbcTemplate.query(queryString, new PlatformMapper(), missionID);
        for (TypeValue platform : platformList) {
            result.put(platform.getType(), platform);
        }

        return result;
    }

}

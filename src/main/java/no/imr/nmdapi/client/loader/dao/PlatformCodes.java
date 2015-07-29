package no.imr.nmdapi.client.loader.dao;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import no.imr.nmdapi.generic.nmdmission.domain.v1.PlatformInfoType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.PlatformType;
import no.imr.nmdapi.client.loader.convert.PlatformMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class PlatformCodes {
    
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    private String baseQueryString = " select platformcode , "
            + "pcs.platformcodesysname as platformcodesysname  "
            + "from nmdreference.platformcode pc,"
            + " nmdreference.platformcodesys pcs,"
            + " nmdmission.mission m " 
            + " where pc.id_platformcodesys = pcs.id"
            + " and  pc.id_platform = m.id_r_platform" 
            + " and m.id = ? "
            + " and m.start_time >= pc.firstvaliddate  "
            + " order by   pc.firstvaliddate ";

   private String invertBaseQueryString = " select platformcode , "
            + "pcs.platformcodesysname as platformcodesysname  "
            + "from nmdreference.platformcode pc,"
            + " nmdreference.platformcodesys pcs,"
            + " nmdmission.mission m " 
            + " where pc.id_platformcodesys = pcs.id"
            + " and  pc.id_platform = m.id_r_platform" 
            + " and m.id = ? "
            + " and m.start_time < pc.firstvaliddate  "
            + " order by   pc.firstvaliddate ";

    
    public Map<String,PlatformType> getMissionPlatformCodes(String missionID){
        String queryString = baseQueryString;
        HashMap<String,PlatformType> result = new HashMap<String,PlatformType>();

        //The query will return all platform codes that were valid before mission start.
        //Since we are ordering by firstvaliddate the last platform code of each type found wil be
        // the valid one at mission start. 
        //Using a hash map so previous values will be discarded and only last added (for eacg type) will be kept
        List platformList  = jdbcTemplate.query(queryString,new PlatformMapper(),missionID);
            for ( Object platform:platformList){
                result.put( ((PlatformType) platform ).getType(),((PlatformType) platform ));
            }
        
        return result;
     }

    public Map<String,PlatformType> getMissionPlatformAfterStart(String missionID){
        String queryString = invertBaseQueryString;
        HashMap<String,PlatformType> result = new HashMap<String,PlatformType>();

        List platformList  = jdbcTemplate.query(queryString,new PlatformMapper(),missionID);
            for ( Object platform:platformList){
                result.put( ((PlatformType) platform ).getType(),((PlatformType) platform ));
            }
        
        return result;
     }


   
}

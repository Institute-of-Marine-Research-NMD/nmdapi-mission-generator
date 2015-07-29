package no.imr.nmdapi.client.loader.dao;

import javax.sql.DataSource;
import no.imr.nmdapi.client.loader.convert.MissionXMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class Mission {
    
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    private String baseQueryString = " select m.id as id,"
            + " missionnumber, "
            + " start_time,"
            + " stop_time,"
            + " startyear,"
            + " purpose,"
            + " datapath,"
            + " mt.code as missiontypecode,"   
            + " mt.description as missiontype "   
            + " from nmdmission.mission m,"
            + " nmdreference.missiontype mt " 
            +"  where m.id_r_missiontype  = mt.id";
    
    public int countAll()
    {
     return jdbcTemplate.queryForObject("select count(*) from nmdmission.mission", Integer.class);
    }
  
     public int countYear(Integer year)
    {
     return jdbcTemplate.queryForObject("select count(*) from nmdmission.mission where startyear = ?", Integer.class,year);
    }
  
   
      public void  proccessMissions(MissionXMLWriter missionXMLWriter){
        String queryString = baseQueryString ;
                
        jdbcTemplate.query(queryString,missionXMLWriter);
     }

     
   
}

package no.imr.nmdapi.client.loader.convert;

import java.io.File;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import no.imr.nmdapi.generic.nmdmission.domain.v1.DatatypeElementType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.DatatypesListElementType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.ExistsEnum;
import no.imr.nmdapi.generic.nmdmission.domain.v1.MissionType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.PlatformType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.PlatformInfoType;
import no.imr.nmdapi.generic.nmdmission.domain.v1.QualityEnum;

import no.imr.nmdapi.client.loader.pojo.CruiseInfo;
import no.imr.nmdapi.client.loader.dao.Cruise;
import no.imr.nmdapi.client.loader.dao.PlatformCodes;
import no.imr.nmdapi.client.loader.dao.Platform;
import no.imr.nmdapi.client.loader.dao.Datatypes;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.FileSystemUtils;

/**
 *
 * @author Terry Hannant <a5119>
 */
public class MissionXMLWriter implements RowCallbackHandler {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MissionXMLWriter.class);
    
    private static final String NOPLATFORM="NOPLATFORMCODES";
    
    
    @Autowired PlatformCodes platformCodeDAO;
    @Autowired Cruise cruiseDAO;
    @Autowired Datatypes datatypeDAO;
    @Autowired Platform platformDAO;
    @Autowired String basePath;
    @Autowired String baseErrorPath;
    
    
    int totalCount;
    private int percentStep;
    XMLTypeConverter xmlTypeConverter;
    private int rowCount;
    private Marshaller marshaller;
 
    public MissionXMLWriter(){
        xmlTypeConverter = new XMLTypeConverter();
        rowCount  = 0;
    }

    public void  init(boolean deleteErrors) throws JAXBException
    {
        JAXBContext ctx = JAXBContext.newInstance("no.imr.nmdapi.generic.nmdmission.domain.v1");
        marshaller = ctx.createMarshaller();
        FileSystemUtils.deleteRecursively(new File(basePath));
        if (deleteErrors)
        {
           FileSystemUtils.deleteRecursively(new File(baseErrorPath));
      }
    }
    
    
    public void setTotalCount(float totalCount) {
        this.totalCount = (int) totalCount;
        this.percentStep = (int) Math.round(5.0*totalCount/100.0);
    }
    

    @Override
    public void processRow(ResultSet rs) throws SQLException {

        String missionID = rs.getString("id");
        String missiontType = rs.getString("missiontype");
        String missionTypeCode = rs.getString("missionTypeCode");
     
        rowCount++;
     
        MissionType mission = new MissionType();
        
        mission.setMissionNumber(BigInteger.valueOf(rs.getInt("missionnumber")));
        mission.setDatapath(rs.getString("datapath"));
        mission.setStartyear(BigInteger.valueOf(rs.getInt("startyear")));
        mission.setStartTime(xmlTypeConverter.convertDate(rs.getDate("start_time")));
        mission.setStopTime(xmlTypeConverter.convertDate(rs.getDate("stop_time")));
           
         //Create purpose 
         MissionType.Purpose purpose = new MissionType.Purpose();
         purpose.setLang("no");  //TODO How should this be really set? Parse for norsk special chars?
         purpose.setValue(rs.getString("purpose"));
         mission.getPurpose().add(purpose);

        //Cruise info
         CruiseInfo cruise=null;
         try
         {
         cruise = cruiseDAO.getMissionCruise(missionID);
         }
         catch (EmptyResultDataAccessException erdae){
             // Can discard exception as empty set is ok to return
         }
         
        if (cruise != null)
        {
        mission.setArrivalPort(cruise.getArrivalPort());
        mission.setDeparturePort(cruise.getDepartPort());
        mission.setCruiseCode(cruise.getCruiseCode());
        mission.setCruiseLeader(cruise.getFullName());
        }

        
        //Platform info
        Map<String,PlatformType> platformMap = platformCodeDAO.getMissionPlatformCodes(missionID);
        if (platformMap.size() >0 )
        {
            PlatformInfoType platformInfo = new PlatformInfoType();
            for ( String platform:platformMap.keySet()){
              platformInfo.getPlatform().add(platformMap.get(platform));
            }
            mission.setPlatformInfo(platformInfo);
        }
        String platformPath = createPlatformURICode(platformMap);
        
        //Data types
        DatatypesListElementType types = new DatatypesListElementType();
        
        
        boolean hasBiotic = (datatypeDAO.countBiotic(missionID) >0);
        boolean hasEchosouder=( datatypeDAO.countEchoSounder(missionID) >0);

        types.getDatatype().add(getDataType("biotic","",hasBiotic?ExistsEnum.YES:ExistsEnum.NO));
        types.getDatatype().add(getDataType("echosounder","",hasEchosouder?ExistsEnum.YES:ExistsEnum.NO));

        mission.setDatatypes(types);
        
        
        String delivery;
        //Map delivery ident
        if (mission.getCruiseCode() == null) {
   
            delivery =missionTypeCode+"-"+
                    mission.getStartyear()+"-"+
                    platformDAO.getMissionPlatform(missionID)+"-"+
                    mission.getMissionNumber().toString();
            
            
        } else if (mission.getCruiseCode().trim().length() == 0) 
        {
                  delivery =missionTypeCode+"-"+
                    mission.getStartyear()+"-"+
                    platformDAO.getMissionPlatform(missionID)+"-"+
                    mission.getMissionNumber().toString();
            
        }
        else {
            delivery = mission.getCruiseCode();
        }
            
        
      //Check for missing manadatory data
      if (platformPath.equals("NOPLATFORMCODES")){
             String altPlatformPath = createPlatformURICode( platformCodeDAO.getMissionPlatformAfterStart(missionID));
                   if (altPlatformPath.equals("NOPLATFORMCODES")){
                        writeToProblemFile( mission, missiontType, platformPath, delivery, missionID, "No Platform codes");
                   } else {
                        writeToProblemFile( mission, missiontType, platformPath, delivery, missionID, "Platform code only after start");
                   }
      }
         
        //Write file out
         File file = mapToFile(mission,missiontType,platformPath,delivery);
          if (file.exists())
            {
                writeToProblemFile( mission, missiontType, platformPath, delivery, missionID, "Duplicate");
            }
        writeMission(file,mission);
          
        rowCount++;
        if ((rowCount%percentStep) == 0) {
             LOG.info((int) Math.round(rowCount/totalCount*100)+"%");
        } 
         
            
    
        LOG.info( "Total missions proccessed ",rowCount);
    }
    
    
   
    private File mapToFile(MissionType mission,String missionType,String platformCode, String delivery) {

        File fullPath=new File(basePath+File.separator+
                   missionType+File.separator+
                   mission.getStartyear().toString()+File.separator+
                   platformCode+File.separator+
                   delivery+File.separator+
                   "mission"
           );
           if (!fullPath.exists())
           {
             fullPath.mkdirs();
           }
           
            return new File(fullPath,"data.xml");
       }

      
    private void writeToProblemFile( MissionType mission,String missionType,String platformCode, String delivery,String missionID,String problem) {
           File file;
       
           
           File fullPath=new File(baseErrorPath+File.separator+problem);
 
           if (!fullPath.exists())
           {
             fullPath.mkdirs();
           }
           file = new File(fullPath,missionType+"_"+
                   mission.getStartyear().toString()+"_"+
                   platformCode+"_"+
                   delivery+"_"+
                   missionID+".xml");
            writeMission(file,mission);
       }
    
      
    public DatatypeElementType getDataType(String type, String desc, ExistsEnum ex) {
        DatatypeElementType datatypeElementType = new DatatypeElementType();
        datatypeElementType.setName(type);
        datatypeElementType.setQualityAssured(QualityEnum.NONE);
        datatypeElementType.setExists(ex);
        return datatypeElementType;
    }

  
    /**
     * 
     *Attempt to create Platform part of URL
     * @param platformMap
     * @return 
     */    
    private String createPlatformURICode(Map<String, PlatformType> platformMap) {
        String shipName = null;
        String callSign = null;
        String result;
        
        if (platformMap.containsKey("Ship Name"))
         {
             shipName=platformMap.get("Ship Name").getValue();
             shipName=shipName.replace('.',' ').replace(File.separator," ");
         }
         if (platformMap.containsKey("ITU Call Sign"))
         {
             callSign=platformMap.get("ITU Call Sign").getValue();
             callSign=callSign.replace('.',' ').replace(File.separator," ");
         }
         if (shipName != null){
             if (callSign != null) {
                 result = shipName+"-"+callSign;
             }
             else
             {
                 result = shipName;
             }
         } else if (callSign !=null){
                 result = callSign;
         } else {
             result =NOPLATFORM;
         }
         
         return result;
           
        
    }

    private void writeMission(File file, MissionType mission) {
            try {
            marshaller.marshal(mission, file);
        } catch (JAXBException ex) {
            Logger.getLogger(MissionXMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
    
    
}

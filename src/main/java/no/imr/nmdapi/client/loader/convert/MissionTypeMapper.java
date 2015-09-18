/*
 */
package no.imr.nmdapi.client.loader.convert;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import no.imr.nmd.commons.cruise.jaxb.CruiseType;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author sjurl
 */
public class MissionTypeMapper implements RowMapper<CruiseType> {

    @Override
    public CruiseType mapRow(ResultSet rs, int i) throws SQLException {
        CruiseType cruise = new CruiseType();
        cruise.setId(rs.getString("id"));
        cruise.setMissiontype(BigInteger.valueOf(rs.getInt("missiontypecode")));
        cruise.setStartyear(BigInteger.valueOf(rs.getInt("startyear")));

        cruise.setMissionNumber(BigInteger.valueOf(rs.getInt("missionnumber")));
        XMLTypeConverter xmlTypeConverter = new XMLTypeConverter();
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cruise.setStartTime(xmlTypeConverter.convertDate(rs.getTimestamp("start_time", cal)));
        cruise.setStopTime(xmlTypeConverter.convertDate(rs.getTimestamp("stop_time", cal)));
        //responsible person missing
        cruise.setSpecificArea(rs.getString("specificarea"));
        //Create purpose 
        CruiseType.Purpose purpose = new CruiseType.Purpose();
        purpose.setLang("no");  //TODO How should this be really set? Parse for norsk special chars?
        purpose.setValue(rs.getString("purpose"));
        cruise.getPurpose().add(purpose);
        cruise.setComments(rs.getString("comments"));

        cruise.setDatapath(rs.getString("datapath"));
        cruise.setReportUrl(rs.getString("reporturl"));

        return cruise;
    }

}

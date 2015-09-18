package no.imr.nmdapi.client.loader.convert;

import java.sql.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Terry Hannant
 */
public class XMLTypeConverter {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(XMLTypeConverter.class);

    public XMLGregorianCalendar convertDate(Date date) {
        XMLGregorianCalendar result = null;
        if (date != null) {

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);

            try {
                result = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (DatatypeConfigurationException ex) {
                log.error("Can not create calendar", ex);
            }

        }
        return result;

    }

}

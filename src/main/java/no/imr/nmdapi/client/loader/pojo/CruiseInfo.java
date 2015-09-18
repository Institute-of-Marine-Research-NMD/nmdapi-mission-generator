package no.imr.nmdapi.client.loader.pojo;

/**
 * Simple pojp for all cruise info
 *
 * @author Terry Hannant
 */
public class CruiseInfo {

    private String cruiseCode;
    private String departPort;
    private String arrivalPort;
    private String fullName;
    private Integer beicruiseno;
    private String orignalsurveyno;

    /**
     * @return the cruiseCode
     */
    public String getCruiseCode() {
        return cruiseCode;
    }

    /**
     * @param cruiseCode the cruiseCode to set
     */
    public void setCruiseCode(String cruiseCode) {
        this.cruiseCode = cruiseCode;
    }

    /**
     * @return the departPort
     */
    public String getDepartPort() {
        return departPort;
    }

    /**
     * @param departPort the departPort to set
     */
    public void setDepartPort(String departPort) {
        this.departPort = departPort;
    }

    /**
     * @return the arrivalPort
     */
    public String getArrivalPort() {
        return arrivalPort;
    }

    /**
     * @param arrivalPort the arrivalPort to set
     */
    public void setArrivalPort(String arrivalPort) {
        this.arrivalPort = arrivalPort;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the beicruiseno
     */
    public Integer getBeicruiseno() {
        return beicruiseno;
    }

    /**
     * @param beicruiseno the beicruiseno to set
     */
    public void setBeicruiseno(Integer beicruiseno) {
        this.beicruiseno = beicruiseno;
    }

    /**
     * @return the orignalsurveyno
     */
    public String getOrignalsurveyno() {
        return orignalsurveyno;
    }

    /**
     * @param orignalsurveyno the orignalsurveyno to set
     */
    public void setOrignalsurveyno(String orignalsurveyno) {
        this.orignalsurveyno = orignalsurveyno;
    }


}

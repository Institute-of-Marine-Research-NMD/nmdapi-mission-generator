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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCruiseCode() {
        return cruiseCode;
    }

    public void setCruiseCode(String cruiseCode) {
        this.cruiseCode = cruiseCode;
    }

    public String getDepartPort() {
        return departPort;
    }

    public void setDepartPort(String departPort) {
        this.departPort = departPort;
    }

    public String getArrivalPort() {
        return arrivalPort;
    }

    public void setArrivalPort(String arrivalPort) {
        this.arrivalPort = arrivalPort;
    }

}

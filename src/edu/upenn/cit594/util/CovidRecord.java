package edu.upenn.cit594.util;

public class CovidRecord {

    private int zipCode;

    private int partiallyVaccinated;

    private int fullyVaccinated;

    private String etlTimestamp;

    public CovidRecord() {

    }
    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }
    public void setPartiallyVaccinated(int partiallyVaccinated) {
        this.partiallyVaccinated = partiallyVaccinated;
    }
    public void setFullyVaccinated(int fullyVaccinated) {
        this.fullyVaccinated = fullyVaccinated;
    }
    public void setEtlTimestamp(String etlTimestamp) {
        this.etlTimestamp = etlTimestamp;
    }
    public int getZipCode() {
        return zipCode;
    }
    public int getPartiallyVaccinated() {
        return partiallyVaccinated;
    }
    public int getFullyVaccinated() {
        return fullyVaccinated;
    }
    public String getEtlTimestamp() {
        return etlTimestamp;
    }
    public String getDate(){
        String[] dateTime = etlTimestamp.split(" ");
        return dateTime[0];
    }
}

package edu.upenn.cit594.util;

public class PropertyRecord {

    private int zipCode;

    private String marketValue;

    private String totalLivableArea;

    public PropertyRecord() {}

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public void setTotalLivableArea(String totalLivableArea) {
        this.totalLivableArea = totalLivableArea;
    }

    public int getZipCode() {
        return zipCode;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public String getTotalLivableArea() {
        return totalLivableArea;
    }


}

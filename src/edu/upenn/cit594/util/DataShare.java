package edu.upenn.cit594.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton to share various data between classes with option to reset
 */
public class DataShare {

    private List<CovidRecord> covidDataList;

    private List<PropertyRecord> propertyDataList;

    private Map<Integer, Integer> populationDataMap;

    private static DataShare dataShare = new DataShare();

    private DataShare() {

    }

    public static DataShare getInstance() {
        return dataShare;
    }

    public void setCovidData(List<CovidRecord> data) {
        this.covidDataList = data;
    }

    public void setPropertyData(List<PropertyRecord> data) {
        this.propertyDataList = data;
    }

    public void setPopulationData(Map<Integer, Integer> data) {
        this.populationDataMap = data;
    }

    public List<CovidRecord> getCovidData() {
        return covidDataList;
    }

    public Map<Integer, Integer> getPopulationMap(){ return populationDataMap;}

    public List<PropertyRecord> getPropertyData() {
        return propertyDataList;
    }

    public void reset() {
        covidDataList = null;
        propertyDataList = null;
        populationDataMap = null;
    }


}

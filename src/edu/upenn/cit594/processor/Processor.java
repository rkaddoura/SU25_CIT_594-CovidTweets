package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.DataShare;
import edu.upenn.cit594.util.PropertyRecord;

import java.util.*;

public class Processor {

    private DataShare ds;
    private final List<CovidRecord> covidRecords;
    private final Map<Integer, Integer> populationMap;
    private final List<PropertyRecord> propertyRecords;

    // memoization
    private Integer totalPopulation;
    private final PropertyMetric mvMetric = new MarketValueMetric();
    private final Map<Integer, Integer> totalMVPerCapInZIP = new HashMap<>();
    private final Set<Integer> availableActions  = new TreeSet<>();
    private final Map<String, Map<Integer, Double>> partialVacPerCap = new HashMap<>();
    private final Map<String, Map<Integer, Double>> fullyVacPerCap = new HashMap<>();
    private final Map<Integer, Integer> avgMarketValueInZIP = new HashMap<>();
    private final Map<Integer, Integer> avgLivableAreaInZIP = new HashMap<>();
    private final Map<String, Float> percentVacInMarketValueRange = new HashMap<>();


    public Processor(){
        this.ds = DataShare.getInstance();
        this.covidRecords = ds.getCovidData();
        this.populationMap = ds.getPopulationMap();
        this.propertyRecords = ds.getPropertyData();
    }


    public Set<Integer> getAvailableActions(){
       if (!availableActions.isEmpty()){
           return availableActions;
       }
        availableActions.add(0);
        availableActions.add(1);

        if (populationMap != null){
            availableActions.add(2);
        }

        if (covidRecords != null && populationMap != null){
            availableActions.add(3);
        }

        if (propertyRecords != null){
            availableActions.add(4);
            availableActions.add(5);
        }

        if (populationMap != null && propertyRecords!= null){
            availableActions.add(6);
        }

        if (populationMap != null && propertyRecords!= null && covidRecords != null){
            availableActions.add(7);
        }

        return availableActions;
    }

    public int calculateTotalPop(){
        if (totalPopulation != null){
            return totalPopulation;
        }

        int totalPop = 0;
        for (Integer zip : populationMap.keySet()){
            totalPop += populationMap.get(zip);
        }

        totalPopulation = totalPop;
        return totalPop;
    }

    public Map<Integer, Double> getVaccinationsPerCapita(String type, String date){

        // check if previously calculated
        if (type.equals("partial") && partialVacPerCap.containsKey(date)){
            return partialVacPerCap.get(date);
        }
        if (type.equals("full") && fullyVacPerCap.containsKey(date)){
            return fullyVacPerCap.get(date);
        }

        // if not previously calculated
        Map<Integer, Integer> vacPerZIP = new HashMap<>();
        Map<Integer, Double> vacPerCapita = new TreeMap<>();

        // select zipcodes with valid record and time
        for (CovidRecord cr : covidRecords){
            if (!date.equals(cr.getDate())){
                continue;
            }

            int zip = cr.getZipCode();
            if (!populationMap.containsKey(zip) || populationMap.get(zip) == 0){
                continue;
            }

            int vacCount;
            if (type.equals("partial")){
                vacCount = cr.getPartiallyVaccinated();
            }else{
                vacCount = cr.getFullyVaccinated();
            }

            if (vacCount == 0){
                continue;
            }
            vacPerZIP.put(zip, (vacPerZIP.getOrDefault(zip, 0) + vacCount));
        }

        // calculate vaccinations per capita
        for (Integer zip : vacPerZIP.keySet()){
            int population = populationMap.get(zip);
            vacPerCapita.put(zip, (vacPerZIP.get(zip) / (double) population));
        }

        if (type.equals("partial")){
            partialVacPerCap.put(date, vacPerCapita);
        }else{
            fullyVacPerCap.put(date, vacPerCapita);
        }

        return vacPerCapita;
    }

    public int calculateAvgInZIP(int zipCode, PropertyMetric pm) {

        boolean isMarketValueMetric = pm instanceof MarketValueMetric;
        boolean isLivableAreaMetric = pm instanceof LivableAreaMetric;

        if (isMarketValueMetric && avgMarketValueInZIP.containsKey(zipCode)){
           return avgMarketValueInZIP.get(zipCode);
        }else if(isLivableAreaMetric && avgLivableAreaInZIP.containsKey(zipCode)){
            return avgLivableAreaInZIP.get(zipCode);
        }

        int propertyCount = 0;
        double metricTotal = 0;

        for (PropertyRecord pr : propertyRecords){
            Float metric = pm.getMetric(pr);
            if (pr.getZipCode() == zipCode && metric != null){
                metricTotal += metric;
                propertyCount++;
            }
        }

        int result;
        if (propertyCount == 0){
             result = 0;
        }else{
            result = (int) (metricTotal / propertyCount);
        }

        // memoize
        if (isMarketValueMetric){
            avgMarketValueInZIP.put(zipCode, result);
        }else{
            avgLivableAreaInZIP.put(zipCode, result);
        }

        return result;
    }

    public int calculateTotalMarketValuePerCapInZIP(int zipCode){

        if (totalMVPerCapInZIP.containsKey(zipCode)){
            return totalMVPerCapInZIP.get(zipCode);
        }

        if (!populationMap.containsKey(zipCode) || populationMap.get(zipCode) == 0){
            totalMVPerCapInZIP.put(zipCode, 0); // memoize
            return 0;
        }

        int population = populationMap.get(zipCode);
        double sumMarketVal = 0;

        for (PropertyRecord pr : propertyRecords){
            if (pr.getZipCode() == zipCode){
                Float marketValue = mvMetric.getMetric(pr);
                if (marketValue != null){
                    sumMarketVal += marketValue;
                }
            }
        }

        int marketValPerCap = (int) (sumMarketVal / population);
        totalMVPerCapInZIP.put(zipCode, marketValPerCap); //memoize

        return marketValPerCap;
    }

    public float calculatePercentVaccinatedGivenMarketVal(int upperBound, int lowerBound){

        String rangeString = lowerBound + " - " + upperBound;
        if (percentVacInMarketValueRange.containsKey(rangeString)){
            return percentVacInMarketValueRange.get(rangeString);
        }

        Set<Integer> zipsInRange = new HashSet<>();
        Map<Integer,Integer> vacRecords = new HashMap<>();

        int totalPop = 0;
        int totalVac = 0;

        // find all zips in range and add total population
        for (int zip : populationMap.keySet()){
            int marketValue = calculateAvgInZIP(zip, mvMetric);
            if (marketValue <= upperBound && marketValue >= lowerBound){
                zipsInRange.add(zip);
                totalPop += populationMap.get(zip);
            }
        }

        // get highest reported total vaccinations in these zipcodes
        for (CovidRecord cr : covidRecords){
            if (zipsInRange.contains(cr.getZipCode())){
                if (vacRecords.containsKey(cr.getZipCode())){
                    if (cr.getFullyVaccinated() > vacRecords.get(cr.getZipCode())){
                        vacRecords.put(cr.getZipCode(), cr.getFullyVaccinated());
                    }
                } else {
                    vacRecords.put(cr.getZipCode(), cr.getFullyVaccinated());
                }
            }
        }

        for (Integer vacNum : vacRecords.values()){
            totalVac += vacNum;
        }

        float result;
        if (totalPop == 0){
            result = 0;
        }else{
            result = ((float) totalVac / (float) totalPop) * 100;
        }

        percentVacInMarketValueRange.put(rangeString, result); // memoize

        return result;
    }
}

package edu.upenn.cit594.util;

public class PopulationRecord {

    private int zipCode;

    private int population;

    public PopulationRecord() {

    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getZipCode() {
        return zipCode;
    }

    public int getPopulation() {
        return population;
    }
}

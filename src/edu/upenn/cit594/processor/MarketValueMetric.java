package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;

public class MarketValueMetric implements PropertyMetric{

    public Float getMetric(PropertyRecord record){

        return parseFloatValue(record.getMarketValue());
    }

}

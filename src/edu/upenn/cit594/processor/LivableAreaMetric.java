package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;

public class LivableAreaMetric implements PropertyMetric{

    public Float getMetric(PropertyRecord record){
        return parseFloatValue(record.getTotalLivableArea());
    }

}

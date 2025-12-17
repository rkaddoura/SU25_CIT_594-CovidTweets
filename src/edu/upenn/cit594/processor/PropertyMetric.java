package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyRecord;

public interface PropertyMetric {

    public Float getMetric(PropertyRecord record);

    default Float parseFloatValue(String value){
        if (value == null || value.isEmpty()){
            return null;
        }else{
            try{
                return Float.parseFloat(value.trim());
            }catch (NumberFormatException e){
                return null;
            }
        }
    }
}

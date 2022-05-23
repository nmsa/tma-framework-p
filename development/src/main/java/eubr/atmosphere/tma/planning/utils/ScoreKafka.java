package eubr.atmosphere.tma.planning.utils;

import java.util.HashMap;

/**
 * This class represents the structure of a score to be published on kafka topic for planning component to consume and 
 * apply rules
 * @author João Ribeiro
 */
public class ScoreKafka {
    private int resourceId;
    private long valueTime;
    private HashMap <Integer,Double> score;

    public ScoreKafka(int resourceId, long valueTime, HashMap<Integer, Double> score) {
        this.resourceId = resourceId;
        this.valueTime = valueTime;
        this.score = score;
    }
    
    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public long getValueTime() {
        return valueTime;
    }

    public void setValueTime(long valueTime) {
        this.valueTime = valueTime;
    }

    public HashMap<Integer, Double> getScore() {
        return score;
    }

    public void setScore(HashMap<Integer, Double> score) {
        this.score = score;
    }
    
}

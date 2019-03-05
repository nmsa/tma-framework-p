package eubr.atmosphere.tma.planning.database;

public class Plan {

    // Maybe this class will need to be moved to tma-utils
    
    public enum STATUS {
        TO_DO,
        IN_PROGRESS,
        COMPLETED;
        
        @Override
        public String toString() {
            return Integer.toString(ordinal());
        }

        public static STATUS valueOf(int ordinal) {
            return (ordinal < values().length) ? values()[ordinal]
                    : COMPLETED;
        }
    }
    
    private int metricId;
    private int qualityModelId;
    private long valueTime;
    private STATUS status; // TODO: define the possible status in a enum
    
    public int getMetricId() {
        return metricId;
    }

    public void setMetricId(int metricId) {
        this.metricId = metricId;
    }

    public int getQualityModelId() {
        return qualityModelId;
    }

    public void setQualityModelId(int qualityModelId) {
        this.qualityModelId = qualityModelId;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public long getValueTime() {
        return valueTime;
    }

    public void setValueTime(long valueTime) {
        this.valueTime = valueTime;
    }
}

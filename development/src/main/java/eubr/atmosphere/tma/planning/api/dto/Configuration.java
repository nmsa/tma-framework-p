package eubr.atmosphere.tma.planning.api.dto;

/**
 * This class holds the information received from the dashboard related to a rule's action configuration data.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */
public class Configuration extends DataObject{
    private int configurationId;
    private String keyName;
    private String value;

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
}

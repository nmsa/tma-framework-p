package eubr.atmosphere.tma.planning.api.dto;

import java.util.ArrayList;

/**
 * This class holds the information received from the dashboard related to a rule's actions to perform.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */
public class Action extends DataObject{
    private int actionId;
    private String actionName;
    private int resourceId;
    private int actuatorId;
    private ArrayList<Configuration> configurations;

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getActuatorId() {
        return actuatorId;
    }

    public void setActuatorId(int actuatorId) {
        this.actuatorId = actuatorId;
    }

    public ArrayList<Configuration> getConfigurationList() {
        return configurations;
    }

    public void setConfigurationList(ArrayList<Configuration> configurations) {
        this.configurations = configurations;
    }
    
    
}

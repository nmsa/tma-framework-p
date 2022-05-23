package eubr.atmosphere.tma.planning.api.dto;

import java.util.ArrayList;

/**
 * This class holds the information received from the dashboard related to a Rule to be added.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */
public class Rule extends DataObject{
    private ArrayList<Action> actionList;
    private int metricId;
    private String operator;
    private double activationThreshold;
    private String ruleName;
    private int resourceId;

    public ArrayList<Action> getActionList() {
        return actionList;
    }

    public void setActionList(ArrayList<Action> actionList) {
        this.actionList = actionList;
    }

    public int getMetricId() {
        return metricId;
    }

    public void setMetricId(int metricId) {
        this.metricId = metricId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public double getActivationThreshold() {
        return activationThreshold;
    }

    public void setActivationThreshold(double activationThreshold) {
        this.activationThreshold = activationThreshold;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    
 
}

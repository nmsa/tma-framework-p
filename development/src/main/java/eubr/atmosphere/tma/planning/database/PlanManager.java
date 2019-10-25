package eubr.atmosphere.tma.planning.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.ConfigurationData;
import eubr.atmosphere.tma.data.Plan;
import eubr.atmosphere.tma.utils.DatabaseManager;

public class PlanManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanManager.class);
    
    public int saveNewPlan(Plan plan) {
        String sql =
                "INSERT INTO Plan(metricId, valueTime, status) VALUES (?, FROM_UNIXTIME(?), ?)";
        PreparedStatement ps;

        try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, plan.getMetricId());
            ps.setLong(2, plan.getValueTime());
            ps.setInt(3, plan.getStatus().ordinal());
            
            DatabaseManager databaseManager = new DatabaseManager();
            return databaseManager.execute(ps);
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting a plan in the database.", e);
        }
        return -1;
    }

    public void saveActionPlan(Plan plan) {
        String sql =
                "INSERT INTO ActionPlan(planId, actionId, executionOrder, status) "
                + "VALUES (?, ?, ?, ?)";
        PreparedStatement ps;

        try {
            DatabaseManager databaseManager = new DatabaseManager();
            for (ActionPlan action : plan.getActionList()) {
                ps = DatabaseManager.getConnectionInstance().prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, action.getPlanId());
                ps.setInt(2, action.getActionId());
                ps.setInt(3, action.getExecutionOrder());
                ps.setInt(4, ActionPlan.STATUS.TO_DO.ordinal());

                databaseManager.execute(ps);
                saveConfigurationData(action.getPlanId(), action.getActionId(), action.getConfigurationList());
            }
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting the actionPlans in the database.", e);
        }
    }

    private void saveConfigurationData(int planId, int actionId, List<ConfigurationData> configurationList) {
        String sql =
                "INSERT INTO ConfigurationData(planId, actionId, configurationId, value) "
                + "VALUES (?, ?, ?, ?)";
        PreparedStatement ps;

        try {
            DatabaseManager databaseManager = new DatabaseManager();
            for (ConfigurationData configuration: configurationList) {
                ps = DatabaseManager.getConnectionInstance().prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, planId);
                ps.setInt(2, actionId);
                ps.setInt(3, configuration.getConfigurationId());
                ps.setString(4, configuration.getValue());

                databaseManager.execute(ps);
            }
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting the configurationData in the database.", e);
        }
    }
}

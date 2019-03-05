package eubr.atmosphere.tma.planning.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Statement;

import eubr.atmosphere.tma.utils.DatabaseManager;

public class PlanManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanManager.class);

    public int saveNewPlan(Plan plan) {
        String sql =
                "INSERT INTO Plan(metricId, qualityModelId, status) VALUES (?, ?, ?)";
        PreparedStatement ps;

        try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, plan.getMetricId());
            ps.setInt(2, plan.getQualityModelId());
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
            for (ActionPlan action : plan.getActionList()) {
                ps = DatabaseManager.getConnectionInstance().prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, action.getPlanId());
                ps.setInt(2, action.getActionId());
                ps.setInt(3, action.getExecutionOrder());
                ps.setInt(4, ActionPlan.STATUS.TO_DO.ordinal());

                DatabaseManager databaseManager = new DatabaseManager();

                // This will be used to insert the configurationData
                int actionPlanId = databaseManager.execute(ps);
            }
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting the actionPlans in the database.", e);
        }
    }
}

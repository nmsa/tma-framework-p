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
                "INSERT INTO Plan(metricId, valueTime, qualityModelId, status) VALUES (?, ?, ?, ?)";
        PreparedStatement ps;

        try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, plan.getMetricId());
            ps.setLong(2, plan.getValueTime());
            ps.setInt(3, plan.getQualityModelId());
            ps.setInt(4, plan.getStatus().ordinal());

            DatabaseManager databaseManager = new DatabaseManager();
            return databaseManager.execute(ps);
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting an actuator in the database.", e);
        }
        return -1;
    }
}

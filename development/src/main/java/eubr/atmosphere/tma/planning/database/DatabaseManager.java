package eubr.atmosphere.tma.planning.database;

import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.ConfigurationData;
import eubr.atmosphere.tma.data.Plan;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private Connection connection = null;
    

    private final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    private void getConnectionInstance() {
        // This will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        // Setup the connection with the DB
        try {
            connection = DriverManager
                    .getConnection("jdbc:mysql://mysql-0.mysql.default.svc.cluster.local:3306/knowledge?"
                            + "user=root&password=passtobereplaced&autoReconnect=true"
                    );
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }
    
    private void close(){
        try{
            connection.close();
        }
        catch(SQLException e){
            LOGGER.error("[ATMOSPHERE] Something went wrong closing database connection.", e);
        }
    }
    
    //================================================ RULES MANIPULATION =========================================
    
    //used to add the initial file
    public int saveRules(String rulesString) {
        String sql = "INSERT INTO AdaptationRules values(1,?)";
        getConnectionInstance();
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            Blob blob = connection.createBlob();
            blob.setBytes(1, rulesString.getBytes());

            ps.setBlob(1, blob);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when saving adaptation rules in the database.", e);
            return -1;
        }
        finally{
            close();
        }
        return 1;
    }
    
    public byte[] readRules() {
        String sql = "SELECT * FROM AdaptationRules";
        getConnectionInstance();
        byte[] rules = null;
        try(Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                Blob blob = rs.getBlob("rulesFile");
                rules = blob.getBytes(1, (int) blob.length());
            }
            //else remain and return null to indicate there's nothing in the database
            
        }
        catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when reading adaptation rules from the database.", e);
        }
        finally{
            close();
        }
        return rules;
    }
    
    public boolean updateRules(String rulesString) {
        String sql = "UPDATE AdaptationRules set rulesFile = ?";
        getConnectionInstance();
        
        boolean success = true;
        
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            Blob blob = connection.createBlob();
            blob.setBytes(1, rulesString.getBytes());

            ps.setBlob(1, blob);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when updating adaptation rules in the database.", e);
            success = false;
        }
        finally{
            close();
        }
        return success;
    }
    
    
    //================================================ PLANS MANIPULATION =========================================
    
    public int saveNewPlan(Plan plan) {
        getConnectionInstance();
        
        String sql =
                "INSERT INTO Plan(metricId, valueTime, status, resourceId) VALUES (?, FROM_UNIXTIME(?), ?, ?)";
        PreparedStatement ps;
        
        int planId = -1;
        
        try {
            ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, plan.getMetricId());
            ps.setLong(2, plan.getValueTime());
            ps.setInt(3, plan.getStatus().ordinal());
            ps.setInt(4, plan.getResourceId());
            
            ps.execute();
            
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                planId = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting a plan in the database.", e);
        } finally{
            close();
        }
        return planId;
    }

    public void saveActionPlan(Plan plan) {
        getConnectionInstance();
        String sql =
                "INSERT INTO ActionPlan(planId, actionId, executionOrder, status) "
                + "VALUES (?, ?, ?, ?)";

        try(PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            //start transaction
            connection.setAutoCommit(false);
            
            for (ActionPlan action : plan.getActionList()) {
                ps.setInt(1, action.getPlanId());
                ps.setInt(2, action.getActionId());
                ps.setInt(3, action.getExecutionOrder());
                ps.setInt(4, ActionPlan.STATUS.TO_DO.ordinal());
                
                ps.executeUpdate();
                
                saveConfigurationData(action.getPlanId(), action.getActionId(), action.getConfigurationList());
            }
            connection.commit();
        } 
        catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting the actionPlans in the database.", e);
            
            if (connection != null) {
                try {
                  connection.rollback();
                } catch (SQLException excep) {
                    LOGGER.error("[ATMOSPHERE] Error when trying to rollback the actionPlans insertion in the database.", e);
                }
            }
        } 
        finally{
            close();
        }
    }

    private void saveConfigurationData(int planId, int actionId, List<ConfigurationData> configurationList) {
        String sql =
                "INSERT INTO ConfigurationData(planId, actionId, configurationId, value) "
                + "VALUES (?, ?, ?, ?)";

        try(PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            for (ConfigurationData configuration: configurationList) {
                ps.setInt(1, planId);
                ps.setInt(2, actionId);
                ps.setInt(3, configuration.getConfigurationId());
                ps.setString(4, configuration.getValue());

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting the configurationData in the database.", e);
        }
    }
}

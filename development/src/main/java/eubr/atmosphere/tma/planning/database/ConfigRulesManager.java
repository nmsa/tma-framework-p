package eubr.atmosphere.tma.planning.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.DatabaseManager;

public class ConfigRulesManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRulesManager.class);

    public Double searchThresholdByConfigProfileID(Integer configProfileID, Integer attributeID) {
    	
    	Double threshold = null;
    	PreparedStatement ps = null;
		String sql = "SELECT threshold FROM preference pref WHERE pref.configurationprofileId = ? and pref.attributeId = ?";

		try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, configProfileID);
            ps.setInt(2, attributeID);

            ResultSet rs = DatabaseManager.executeQuery(ps);
            if (rs.next()) {
            	threshold = rs.getDouble("threshold");
            }
            return threshold;
            
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when getting threshold from preference using configurationprofileId and attributeId.", e);
        }
		
        return null;
    }
    
    public Double searchKAnonimityByID(Date timestamp) {
    	
    	if (timestamp == null) {
    		return null;
    	}
    	
    	Double kValue = null;
    	PreparedStatement ps = null;
    	
    	Integer probeId = Integer.parseInt(PropertiesManager.getInstance().getProperty("probe.id"));
    	Integer descriptionId = Integer.parseInt(PropertiesManager.getInstance().getProperty("k.id"));
    	Integer resourceId = Integer.parseInt(PropertiesManager.getInstance().getProperty("resource.id"));
    	
		String sql = "SELECT d.value as kvalue FROM Data d WHERE d.probeId = ? and d.descriptionId = ? and d.resourceId = ? and d.valueTime = ?";

		try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setInt(1, probeId);
            ps.setInt(2, descriptionId);
            ps.setInt(3, resourceId);
            ps.setTimestamp(4, new Timestamp(timestamp.getTime()));
            
            LOGGER.info("timestamp: " + new Timestamp(timestamp.getTime()));

            ResultSet rs = DatabaseManager.executeQuery(ps);
            if (rs.next()) {
            	kValue = rs.getDouble("kvalue");
            }
            return kValue;
            
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when getting kValue from Data using probeId, descriptionId and resourceId.", e);
        }
		
        return null;
    }
    

}

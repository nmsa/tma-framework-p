package eubr.atmosphere.tma.planning.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

    private static PropertiesManager instance = null;
    private static Properties props = null;

    private PropertiesManager() {
        InputStream inputStream = PropertiesManager.class.getResourceAsStream("/environment.properties");
        props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PropertiesManager getInstance() {
        if (instance == null) {
            instance = new PropertiesManager();
        }
        return instance;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
}

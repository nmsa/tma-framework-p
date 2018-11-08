package eubr.atmosphere.tma.planning;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.planning.utils.RestServices;

public class KubernetesManager {
    
    public static int getReplicas(String statefulSetName) {
        String baseK8sAPI = PropertiesManager.getInstance().getProperty("baseK8sAPI");
        String uri = baseK8sAPI + statefulSetName;
        InputStreamReader isr;
        
        try {
            HttpResponse response = RestServices.requestRestService(uri);
            isr = new InputStreamReader(response.getEntity().getContent());
            return parseResult(isr);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private static int parseResult(InputStreamReader isr) {
        Gson gson = new GsonBuilder().create();
        Object rawJson = gson.fromJson(isr, Object.class);
        
        LinkedTreeMap<String, Object> c = (LinkedTreeMap<String, Object>) rawJson;
        LinkedTreeMap<String, Object> status = ((LinkedTreeMap<String, Object>) c.get("status"));
        Double replicas = ((Double) status.get("replicas"));
        
        return replicas.intValue();
    }
}

package eubr.atmosphere.tma.planning.api;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * This class is used to run the spring microservice exposing endpoints for rules management through tma-dashboard.
 * 
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */
@SpringBootApplication
public class RulesManagerRest {
    public static AtomicBoolean updateRuleSet;
   
    public void start(AtomicBoolean updateRuleSet) {
        RulesManagerRest.updateRuleSet = updateRuleSet;
        SpringApplication.run(RulesManagerRest.class);
    }
    
}
package eubr.atmosphere.tma.planning.api.controller;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import eubr.atmosphere.tma.planning.api.util.Constants;
import eubr.atmosphere.tma.planning.api.RulesManagerRest;
import eubr.atmosphere.tma.planning.api.dto.Configuration;
import eubr.atmosphere.tma.planning.api.dto.Action;
import eubr.atmosphere.tma.planning.api.dto.Rule;
import eubr.atmosphere.tma.planning.database.DatabaseManager;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This class is a Rest Controller. It handles every request made to the
 * server related with Adaptation Rules management and information.
 *
 * @author João Ribeiro  <jdribeiro@student.dei.uc.pt>
 */
@CrossOrigin
@RestController
public class RulesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesController.class);
    
    private static AtomicBoolean updateRuleSet = RulesManagerRest.updateRuleSet;
    
    private void notifyUpdateRules() {
        synchronized(updateRuleSet){
            updateRuleSet.set(true);
        }
    }
    
    @DeleteMapping("/removeRule/{ruleName}")
    public ResponseEntity<Map> updateRules(@PathVariable(name= "ruleName") String ruleName, HttpServletResponse response) {
        DatabaseManager db = new DatabaseManager();
        
        String currentRules = new String(db.readRules());
        String newRules = "";
        
        String ruleBegin = "rule \"" + ruleName + "\"";
        String ruleEnd = "end";
        
        try (Scanner scanner = new Scanner(currentRules)) {
            String nextLine;
            boolean textToRemove = false;
            
            while (scanner.hasNextLine()) {
                nextLine = scanner.nextLine();
                if(textToRemove){ 
                    if(nextLine.startsWith(ruleEnd)){
                        textToRemove = false;
                    }
                    //else => ignore text
                }
                else{
                    if(nextLine.startsWith(ruleBegin)){
                        textToRemove = true;
                        //it also means there was a \n before and we need to delete it
                        newRules = newRules.substring(0, newRules.length() - 1);
                    }
                    else{
                        newRules += nextLine + "\n";
                    }
                }
            }
        }
        newRules = newRules.substring(0, newRules.length() - 1);
        
        //if it wasnt successful, return internal error
        if(!db.updateRules(newRules)){
            return AdminController.genericResponseEntity(Constants.HTTPSERVERERROR, Constants.ERROR, "There was a problem with the connection to the database");
        }
        
        notifyUpdateRules();
        return AdminController.genericResponseEntity(Constants.HTTPSUCESS, Constants.SUCCESS, "Rule successfully removed! Planning will soon update its rule set.");
    }
    
    @GetMapping("/getRules")
    public ResponseEntity<Map> getRules(
            @RequestParam(required = false, defaultValue="",name = "filter") String filter,
            HttpServletResponse response) {
        ArrayList<String> rulesNamesList = getRulesNamesList(filter);
        
        HashMap<String, ArrayList<String>> rulesNamesListJson = new HashMap<>();
        rulesNamesListJson.put("rulesNames", rulesNamesList);
        
        return new ResponseEntity<>(
                rulesNamesListJson,
                HttpStatus.valueOf(response.getStatus())
        );
    }
    
    @GetMapping("/getRules/{ruleName}")
    public ResponseEntity<Map> getRule(@PathVariable(name= "ruleName") String ruleName, HttpServletResponse response) {
        DatabaseManager db = new DatabaseManager();
        
        String currentRules = new String(db.readRules());
        
        String ruleDetail = "";
        
        String ruleDeclBeginPattern = "rule \"" + ruleName + "\"";
        
        String ruleEndPattern = "end";
        
        try (Scanner scanner = new Scanner(currentRules)) {
            String nextLine;
            boolean insideRule = false;
            
            while (scanner.hasNextLine()) {
                nextLine = scanner.nextLine();
                if(insideRule){
                    if(nextLine.startsWith(ruleEndPattern)){
                        break;
                    }
                    else{
                        ruleDetail += nextLine + "\n";
                    }
                }
                else if(nextLine.startsWith(ruleDeclBeginPattern)){
                    insideRule = true;
                }
            }
        }
        ruleDetail = ruleDetail.substring(0, ruleDetail.length() - 1);
        
        HashMap<String, String> ruleDetailJson = new HashMap<>();
        ruleDetailJson.put("ruleDetail", ruleDetail);
        
        return new ResponseEntity<>(
                ruleDetailJson,
                HttpStatus.valueOf(response.getStatus())
        );
    }
    
    @PostMapping("/addRule")
    public ResponseEntity<Map> addRule(@RequestBody Rule newRule, HttpServletResponse response) {
        //if rule name already exists send error info
        if(getRulesNamesList("").contains(newRule.getRuleName())){
            LOGGER.warn("[ATMOSPHERE] Rule name given already exists");
            return AdminController.genericResponseEntity(Constants.HTTPBADREQUEST, Constants.WARNING, "Rule name already exists, please choose another name");
        }
        
        DatabaseManager db = new DatabaseManager();
        
        String newRules = new String(db.readRules());
        
        //add space between last Rule or any text in the rules file
        newRules += "\n\n";
        
        String ruleDeclaration = "rule \"%s\"\n\twhen\n\t\t";
        newRules += String.format(ruleDeclaration, newRule.getRuleName());
        
        String ruleCondition = "$score: ScoreKafka (resourceId == %d && score.get(%d) %s %4.3f )\n\tthen\n";
        newRules += String.format(Locale.US, ruleCondition, newRule.getResourceId(),
                newRule.getMetricId(),newRule.getOperator(),newRule.getActivationThreshold());
        
        
        String ruleConsequence = "\t\tList<Action> actionList = new ArrayList();\n\t\tAction action;\n\n";
        
        String actionCreationFormat = "\t\taction = new Action(%d, \"%s\", $score.getResourceId(), %d);\n";
        String actionConfigurationAdditionFormat = "\t\taction.addConfiguration(new Configuration(%d, \"%s\", \"%s\"));\n";
        String actionAddition = "\t\tactionList.add(action);\n\n";
        
        for(Action a : newRule.getActionList()){
            ruleConsequence += String.format(actionCreationFormat,
                    a.getActionId(), a.getActionName(), a.getActuatorId());
            
            if(a.getConfigurationList() != null){
                for(Configuration c: a.getConfigurationList()){
                    ruleConsequence += String.format(actionConfigurationAdditionFormat,
                            c.getConfigurationId(), c.getKeyName(), c.getValue());
                }
            }
            ruleConsequence += actionAddition;
        }
        
        newRules += ruleConsequence;
        
        //add closure of the rule
        String ruleClosure = "\t\tAdaptationManager.performAdaptation( actionList, AdaptationManager.obtainMetricData($score, %d) );\nend";
        newRules += String.format(ruleClosure,newRule.getMetricId());
        
        //if it wasnt successful, return internal error
        if(!db.updateRules(newRules)){
            return AdminController.genericResponseEntity(Constants.HTTPSERVERERROR, Constants.ERROR, "There was a problem with the connection to the database");
        }
        
        notifyUpdateRules();
        
        return AdminController.genericResponseEntity(Constants.HTTPSUCESS, Constants.SUCCESS, "Rule successfully added! Planning will soon update its rule set.");
    }
    
    private ArrayList<String> getRulesNamesList(String filter){
        DatabaseManager db = new DatabaseManager();
        
        String currentRules = new String(db.readRules());
        ArrayList<String> rulesNamesList = new ArrayList();
        
        String ruleDeclBeginPattern = "rule \"";
        //pass filter to lower case
        String filterRegex = ".*" + filter.toLowerCase() + ".*";
        
        try (Scanner scanner = new Scanner(currentRules)) {
            String nextLine;
            int foundDeclIndex;
            
            while (scanner.hasNextLine()) {
                nextLine = scanner.nextLine();
                foundDeclIndex = nextLine.indexOf(ruleDeclBeginPattern);
                if(foundDeclIndex != -1){
                    //advance 6 indexes to get to the rule name, where index 0 is the 'r' => (Ex: rule "ExampleRule" )
                    foundDeclIndex += 6;
                    String foundRule = nextLine.substring(foundDeclIndex,nextLine.indexOf("\"", foundDeclIndex)); 
                    //compare rule name with the regex, both in lower case
                    if(foundRule.toLowerCase().matches(filterRegex)){
                        rulesNamesList.add(foundRule);
                    }
                }
            }
        }
        return rulesNamesList;
    }
}

/**
 * <b>ATMOSPHERE</b> - http://www.atmosphere-eubrazil.eu/
 *** <p>
 * <b>Trustworthiness Monitoring & Assessment Framework</b>
 * Component: Admin API
 * <p>
 * Repository: https://github.com/eubr-atmosphere/tma-framework License:
 * https://github.com/eubr-atmosphere/tma-framework/blob/master/LICENSE
 * <p>
 * <p>
 */
package eubr.atmosphere.tma.planning.api.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eubr.atmosphere.tma.planning.api.util.Constants;

/**
 * This class is a Rest Controller. It handles general requests made to the
 * server.
 * <p>
 *
 * @author Paulo Goncalves  <pgoncalves@student.dei.uc.pt>
 * @author Jose A. D. Pereira  <josep@dei.uc.pt>
 * @author Rui Silva <rfsilva@student.dei.uc.pt>
 * @author Nuno Antunes     <nmsa@dei.uc.pt>
 *
 */
@CrossOrigin
@RestController
public class AdminController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    static public boolean isInputNotValid(String toBeEvaluated) {
        String forbiddenCaracters = "*;,~^´`+«»?'=)(&%$#\"!|\\<>";
        for (int i = 0; i < forbiddenCaracters.length(); i++) {
            if (toBeEvaluated.indexOf(forbiddenCaracters.charAt(i)) != -1) {
                return true;
            }
        }
        return false;
    }

    static public ResponseEntity<Map> genericResponseEntity(int status, int messageType, String message) {
        HashMap<Object, Object> json = new HashMap<>();
        switch (messageType) {
            case Constants.ERROR:
                json.put("messageType", "error");
                break;
            case Constants.WARNING:
                json.put("messageType", "warning");
                break;
            case Constants.SUCCESS:
                json.put("messageType", "success");
                break;
            default:
                json.put("messageType", "unknown");
                break;
        }
        json.put("message", message);
        return new ResponseEntity<>(json, HttpStatus.valueOf(status));
    }
    
    //Everytime there is an http error, it will be treated by this method 
    @RequestMapping("/error")
    public Map error(HttpServletRequest request, HttpServletResponse response) {
        HashMap<Object, Object> responsejson = new HashMap();

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        Integer statusCode;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
            responsejson.put("status", "" + statusCode);
        } else {
            statusCode = -1;
            responsejson.put("status", "unknown error status, please contact the support team");
        }

        switch (statusCode) {
            case Constants.HTTPURLNOTFOUND:
                responsejson.put("message", "URL not found");
                break;
            case Constants.HTTPSERVERERROR:
                responsejson.put("message", "Server side error, please try again later or contact the support to know what's going on");
                break;
            case Constants.HTTPUNSUPPORTEDMEDIATYPE:
                responsejson.put("message", "Unsupported media type, please check what you are sending");
                break;
            case Constants.HTTPBADREQUEST:
                responsejson.put("message", "Bad Request");
                break;
            case Constants.HTTPMETHODNOTALLOWED:
                responsejson.put("message", "The HTTP Method given is not allowed to this URI");
                break;
            default:
                if (message != null) {
                    responsejson.put("message", message.toString());
                } else {
                    responsejson.put("message", "unknown error, please contact the support team");
                }
        }
        return responsejson;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}

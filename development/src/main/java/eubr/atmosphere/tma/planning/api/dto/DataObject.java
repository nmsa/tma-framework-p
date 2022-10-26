/**
 * <b>ATMOSPHERE</b> - http://www.atmosphere-eubrazil.eu/
 *** 
 * <p>
 * <b>Trustworthiness Monitoring & Assessment Framework</b>
 * Component: Admin API
 * <p>
 * Repository: https://github.com/eubr-atmosphere/tma-framework License:
 * https://github.com/eubr-atmosphere/tma-framework/blob/master/LICENSE
 * <p>
 * <p>
 */
package eubr.atmosphere.tma.planning.api.dto;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This class is used in order to avoid code repetition.
 * <p>
 *
 * @author Paulo Goncalves  <pgoncalves@student.dei.uc.pt>
 * @author Jose A. D. Pereira  <josep@dei.uc.pt>
 * @author Rui Silva <rfsilva@student.dei.uc.pt>
 * @author Nuno Antunes     <nmsa@dei.uc.pt>
 *
 */
public abstract class DataObject {

    protected Exception exception = null;
    protected String errorLogger;
    protected String errorMessage;
    protected String messageType;
    protected int statusCode;

    public ResponseEntity<Map> errorHandler(Logger LOGGER) {
        if (this.exception != null) {
            LOGGER.error(this.errorLogger, this.exception);
        } else {
            LOGGER.error(this.errorLogger);
        }

        HashMap<Object, Object> json = new HashMap<>();
        json.put("status", "" + this.statusCode);
        json.put("message", this.errorMessage);
        json.put("messageType", "warning");
        return new ResponseEntity<>(json, HttpStatus.valueOf(this.statusCode));
    }
}

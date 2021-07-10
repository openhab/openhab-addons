/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tapocontrol.internal.helpers;

import static org.openhab.binding.tapocontrol.internal.helpers.TapoErrorConstants.*;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class Handling TapoErrors
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoErrorHandler {
    private Integer errorCode = 0;
    private String errorMessage = "";
    private String infoMessage = "";
    private Gson gson = new Gson();

    /**
     * GET ERROR-MESSAGE
     * 
     * @param errCode error Number (or constant ERR_CODE )
     * @return error-message if set constant ERR_CODE_MSG. if not name of ERR_CODE is returned
     */
    private String getErrorMessage(Integer errCode) {
        Field[] fields = TapoErrorConstants.class.getDeclaredFields();
        /* loop ErrorConstants and search for code in value */
        for (Field f : fields) {
            String constName = f.getName();
            try {
                Integer val = (Integer) f.get(this);
                if (val.equals(errCode)) {
                    /* get constan named by errorcode and _MSG (ERR_CODE_MSG) */
                    try {
                        String msg = TapoErrorConstants.class.getDeclaredField(constName + "_MSG").get(null).toString();
                        if (msg.length() > 2) {
                            return msg;
                        } else {
                            return constName;
                        }
                    } catch (Exception e) {
                        return constName;
                    }
                }
            } catch (Exception e) {
                // next loop
            }
        }
        return errCode.toString();
    }

    /**
     * Constructor
     *
     */
    public TapoErrorHandler() {
    }

    /**
     * Constructor
     * 
     * @param errorCode error code (number)
     */
    public TapoErrorHandler(Integer errorCode) {
        raiseError(errorCode);
    }

    /**
     * Constructor
     * 
     * @param errorCode error code (number)
     * @param infoMessage optional info-message
     */
    public TapoErrorHandler(Integer errorCode, String infoMessage) {
        raiseError(errorCode, infoMessage);
    }

    /**
     * Constructor
     * 
     * @param exception Exception
     */
    public TapoErrorHandler(Exception ex) {
        raiseError(ex);
    }

    /***********************************
     *
     * Private Functions
     *
     ************************************/
    private static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /***********************************
     *
     * Public Functions
     *
     ************************************/

    /**
     * Raises new error
     * 
     * @param errorCode error code (number)
     */
    public void raiseError(Integer errorCode) {
        raiseError(errorCode, "");
    }

    /**
     * Raises new error
     * 
     * @param errorCode error code (number)
     * @param infoMessage optional info-message
     */
    public void raiseError(Integer errorCode, String infoMessage) {
        this.errorCode = errorCode;
        this.errorMessage = getErrorMessage(errorCode);
        this.infoMessage = infoMessage;
    }

    /**
     * Raises new error
     * 
     * @param exception Exception
     */
    public void raiseError(Exception ex) {
        raiseError(ex, "");
    }

    /**
     * Raises new error
     * 
     * @param exception Exception
     * @param infoMessage optional info-message
     */
    public void raiseError(Exception ex, String infoMessage) {
        this.errorCode = ex.hashCode();
        this.errorMessage = getValueOrDefault(ex.getMessage(), ex.toString());
        this.infoMessage = infoMessage;
    }

    /**
     * Reset Error
     */
    public void reset() {
        this.errorCode = 0;
        this.errorMessage = "";
        this.infoMessage = "";
    }

    /***********************************
     *
     * GET RESULTS
     *
     ************************************/

    /**
     * Get Error Message
     * 
     * @return error text
     */
    public String getMessage() {
        return this.errorMessage;
    }

    /**
     * Get Error Message directly by error-number
     * 
     * @param errorCode
     * @return error message
     */
    public String getMessage(Integer errorCode) {
        return getErrorMessage(errorCode);
    }

    /**
     * Get Info Message
     * 
     * @return error extended info
     */
    public String getExtendedInfo() {
        return this.infoMessage;
    }

    /**
     * Get Error Number
     * 
     * @return error number
     */
    public Integer getNumber() {
        return this.errorCode;
    }

    /**
     * Get JSON-Object with errror
     * 
     * @return JsonObject with error-informations
     */
    public JsonObject getJson() {
        JsonObject json;
        json = gson.fromJson("{'error_code': '" + errorCode + "', 'error_message':'" + errorMessage + "'}",
                JsonObject.class);
        if (json == null) {
            json = new JsonObject();
        }
        return json;
    }
}

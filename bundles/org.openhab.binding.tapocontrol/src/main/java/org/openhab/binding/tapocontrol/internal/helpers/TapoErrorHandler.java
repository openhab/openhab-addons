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

import java.util.HashMap;

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
    private HashMap<Integer, String> codeMap = new HashMap<Integer, String>();

    /**
     * Set ErrorMessages
     */
    private void setErrorMessages() {
        codeMap.put(ERROR_API_KEY_LENGTH, ERROR_API_KEY_LENGTH_MSG);
        codeMap.put(ERROR_API_CREDENTIALS, ERROR_API_CREDENTIALS_MSG);
        codeMap.put(ERROR_API_REQUEST, ERROR_API_REQUEST_MSG);
        codeMap.put(ERROR_JSON_FORMAT, ERROR_JSON_FORMAT_MSG);
        codeMap.put(ERROR_RESPONSE, ERROR_RESPONSE_MSG);
        codeMap.put(ERROR_COOKIE, ERROR_COOKIE_MSG);
        codeMap.put(ERROR_LOGIN, ERROR_LOGIN_MSG);
        codeMap.put(ERROR_DEVICE_OFFLINE, ERROR_DEVICE_OFFLINE_MSG);
    }

    /**
     * Constructor
     *
     */
    public TapoErrorHandler() {
        setErrorMessages();
    }

    /**
     * Constructor
     * 
     * @param errorCode error code (number)
     */
    public TapoErrorHandler(Integer errorCode) {
        setErrorMessages();
        raiseError(errorCode);
    }

    /**
     * Constructor
     * 
     * @param errorCode error code (number)
     * @param infoMessage optional info-message
     */
    public TapoErrorHandler(Integer errorCode, String infoMessage) {
        setErrorMessages();
        raiseError(errorCode, infoMessage);
    }

    /**
     * Constructor
     * 
     * @param exception Exception
     */
    public TapoErrorHandler(Exception ex) {
        setErrorMessages();
        raiseError(ex);
    }

    /***********************************
     *
     * Private Functions
     *
     ************************************/
    private String getMessageFromMap(Integer errorCode) {
        return getValueOrDefault(codeMap.get(errorCode), errorCode.toString());
    }

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
        this.errorMessage = getMessageFromMap(errorCode);
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
     * @return
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

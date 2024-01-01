/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class Handling TapoErrors
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoErrorHandler extends Exception {
    private TapoErrorCode errorCode = TapoErrorCode.NO_ERROR;
    private static final long serialVersionUID = 0L;
    private String infoMessage = "";
    private Gson gson = new Gson();

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
     * @param ex Exception
     */
    public TapoErrorHandler(Exception ex) {
        raiseError(ex);
    }

    /**
     * Constructor
     * 
     * @param ex Exception
     * @param infoMessage optional info-message
     */
    public TapoErrorHandler(Exception ex, String infoMessage) {
        raiseError(ex, infoMessage);
    }

    /**
     * Constructor TapoErrorCodeEnum
     * 
     * @param errorCode error code (TapoErrorCodeEnum)
     */
    public TapoErrorHandler(TapoErrorCode errorCode) {
        raiseError(errorCode);
    }

    /**
     * Constructor
     * 
     * @param errorCode error code (TapoErrorCodeEnum)
     * @param infoMessage optional info-message
     */
    public TapoErrorHandler(TapoErrorCode errorCode, String infoMessage) {
        raiseError(errorCode, infoMessage);
    }

    /***********************************
     *
     * Private Functions
     *
     ************************************/

    /**
     * GET ERROR-MESSAGE
     * 
     * @param errCode error Number (or constant ERR_API_CODE )
     * @return error-message if code found in i18n, else return code
     */
    private String getErrorMessage(Integer errCode) {
        String key = TapoErrorCode.fromCode(errCode).name().replace("ERR_", "error-").replace("_", "-").toLowerCase();
        return String.format("@text/%s [ \"%s\" ]", key, errCode.toString());
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
        raiseError(TapoErrorCode.fromCode(errorCode), infoMessage);
    }

    /**
     * Raises new error
     * 
     * @param ex Exception
     */
    public void raiseError(Exception ex) {
        raiseError(ex, "");
    }

    /**
     * Raises new error
     * 
     * @param ex Exception
     * @param infoMessage optional info-message
     */
    public void raiseError(Exception ex, String infoMessage) {
        raiseError(TapoErrorCode.fromCode(ex.hashCode()), infoMessage);
    }

    /**
     * Raises new error
     * 
     * @param errorCode error code (TapoErrorCodeEnum)
     */
    public void raiseError(TapoErrorCode errorCode) {
        raiseError(errorCode, "");
    }

    /**
     * Raises new error
     * 
     * @param errorCode error code (TapoErrorCodeEnum)
     * @param infoMessage optional info-message
     */
    public void raiseError(TapoErrorCode errorCode, String infoMessage) {
        this.errorCode = errorCode;
        this.infoMessage = infoMessage;
    }

    /**
     * Take over tapoError
     * 
     * @param tapoError
     */
    public void set(TapoErrorHandler tapoError) {
        this.errorCode = TapoErrorCode.fromCode(tapoError.getCode());
        this.infoMessage = tapoError.getExtendedInfo();
    }

    /**
     * Reset Error
     */
    public void reset() {
        this.errorCode = NO_ERROR;
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
    @Override
    @Nullable
    public String getMessage() {
        return getErrorMessage(errorCode.getCode());
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
     * Get Error Code
     * 
     * @return error code (integer)
     */
    public Integer getCode() {
        return this.errorCode.getCode();
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
     * Get Error Code
     * 
     * @return error code
     */
    public TapoErrorCode getError() {
        return this.errorCode;
    }

    /**
     * Check if has Error
     * 
     * @return true if has error
     */
    public Boolean hasError() {
        return this.errorCode != NO_ERROR;
    }

    /**
     * Get JSON-Object with errror
     * 
     * @return JsonObject with error-informations
     */
    public JsonObject getJson() {
        JsonObject json;
        json = gson.fromJson(
                "{'error_code': '" + errorCode + "', 'error_message':'" + getErrorMessage(getCode()) + "'}",
                JsonObject.class);
        if (json == null) {
            json = new JsonObject();
        }
        return json;
    }
}

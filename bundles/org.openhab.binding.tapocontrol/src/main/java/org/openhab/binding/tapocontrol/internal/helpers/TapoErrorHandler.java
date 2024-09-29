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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorType;

/**
 * Class Handling TapoErrors
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoErrorHandler extends Exception {
    private static final long serialVersionUID = 0L;
    private Integer errorNumber = NO_ERROR.getCode();
    private String infoMessage = "";

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
    public TapoErrorHandler(Integer errorCode, @Nullable String infoMessage) {
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
    public TapoErrorHandler(Exception ex, @Nullable String infoMessage) {
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
    public TapoErrorHandler(TapoErrorCode errorCode, @Nullable String infoMessage) {
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
        return String.format("@text/%s [ \"%s\" ]", key, errorNumber);
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
    public void raiseError(Exception ex, @Nullable String infoMessage) {
        try {
            throw ex;
        } catch (TapoErrorHandler e) {
            raiseError(e.getCode(), e.getMessagText());
        } catch (TimeoutException e) {
            raiseError(ERR_BINDING_CONNECT_TIMEOUT, infoMessage);
        } catch (InterruptedException e) {
            raiseError(ERR_BINDING_SEND_REQUEST, infoMessage);
        } catch (ExecutionException e) {
            raiseError(ERR_BINDING_SEND_REQUEST, infoMessage);
        } catch (Exception e) {
            raiseError(e.hashCode(), infoMessage);
        }
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
    public void raiseError(TapoErrorCode errorCode, @Nullable String infoMessage) {
        raiseError(errorCode.getCode(), infoMessage);
    }

    /**
     * Raises new error
     * 
     * @param errorCode error code (number)
     * @param infoMessage optional info-message
     */
    public void raiseError(Integer errorCode, @Nullable String infoMessage) {
        errorNumber = errorCode;
        if (infoMessage != null) {
            this.infoMessage = infoMessage;
        } else {
            this.infoMessage = "";
        }
    }

    /**
     * Take over tapoError
     * 
     * @param tapoError
     */
    public void set(TapoErrorHandler tapoError) {
        errorNumber = tapoError.getCode();
        infoMessage = tapoError.getExtendedInfo();
    }

    /**
     * Reset Error
     */
    public void reset() {
        errorNumber = NO_ERROR.getCode();
        infoMessage = "";
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
        return getMessage(errorNumber);
    }

    public String getMessagText() {
        return getMessage(errorNumber);
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
        return errorNumber;
    }

    /**
     * Get Info Message
     * 
     * @return error extended info
     */
    public String getExtendedInfo() {
        return infoMessage;
    }

    /**
     * Get Error Code
     * 
     * @return error code
     */
    public TapoErrorCode getError() {
        return TapoErrorCode.fromCode(errorNumber);
    }

    /**
     * Check if has Error
     * 
     * @return true if has error
     */
    public boolean hasError() {
        return !NO_ERROR.getCode().equals(errorNumber);
    }

    public TapoErrorType getType() {
        return TapoErrorCode.fromCode(errorNumber).getType();
    }

    @Override
    public String toString() {
        return getErrorMessage(getCode());
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Meyer - Initial contribution
 */
public class RobonectAnswer {
    
    private boolean successful;

    @SerializedName("error_code")
    private Integer errorCode;
    
    @SerializedName("error_message")
    private String errorMessage;
    
    public RobonectAnswer withSuccessful(boolean successful){
        this.successful = successful;
        return this;
    }
    
    public RobonectAnswer withErrorCode(Integer errorCode){
        this.errorCode = errorCode;
        return this;
    }
    
    public RobonectAnswer withErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
        return this;
    }
    
    public boolean isSuccessful() {
        return successful;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

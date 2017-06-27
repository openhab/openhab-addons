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
public class ErrorEntry  {
    
    private String date; 
    
    @SerializedName("error_code")
    private Integer errorCode;
    
    @SerializedName("error_message")
    private String errorMessage;

    private String time;
    
    private String unix;

    public String getDate() {
        return date;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTime() {
        return time;
    }

    public String getUnix() {
        return unix;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnix(String unix) {
        this.unix = unix;
    }
}

package org.openhab.binding.robonect.model;

import com.google.gson.annotations.SerializedName;

/**
 * {"successful": false, "error_code": 7, "error_message": "Automower already stopped"}
 * {"successful": true}
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
}

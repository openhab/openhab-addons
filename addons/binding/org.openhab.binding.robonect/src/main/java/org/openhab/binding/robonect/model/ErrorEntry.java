package org.openhab.binding.robonect.model;

import com.google.gson.annotations.SerializedName;

/**
 * {
       "date": "02.05.2017", 
       "error_code": 15, 
       "error_message": "Grasi ist angehoben", 
       "time": "20:36:43", 
       "unix": 1493757403
     }
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
}

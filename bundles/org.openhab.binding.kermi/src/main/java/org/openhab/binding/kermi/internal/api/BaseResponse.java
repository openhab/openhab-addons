package org.openhab.binding.kermi.internal.api;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> {

    @SerializedName("ResponseData")
    private T responseData;

    @SerializedName("StatusCode")
    private int statusCode;

    @SerializedName("ExceptionData")
    private Object exceptionData;

    @SerializedName("DisplayText")
    private String displayText;

    @SerializedName("DetailedText")
    private String detailedText;

    public T getResponseData() {
        return responseData;
    }

    public void setResponseData(T responseData) {
        this.responseData = responseData;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getDetailedText() {
        return detailedText;
    }

    public void setDetailedText(String detailedText) {
        this.detailedText = detailedText;
    }

}

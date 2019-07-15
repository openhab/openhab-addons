package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("error_msg")
    @Expose
    private String errorMsg;

    /**
     * @return
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * @param errorMsg
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}

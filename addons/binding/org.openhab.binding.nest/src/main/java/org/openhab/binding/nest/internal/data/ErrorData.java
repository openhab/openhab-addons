package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The data of Nest API errors.
 *
 * @author Wouter Born - Improve exception handling
 */
public class ErrorData {
    @SerializedName("error")
    private String error;
    @SerializedName("type")
    private String type;
    @SerializedName("message")
    private String message;
    @SerializedName("instance")
    private String instance;

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "ErrorData [error=" + error + ", type=" + type + ", message=" + message + ", instance=" + instance + "]";
    }
}

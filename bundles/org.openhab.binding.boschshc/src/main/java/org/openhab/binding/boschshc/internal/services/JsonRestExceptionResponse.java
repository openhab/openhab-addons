package org.openhab.binding.boschshc.internal.services;

public class JsonRestExceptionResponse extends BoschSHCServiceState {
    public JsonRestExceptionResponse() {
        super("JsonRestExceptionResponseEntity");
    }

    /**
     * The error code of the occurred Exception.
     */
    public String errorCode;

    /**
     * The HTTP status of the error.
     */
    public int statusCode;
}
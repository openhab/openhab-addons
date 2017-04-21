package org.openhab.binding.antiferencematrix.internal.model;

public class Response {

    private boolean result;
    private String errorMessage;

    /**
     * Returns the error message string
     * 
     * @return The error message or null if the request was a success
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns whether the request was a success
     * 
     * @return true if the request was a success false if not
     */
    public boolean getResult() {
        return result;
    }

}

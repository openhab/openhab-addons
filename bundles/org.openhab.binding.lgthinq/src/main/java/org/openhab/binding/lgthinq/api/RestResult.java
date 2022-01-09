package org.openhab.binding.lgthinq.api;

// used to compose the result from post/get rest calls to LG EMP API
public  class RestResult {
    private String jsonResponse;
    private int resultCode;

    public RestResult(String jsonResponse, int resultCode) {
        this.jsonResponse = jsonResponse;
        this.resultCode = resultCode;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public int getStatusCode() {
        return resultCode;
    }
}

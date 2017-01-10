package org.openhab.binding.etapu.channels;

public abstract class ETAChannel {
    private String url;

    private String response;

    public void setUrl(String url) {
        this.url = url;
    }

    protected String getUrl() {
        return url;
    }

    public void setResponse(String r) {
        this.response = r;
    }

    protected String getResponse() {
        return response;
    }

    public abstract Object getValue();

}

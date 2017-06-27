package org.openhab.binding.supla.internal.server.http;

public final class Response {
    private final int statusCode;
    private final String response;

    public Response(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }

    public boolean success() {
        return statusCode == 200;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", response='" + response + '\'' +
                '}';
    }
}

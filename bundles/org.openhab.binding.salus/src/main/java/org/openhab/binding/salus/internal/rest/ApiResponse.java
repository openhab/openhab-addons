package org.openhab.binding.salus.internal.rest;

public record ApiResponse<T> (T body, Error error) {
    public static <T> ApiResponse<T> ok(T body) {
        return new ApiResponse<>(body, null);
    }

    public static <T> ApiResponse<T> error(Error error) {
        return new ApiResponse<>(null, error);
    }

    public ApiResponse {
        if (body != null && error != null) {
            throw new IllegalArgumentException("body and error cannot be both present");
        }
        if (body == null && error == null) {
            throw new IllegalArgumentException("body and error cannot be both null");
        }
    }

    public boolean succeed() {
        return body != null;
    }

    public boolean failed() {
        return !succeed();
    }
}

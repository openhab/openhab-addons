package org.openhab.binding.salus.internal.rest;

public record Error(String code, String message) {
    public Error(int code, String error) {
        this(String.valueOf(code), error);
    }
}

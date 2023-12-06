package org.openhab.binding.salusbinding.internal.rest;

public record Error(String code, String message) {
    public Error(int code, String error) {
        this(String.valueOf(code), error);
    }
}

package org.openhab.binding.smartthings.internal.dto;

public class ErrorObject {

    public String requestId;

    public class Error {
        public String code;
        public String message;

        public record Detail(String code, String target, String message) {

        }

        public Detail[] details;
    }

    public Error error;
}

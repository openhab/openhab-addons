package org.openhab.binding.lgthinq.lgservices.errors;

import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;

public class LGThinqAccessException extends LGThinqApiException {
    public LGThinqAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public LGThinqAccessException(String message, Throwable cause, ResultCodes reasonCode) {
        super(message, cause, reasonCode);
    }

    public LGThinqAccessException(String message) {
        super(message);
    }

    public LGThinqAccessException(String message, ResultCodes resultCode) {
        super(message, resultCode);
    }
}

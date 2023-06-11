package org.smslib;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class ModemResponse {
    String responseData;

    boolean responseOk;

    public ModemResponse(String responseData, boolean responseOk) {
        this.responseData = responseData;
        this.responseOk = responseOk;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public boolean isResponseOk() {
        return this.responseOk;
    }
}

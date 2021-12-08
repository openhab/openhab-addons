package org.openhab.binding.blink.internal;

import org.openhab.binding.blink.internal.dto.BlinkAccount;

public class BlinkTestUtil {

    public static BlinkAccount testBlinkAccount() {
        BlinkAccount blinkAccount = new BlinkAccount();
        blinkAccount.account = new BlinkAccount.Account();
        blinkAccount.account.account_id = 123L;
        blinkAccount.account.tier = "e006";
        blinkAccount.account.client_id = 987L;
        blinkAccount.auth = new BlinkAccount.Auth();
        blinkAccount.auth.token = "abc";
        return blinkAccount;
    }
}

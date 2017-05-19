package org.openhab.binding.evohome.internal.api;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.evohome.internal.api.handlers.AuthenticatedHandlerBase;
import org.openhab.binding.evohome.internal.api.handlers.AuthenticationHandler;
import org.openhab.binding.evohome.internal.api.models.UserAccountResponse;

public class AccountHandler extends AuthenticatedHandlerBase {
    private UserAccountResponse userAccount = null;

    public AccountHandler(AuthenticationHandler authenticationHandler) {
        super(authenticationHandler);
    }

    public void getUserAccount() {
        String url = EvohomeApiConstants.URL_BASE + EvohomeApiConstants.URL_ACCOUNT;

        UserAccountResponse userAccount =  new UserAccountResponse();
        userAccount = doRequest(HttpMethod.GET, url, null, null, userAccount);
    }
}

package org.openhab.binding.evohome.internal.api.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.evohome.internal.api.models.AuthenticationResponse;

public class AuthenticatedHandlerBase extends HandlerBase {
    private AuthenticationHandler authenticationHandler;

    public AuthenticatedHandlerBase(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    protected <TIn, TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers,
            TIn requestContainer, TOut out) {

        if (authenticationHandler != null) {
            if (headers == null) {
                headers = new HashMap<String,String>();
            }

            AuthenticationResponse auth = authenticationHandler.getAuthenticationData();
            String                appId = authenticationHandler.getApplicationId();

            headers.put("Authorization", "bearer " + auth.AccessToken);
            headers.put("applicationId", appId);
            headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
        }

        return super.doRequest(method, url, headers, requestContainer, out);
    }

}

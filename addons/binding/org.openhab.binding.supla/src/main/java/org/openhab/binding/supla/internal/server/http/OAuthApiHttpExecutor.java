package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaToken;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openhab.binding.supla.internal.server.http.CommonHeaders.authorizationHeader;

public final class OAuthApiHttpExecutor implements HttpExecutor {
    private final HttpExecutor httpExecutor;
    private final TokenManager tokenManager;

    public OAuthApiHttpExecutor(HttpExecutor httpExecutor, TokenManager tokenManager) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.tokenManager = checkNotNull(tokenManager);
    }

    @Override
    public Response get(Request request) {
        return httpExecutor.get(buildOAuthRequest(request));
    }

    private Request buildOAuthRequest(Request request) {
        final SuplaToken token = tokenManager.obtainToken();
        final List<Header> headers = new ArrayList<>(request.getHeaders());
        headers.add(authorizationHeader(token));
        final String path = "/api" + request.getPath();
        return new Request(path, headers);
    }

    @Override
    public Response post(Request request, Body body) {
        return httpExecutor.post(buildOAuthRequest(request), body);
    }

    @Override
    public Response patch(Request request, Body body) {
        return httpExecutor.patch(buildOAuthRequest(request), body);
    }

    @Override
    public void close() {
        httpExecutor.close();
    }
}

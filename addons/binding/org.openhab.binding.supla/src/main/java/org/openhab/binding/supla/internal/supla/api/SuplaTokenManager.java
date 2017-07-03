package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.server.http.*;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;
import org.openhab.binding.supla.internal.supla.entities.SuplaToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openhab.binding.supla.internal.server.http.CommonHeaders.CONTENT_TYPE_JSON;

public final class SuplaTokenManager implements TokenManager {
    private final Logger logger = LoggerFactory.getLogger(SuplaTokenManager.class);
    private final JsonMapper jsonMapper;
    private final HttpExecutor httpExecutor;
    private final SuplaCloudServer server;
    private final Body body;

    public SuplaTokenManager(JsonMapper jsonMapper, HttpExecutor httpExecutor, SuplaCloudServer server) {
        this.jsonMapper = checkNotNull(jsonMapper);
        this.httpExecutor = httpExecutor;
        this.server = checkNotNull(server);
        body = new JsonBody(ImmutableMap.<String, String>builder()
                .put("client_id", server.getClientId())
                .put("client_secret", server.getSecretAsString()).put("grant_type", "password")
                .put("username", server.getUsername())
                .put("password", server.getPasswordAsString())
                .build(),
                jsonMapper);
        checkArgument(!(httpExecutor instanceof OAuthApiHttpExecutor), "HttpExecutor cannot be OAuthApiHttpExecutor class!");
    }

    @Override
    public SuplaToken obtainToken() {
        final Response response = httpExecutor.post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body);
        if (response.success()) {
            return jsonMapper.to(SuplaToken.class, response.getResponse());
        } else {
            throw new RuntimeException("Got error " + response.getStatusCode() + " while obtaining token for server " + server + "!");
        }
    }
}

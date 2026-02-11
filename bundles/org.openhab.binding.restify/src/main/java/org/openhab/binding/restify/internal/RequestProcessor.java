package org.openhab.binding.restify.internal;

import java.io.Serial;
import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.config.Config;

public class RequestProcessor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Config config;
    private final Engine engine;

    public RequestProcessor(Config config, Engine engine) {
        this.config = config;
        this.engine = engine;
    }

    public Json.JsonObject process(Method method, String path, @Nullable String authorization)
            throws AuthorizationException, NotFoundException, ParameterException {
        var response = config.findResponse(path, method).orElseThrow(() -> new NotFoundException(path, method));
        if (response.authorization() != null) {
            authorize(response.authorization(), authorization);
        }
        return engine.evaluate(response.schema());
    }

    private void authorize(Authorization required, @Nullable String provided) throws AuthorizationException {
        if (provided == null) {
            throw new AuthorizationException("Authorization required");
        }

        switch (required) {
            case Authorization.Basic basic -> authorize(basic, provided);
            case Authorization.Bearer bearer -> authorize(bearer, provided);
        }
    }

    private void authorize(Authorization.Basic basic, String provided) throws AuthorizationException {
        var password = config.globalConfig().usernamePasswords().get(basic.username());
        if (password == null) {
            throw new AuthorizationException("There is no password configured for user: " + basic.username());
        }
        var expected = "Basic " + basic.username() + ":" + password;
        if (!provided.equals(expected)) {
            throw new AuthorizationException("Invalid username or password");
        }
    }

    private void authorize(Authorization.Bearer bearer, String provided) throws AuthorizationException {
        if (!provided.equals("Bearer " + bearer.token())) {
            throw new AuthorizationException("Invalid token");
        }
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}

package org.openhab.binding.restify.internal;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

public class RequestProcessor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // todo proper loading
    private final Map<String, Schema> getSchema = Map.of("/hello", new Schema.StringSchema("foo", null, "boo"));
    private final Engine engine;

    public RequestProcessor(Engine engine) {
        this.engine = engine;
    }

    public Response process(Method method, String path, @Nullable String authorization)
            throws AuthorizationException, NotFoundException, ParameterException {
        // todo support other methods
        var schema = getSchema.get(path);
        if (schema == null) {
            throw new NotFoundException(path);
        }
        if (schema.authorization() != null) {
            authorize(schema.authorization(), authorization);
        }
        return engine.evaluate(schema);
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
        var expected = "Basic " + basic.username() + ":" + basic.password();
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

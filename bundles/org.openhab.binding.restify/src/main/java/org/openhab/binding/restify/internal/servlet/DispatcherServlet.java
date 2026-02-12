package org.openhab.binding.restify.internal.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.*;

import java.io.IOException;
import java.io.Serial;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.config.Config;
import org.openhab.binding.restify.internal.config.ConfigWatcher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component(service = Servlet.class, property = { "osgi.http.whiteboard.servlet.pattern=/restify/*",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)" })
public class DispatcherServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class.getName());
    private final EndpointRegistry registry = new EndpointRegistry();
    private final JsonEncoder jsonEncoder;
    private final ConfigWatcher configWatcher;
    private final Engine engine;

    @Activate
    public DispatcherServlet(@Reference JsonEncoder jsonEncoder, @Reference ConfigWatcher configWatcher,
            @Reference Engine engine) {
        this.jsonEncoder = jsonEncoder;
        this.configWatcher = configWatcher;
        this.engine = engine;
        logger.info("Starting DispatcherServlet");
    }

    private void process(Method method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var uri = req.getRequestURI();
        logger.debug("Processing {}:{}", method, uri);
        try {
            var json = process(method, uri, req.getHeader("Authorization"));
            resp.setStatus(SC_OK);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonEncoder.encode(json));
        } catch (UserRequestException e) {
            respondWithError(resp, e.getStatusCode(), e);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            respondWithError(resp, SC_BAD_REQUEST, ex);
        } catch (Exception ex) {
            respondWithError(resp, SC_INTERNAL_SERVER_ERROR, ex);
        }
        resp.getWriter().close();
    }

    public Json.JsonObject process(Method method, String path, @Nullable String authorization)
            throws AuthorizationException, NotFoundException, ParameterException {
        var config = configWatcher.currentConfig();
        var response = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        if (response.authorization() != null) {
            authorize(config, response.authorization(), authorization);
        }
        return engine.evaluate(response.schema());
    }

    private void authorize(Config config, Authorization required, @Nullable String provided)
            throws AuthorizationException {
        if (provided == null) {
            throw new AuthorizationException("Authorization required");
        }

        switch (required) {
            case Authorization.Basic basic -> authorize(config, basic, provided);
            case Authorization.Bearer bearer -> authorize(bearer, provided);
        }
    }

    private void authorize(Config config, Authorization.Basic basic, String provided) throws AuthorizationException {
        var password = config.usernamePasswords().get(basic.username());
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

    private void respondWithError(HttpServletResponse resp, int statusCode, Exception e) throws IOException {
        logger.error("%s: %s".formatted(statusCode, e.getMessage()), e);
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"code\": %d, \"error\": \"%s\"}".formatted(statusCode, e.getMessage()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(GET, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(POST, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(PUT, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(DELETE, req, resp);
    }

    public void register(String path, Method method, @Nullable Response response) {
        registry.register(path, method, response);
    }

    public void unregister(String path, Method method) {
        registry.unregister(path, method);
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}

package org.openhab.binding.restify.internal;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.openhab.binding.restify.internal.RequestProcessor.Method.*;
import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.io.IOException;
import java.io.Serial;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.restify.internal.RequestProcessor.Method;
import org.openhab.binding.restify.internal.config.ConfigException;
import org.openhab.binding.restify.internal.config.ConfigWatcher;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
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
    private final RequestProcessor requestProcessor;
    private final JsonEncoder jsonEncoder;
    private final ScheduledExecutorService scheduledPool;
    private final ConfigWatcher configWatcher;

    @Activate
    public DispatcherServlet(@Reference HttpService httpService, @Reference ItemRegistry itemRegistry,
            @Reference ThingRegistry thingRegistry) throws ConfigException, IOException {
        scheduledPool = ThreadPoolManager.getScheduledPool(BINDING_ID);
        configWatcher = new ConfigWatcher(scheduledPool);
        requestProcessor = new RequestProcessor(configWatcher, new Engine(itemRegistry, thingRegistry));
        jsonEncoder = new JsonEncoder();
    }

    private void process(Method method, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var json = requestProcessor.process(method, req.getContextPath(), req.getHeader("Authorization"));
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

    @Deactivate
    public void deactivate() throws Exception {
        configWatcher.close();
        scheduledPool.close();
    }
}

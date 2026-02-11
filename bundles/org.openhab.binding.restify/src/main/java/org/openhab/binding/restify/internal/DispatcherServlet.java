package org.openhab.binding.restify.internal;

import static javax.servlet.http.HttpServletResponse.*;
import static org.openhab.binding.restify.internal.RequestProcessor.Method.*;

import java.io.IOException;
import java.io.Serial;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.restify.internal.RequestProcessor.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class.getName());
    private final RequestProcessor requestProcessor;
    private final JsonEncoder jsonEncoder;
    @Serial
    private static final long serialVersionUID = 1L;

    public DispatcherServlet(RequestProcessor requestProcessor, JsonEncoder jsonEncoder) {
        this.requestProcessor = requestProcessor;
        this.jsonEncoder = jsonEncoder;
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
}

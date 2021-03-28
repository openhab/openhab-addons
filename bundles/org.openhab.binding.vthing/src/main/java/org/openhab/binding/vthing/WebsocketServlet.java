package org.openhab.binding.vthing;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Juergen Weber - Initial contribution
 */

@WebServlet(name = "WebSocket Servlet")
public class WebsocketServlet extends WebSocketServlet {

    private final Logger logger = LoggerFactory.getLogger(WebsocketServlet.class);

    @Override
    public void configure(WebSocketServletFactory factory) {

        logger.trace("configure");
        factory.register(WebSocketHandler.class);
    }
}

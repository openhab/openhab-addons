package org.openhab.binding.isy.internal;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IsyWebSocketSubscription implements WebSocketListener {

    private Logger logger = LoggerFactory.getLogger(IsyWebSocketSubscription.class);
    private ISYModelChangeListener listener;
    private String connectUrl;
    private String authenticationHeader;
    Future<Session> future = null;

    public IsyWebSocketSubscription(String url, String authenticationHeader, ISYModelChangeListener listener) {
        this.listener = listener;
        this.connectUrl = url;
        this.authenticationHeader = authenticationHeader;
        connect();
    }

    private void connect() {

        WebSocketClient client = new WebSocketClient();
        try {

            client.start();
            URI echoUri = new URI("ws://" + this.connectUrl + "/rest/subscribe");
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("ISYSUB");
            request.setHeader("Authorization", this.authenticationHeader);
            request.setHeader("Sec-WebSocket-Version", "13");
            request.setHeader("Origin", "com.universal-devices.websockets.isy");
            Future<Session> future = client.connect(this, echoUri, request);

            future.get();
            logger.info("Connecting to :" + echoUri);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                // client.stop();
                logger.debug("in finally in IsyWebSocketSubscription");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

    }

    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onWebSocketClose(int arg0, String arg1) {
        logger.debug("Socket Closed: [" + arg0 + "] " + arg1);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("Reconnecting via Websocket to Isy.");
        if (this.future != null) {
            this.future.cancel(true);
        }
        connect();
    }

    @Override
    public void onWebSocketConnect(Session arg0) {
        logger.debug("Socket Connected: " + arg0);

    }

    @Override
    public void onWebSocketError(Throwable arg0) {
        logger.error("Error with websocket communication", arg0);

    }

    @Override
    public void onWebSocketText(String arg0) {
        // logger.debug("rest subscription: " + arg0);
        parseXml(arg0);
    }

    private void parseVariableEvent(String message) {

    }

    private void parseXml(String message) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(message));
            try {
                Document doc = builder.parse(is);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression controlExpr = xpath.compile("//Event/control");
                XPathExpression actionExpr = xpath.compile("//Event/action");
                XPathExpression nodeExpr = xpath.compile("//Event/node");
                String control = (String) controlExpr.evaluate(doc, XPathConstants.STRING);
                String action = (String) actionExpr.evaluate(doc, XPathConstants.STRING);
                String node = (String) nodeExpr.evaluate(doc, XPathConstants.STRING);
                if ("ST".equals(control)) {
                    logger.debug("status change detected");
                    logger.debug("Test: " + control);
                    logger.debug("Action: " + action);
                    logger.debug("Node: " + node);
                    listener.onModelChanged(control, action, node);
                } else if ("_1".equals(control) && "6".equals(action)) {
                    logger.debug("Possible variable event: " + message);
                    XPathExpression valueExp = xpath.compile("//Event/eventInfo/var/val");
                    String value = (String) valueExp.evaluate(doc, XPathConstants.STRING);
                    String id = (String) xpath.compile("//Event/eventInfo/var/@id").evaluate(doc,
                            XPathConstants.STRING);
                    String type = (String) xpath.compile("//Event/eventInfo/var/@type").evaluate(doc,
                            XPathConstants.STRING);
                    logger.debug("Variable with id: " + id + " type: " + type + ", value:" + value);
                    String theEvent = type + " " + id + " " + value;
                    listener.onModelChanged(control, action, theEvent);
                }
            } catch (SAXException | IOException e) {
                logger.error("parse exception", e);
            }

        } catch (ParserConfigurationException | XPathExpressionException e) {
            logger.error("parse exception", e);
        }

    }

}
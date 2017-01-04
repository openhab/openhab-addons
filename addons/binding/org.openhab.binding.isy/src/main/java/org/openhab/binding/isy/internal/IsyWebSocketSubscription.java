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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IsyWebSocketSubscription implements WebSocketListener {

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
            System.out.printf("Connecting to : %s%n", echoUri);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                // client.stop();
                System.out.println("fit finally in IsyWebSocketSubscription");
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
        System.err.println("Socket Closed: [" + arg0 + "] " + arg1);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Reconnecting");
        if (this.future != null) {
            this.future.cancel(true);
        }
        connect();
    }

    @Override
    public void onWebSocketConnect(Session arg0) {
        System.out.println("Socket Connected: " + arg0);

    }

    @Override
    public void onWebSocketError(Throwable arg0) {
        // TODO Auto-generated method stub
        System.err.println("FOUND ERROR, " + arg0);

    }

    @Override
    public void onWebSocketText(String arg0) {
        parseXml(arg0);
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
                    System.out.println("status change detected");
                    System.out.println("Test: " + control);
                    System.out.println("Action: " + action);
                    System.out.println("Node: " + node);
                    listener.onModelChanged(control, action, node);
                }
            } catch (SAXException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (ParserConfigurationException | XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
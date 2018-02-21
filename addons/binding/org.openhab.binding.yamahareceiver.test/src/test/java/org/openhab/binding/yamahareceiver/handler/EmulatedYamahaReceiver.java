/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Emulates a Yamaha Receiver XML API endpoint (Http/XML on localhost:12121/YamahaRemoteControl/ctrl)
 *
 * @author David Graeff - Initial contribution
 */
public class EmulatedYamahaReceiver implements Runnable {
    public final static String XML_END = "</YAMAHA_AV>";

    private Logger logger = LoggerFactory.getLogger(EmulatedYamahaReceiver.class);
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private final int port;
    final CountDownLatch loadingDone = new CountDownLatch(1);
    private Thread listenThread;

    public String lastPut;
    private ServerSocket serverSocket;

    /**
     * Starts a listener thread on the given port
     *
     * @param port
     */
    public EmulatedYamahaReceiver(int port) {
        this.port = port;
        listenThread = new Thread(this);
        listenThread.start();
    }

    /**
     * Stops the listener thread
     */
    public void destroy() {
        if (listenThread == null) {
            return;
        }

        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }

        try {
            listenThread.join(500);
            listenThread.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        serverSocket = null;
        listenThread = null;
    }

    protected OutputStream connectionAccepted(Socket socket) throws IOException, ParserConfigurationException,
            SAXException, ParseException, TransformerFactoryConfigurationError, TransformerException {
        InputStream is = socket.getInputStream();
        String request = ParserUtils.parseRequestURL(is);
        if (request == null || !request.equals("/YamahaRemoteControl/ctrl")) {
            return null;
        }
        int len = Integer.valueOf(ParserUtils.parseHTTPHeaders(is).get("Content-Length"));
        String bodyXML = ParserUtils.parseBodyXML(is, len);
        logger.debug("Received request: {}", (bodyXML.contains("GET") ? bodyXML : "PUT"));

        OutputStream outputStream = socket.getOutputStream();
        String responseXML = "";
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(bodyXML)));
        String nodeName = doc.getFirstChild().hasChildNodes() == false ? null : doc.getFirstChild().getNodeName();
        if (nodeName == null || !nodeName.equals("YAMAHA_AV")) {
            logger.debug("Could not handle response");
        } else {
            // We are not sending back a valid xml document. The
            // header "<?xml version=\"1.0\" encoding=\"utf-8\"?>" is missing for Yamaha AVR responses.
            responseXML = parse(doc.getFirstChild());
            String httpResponse = "HTTP/1.1 200 OK\r\nContent-type: text/xml\r\nContent-length: "
                    + String.valueOf(responseXML.length()) + "\r\n\r\n" + responseXML;
            outputStream.write(httpResponse.getBytes("UTF-8"));
            outputStream.flush();
        }
        return outputStream;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;
            loadingDone.countDown();
            OutputStream outputStream = null;
            try (Socket socket = server.accept()) {
                outputStream = connectionAccepted(socket);
            } catch (ParseException | TransformerFactoryConfigurationError | TransformerException | SAXException
                    | ParserConfigurationException e) {
                logger.debug("Could not handle response!", e);
                String httpResponse = "HTTP/1.1 404 OK\r\nContent-type:text/xml\r\n\r\n";
                if (outputStream != null) {
                    outputStream.write(httpResponse.getBytes("UTF-8"));
                    outputStream.flush();
                }
            } catch (IOException e) {
                logger.debug("Client connection failed!", e);
            }
        } catch (IOException e) {
            logger.debug("Emulated Yamaha device server failed!", e);
        }
        serverSocket = null;
    }

    public boolean waitForStarted(long timeout_ms) throws InterruptedException {
        return loadingDone.await(timeout_ms, TimeUnit.MILLISECONDS);
    }

    private String parse(Node firstChild)
            throws ParseException, TransformerFactoryConfigurationError, TransformerException {
        String value = firstChild.getAttributes().getNamedItem("cmd").getNodeValue();
        switch (value) {
            case "GET":
                return "<YAMAHA_AV rsp=\"GET\" RC=\"0\">" + parseGET(firstChild.getFirstChild()) + XML_END;
            case "PUT":
                return "<YAMAHA_AV rsp=\"PUT\" RC=\"0\">" + parsePUT(firstChild.getFirstChild()) + XML_END;
            default:
                throw new ParseException("Test parser expected GET/PUT for cmd attribute", 0);
        }
    }

    private String parsePUT(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        lastPut = writer.toString();
        return "";
    }

    private static String parseGET(Node firstChild) throws ParseException {
        switch (firstChild.getNodeName()) {
            case "System": {
                Node child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for system/ only supports GetParam", 0);
                }
                switch (child.getNodeName()) {
                    case "Config":
                        return "<System><Config><Model_Name>Test AVR</Model_Name><System_ID>1234</System_ID><Version>1.0</Version><Feature_Existence><Main_Zone>1</Main_Zone><Zone_2>1</Zone_2><Zone_3>0</Zone_3></Feature_Existence></Config></System>";
                    case "Power_Control":
                        return "<System><Power_Control><Power>On</Power></Power_Control></System>";
                    default:
                        throw new ParseException("Node for system/ not supported", 0);
                }
            }
            case "NET_RADIO":
                Node child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for Play_Info/ only supports GetParam", 0);
                }
                if (child.getNodeName().equals("Play_Info")) {
                    return "<NET_RADIO>" + playInfo() + "</NET_RADIO>";
                } else if (child.getNodeName().equals("List_Info")) {
                    return "<NET_RADIO>" + listInfo() + "</NET_RADIO>";
                } else if (child.getNodeName().equals("Play_Control")) {
                    return "<NET_RADIO>" + playControl() + "</NET_RADIO>";
                } else {
                    throw new ParseException("Node for NET_RADIO only supports Play_Info and List_Info", 0);
                }
            case "Main_Zone":
                return "<Main_Zone>" + parseZone(firstChild.getFirstChild()) + "</Main_Zone>";
            case "Zone_2":
                return "<Zone_2>" + parseZone(firstChild.getFirstChild()) + "</Zone_2>";
            default:
                throw new ParseException("Node for GET not supported", 0);
        }
    }

    private static String playControl() {
        // Play_Control/Preset/Preset_Sel
        return "<Play_Control><Preset><Preset_Sel>1</Preset_Sel></Preset></Play_Control>";
    }

    private static String playInfo() {
        return "<Play_Info><Playback_Info>Play</Playback_Info><Meta_Info><Station>TestStation</Station><Artist>TestArtist</Artist><Album>TestAlbum</Album><Song>TestSong</Song></Meta_Info></Play_Info>";
    }

    private static String listInfo() {
        return "<List_Info><Menu_Status>Ready</Menu_Status><Menu_Name>Testname</Menu_Name><Menu_Layer>2</Menu_Layer><Cursor_Position><Current_Line>1</Current_Line><Max_Line>1</Max_Line></Cursor_Position><Current_List><Line_1><Txt>Eintrag1</Txt></Line_1></Current_List></List_Info>";
    }

    private static String parseZone(Node firstChild) throws ParseException {
        String nodeName = firstChild.getNodeName();
        Node child;
        switch (nodeName) {
            case "Basic_Status":
                child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for Basic_Status/ only supports GetParam", 0);
                }
                return "<Basic_Status><Power_Control><Power>On</Power></Power_Control><Input><Input_Sel>Net Radio</Input_Sel><Input_Sel_Item_Info><Src_Name>NET_RADIO</Src_Name></Input_Sel_Item_Info></Input><Surround><Program_Sel><Current><Sound_Program>7ch Stereo</Sound_Program></Current></Program_Sel></Surround><Volume><Mute>Off</Mute><Lvl><Val>100</Val></Lvl></Volume></Basic_Status>";
            case "Input":
                firstChild = firstChild.getFirstChild();
                if (firstChild.getNodeName().equals("Input_Sel_Item")) {
                    child = firstChild.getFirstChild();
                    if (!child.getTextContent().equals("GetParam")) {
                        throw new ParseException("Node for Input/Input_Sel_Item/ only supports GetParam", 0);
                    }
                    return "<Input><Input_Sel_Item><Item1><Param>HDMI 1</Param><RW>RW</RW></Item1><Item2><Param>Net Radio</Param><RW>RW</RW></Item2></Input_Sel_Item></Input>";
                }
                break;
            case "List_Info":
                child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for List_Info/ only supports GetParam", 0);
                }
                return listInfo();
            case "Play_Info":
                child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for Play_Info/ only supports GetParam", 0);
                }
                return playInfo();
            case "Play_Control":
                child = firstChild.getFirstChild();
                if (!child.getTextContent().equals("GetParam")) {
                    throw new ParseException("Node for Play_Control/ only supports GetParam", 0);
                }
                return playControl();
        }

        throw new ParseException("Node for zone not supported", 0);
    }

    public int getPort() {
        return port;
    }
}

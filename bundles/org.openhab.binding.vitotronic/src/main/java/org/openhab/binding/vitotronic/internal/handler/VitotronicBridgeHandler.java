/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vitotronic.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.vitotronic.internal.VitotronicBindingConfiguration;
import org.openhab.binding.vitotronic.internal.discovery.VitotronicDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The {@link VitotronicBridgeHandler} class handles the connection to the
 * optolink adapter.
 *
 * @author Stefan Andres - Initial contribution
 */
public class VitotronicBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(VitotronicBridgeHandler.class);

    private String ipAddress;
    private int port;
    private int refreshInterval = 300;
    private Socket socket;
    private PrintStream out;
    private InputStream inStream;
    private boolean isConnect = false;
    private boolean isDiscover = false;

    public VitotronicBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateThingHandlersStatus(status);
    }

    public void updateStatus() {
        if (isConnect) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    // Managing Thing Discovery Service

    private VitotronicDiscoveryService discoveryService = null;

    public void registerDiscoveryService(VitotronicDiscoveryService discoveryService) {
        if (discoveryService == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingDiscoveryListener.");
        } else {
            this.discoveryService = discoveryService;
            logger.trace("register Discovery Service");
        }
    }

    public void unregisterDiscoveryService() {
        discoveryService = null;
        logger.trace("unregister Discovery Service");
    }

    // Handles Thing discovery

    private void createThing(String thingType, String thingID) {
        logger.trace("Create thing Type='{}' id='{}'", thingType, thingID);
        if (discoveryService != null) {
            discoveryService.addVitotronicThing(thingType, thingID);
        }
    }

    // Managing ThingHandler

    private Map<String, VitotronicThingHandler> thingHandlerMap = new HashMap<>();

    public void registerVitotronicThingListener(VitotronicThingHandler thingHandler) {
        if (thingHandler == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingHandler.");
        } else {
            String thingID = thingHandler.getThing().getUID().getId();
            if (thingHandlerMap.get(thingID) == null) {
                thingHandlerMap.put(thingID, thingHandler);
                logger.trace("register thingHandler for thing: {}", thingID);
                updateThingHandlerStatus(thingHandler, this.getStatus());
                sendSocketData("get " + thingID);
            } else {
                logger.trace("thingHandler for thing: '{}' already registered", thingID);
            }
        }
    }

    public void unregisterThingListener(VitotronicThingHandler thingHandler) {
        if (thingHandler != null) {
            String thingID = thingHandler.getThing().getUID().getId();
            if (thingHandlerMap.remove(thingID) == null) {
                logger.trace("thingHandler for thing: {} not registered", thingID);
            }
        }
    }

    private void updateThingHandlerStatus(VitotronicThingHandler thingHandler, ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    private void updateThingHandlersStatus(ThingStatus status) {
        for (Map.Entry<String, VitotronicThingHandler> entry : thingHandlerMap.entrySet()) {
            updateThingHandlerStatus(entry.getValue(), status);
        }
    }

    // Background Runables

    private ScheduledFuture<?> pollingJob;

    private Runnable pollingRunnable = () -> {
        logger.trace("Polling job called");
        if (!isConnect) {
            startSocketReceiver();
            try {
                Thread.sleep(5000); // Wait for connection .
            } catch (InterruptedException e) {
            }
        }
        if (isConnect) {
            scanThings();
            refreshData();
        }
    };

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void refreshData() {
        logger.trace("Job: refresh Data...");
        for (Map.Entry<String, VitotronicThingHandler> entry : thingHandlerMap.entrySet()) {
            String channelList = entry.getValue().getActiveChannelListAsString();
            String thingId = entry.getValue().getThing().getUID().getId();
            if (isConnect && (channelList.length() > 0)) {
                logger.trace("Get Data for '{}'", thingId);
                sendSocketData("get " + thingId + " " + channelList);
            }
        }
    }

    // Methods for ThingHandler

    public void scanThings() {
        logger.trace("Job: Discover Things...");
        if (!isDiscover) {
            sendSocketData("list");
            isDiscover = true;
        }
    }

    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    public void updateChannel(String thingId, String channelId, String value) {
        sendSocketData("set " + thingId + ":" + channelId + " " + value);
    }

    // internal Methods

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels - nothing to do
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vitotronic bridge handler {}", getThing().getUID());
        updateStatus();
        VitotronicBindingConfiguration configuration = getConfigAs(VitotronicBindingConfiguration.class);
        ipAddress = configuration.ipAddress;
        port = configuration.port;
        refreshInterval = configuration.refreshInterval;

        isDiscover = false;
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Vitotronic bridge handler {}", getThing().getUID());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    // Connection to adapter

    private void openSocket() {
        logger.trace("Try to open connection to Optolink Adapter {}:{}", ipAddress, port);

        try {
            socket = new Socket(ipAddress, port);
            out = new PrintStream(socket.getOutputStream());
            inStream = socket.getInputStream();
        } catch (UnknownHostException e) {
            logger.error("Can't find Host: {}:{}", ipAddress, port);
        } catch (IOException e) {
            logger.debug("Error in communication to Host: {}:{}", ipAddress, port);
            logger.trace("Diagnostic: ", e);
        }
    }

    Runnable socketReceiverRunnable = () -> {
        logger.trace("Start Background Thread for recieving data from adapter");
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            xmlReader.setContentHandler(new XmlHandler());
            logger.trace("Start Parser for optolink adapter");
            xmlReader.parse(new InputSource(inStream));

        } catch (IOException e) {
            logger.trace("Connection error from optolink adapter");
        } catch (SAXException e) {
            logger.trace("XML Parser Error");

        }
        updateStatus(ThingStatus.OFFLINE);
        isConnect = false;
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
        logger.trace("Connection to optolink adapter is died ... wait for restart");
    };

    private void startSocketReceiver() {
        if (!isConnect) {
            openSocket();

            Thread thread = new Thread(socketReceiverRunnable);
            thread.setName("VitotronicSocketThread");
            thread.start();
        }
    }

    private void sendSocketData(String message) {
        try {
            logger.trace("Send Message {}", message);
            if (isConnect) {
                if (message.matches("^set.*REFRESH$")) {
                    String[] msgParts = message.split(" ");
                    String[] thingChannel = msgParts[1].split(":");
                    message = "get " + thingChannel[0] + " " + thingChannel[1];
                }
                out.write((message + "\n").getBytes());
            }
        } catch (IOException e) {
            logger.error("Error in sending data to optolink adapter");
            logger.trace("Diagnostic: ", e);
        }
    }

    // Handles all data what received from optolink adapter

    public class XmlHandler implements ContentHandler {
        boolean isData;
        boolean isDefine;
        boolean isThing;
        boolean isChannel;
        boolean isDescription;
        String thingID;
        String thingType;
        String channelID;
        String description;
        VitotronicThingHandler thingHandler;
        Set<String> channels = new HashSet<>();

        @Override
        public void startElement(String uri, String localName, String pName, Attributes attr) throws SAXException {
            try {
                switch (localName) {
                    case "optolink":
                        isConnect = true;
                        updateStatus(ThingStatus.ONLINE);
                        break;
                    case "data":
                        isDefine = false;
                        break;
                    case "define":
                        isDefine = true;
                        break;
                    case "description":
                        isDescription = true;
                        break;
                    case "thing":
                        isThing = true;
                        if (isDefine) {
                            thingType = attr.getValue("type");
                        }
                        thingID = attr.getValue("id");
                        channels.clear();
                        thingHandler = thingHandlerMap.get(thingID);
                        break;
                    case "channel":
                        isChannel = true;
                        channelID = attr.getValue("id");
                        if (isDefine) {
                            channels.add(channelID);
                        } else { // is data
                            if (thingHandler != null) {
                                logger.trace("Set Data for channel '{}' value '{}'", channelID, attr.getValue("value"));
                                thingHandler.setChannelValue(channelID, attr.getValue("value"));
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                logger.error("Error in parsing data");
                logger.trace("Diagnostic: ", e);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isDescription) {
                description = new String(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
                case "description":
                    isDescription = false;
                    break;
                case "thing":
                    if (isDefine) {
                        createThing(thingType, thingID);
                    }
                    isThing = false;
                    thingHandler = null;
                    break;
                case "channel":
                    isChannel = false;
                    break;
            }
        }

        // Unused function of xmlReader
        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        }

        @Override
        public void processingInstruction(String arg0, String arg1) throws SAXException {
        }

        @Override
        public void setDocumentLocator(Locator arg0) {
        }

        @Override
        public void skippedEntity(String arg0) throws SAXException {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String arg0, String arg1) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }
}

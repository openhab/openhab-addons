/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitotronic.handler;

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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.vitotronic.VitotronicBindingConfiguration;
import org.openhab.binding.vitotronic.internal.discovery.VitotronicDiscoveryService;
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

    private Logger logger = LoggerFactory.getLogger(VitotronicBridgeHandler.class);

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
        if (discoveryService != null)
            discoveryService.addVitotronicThing(thingType, thingID);
    }

    // Managing ThingHandler

    private Map<String, VitotronicThingHandler> thingHandlerMap = new HashMap<String, VitotronicThingHandler>();

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
                logger.trace("thingHandler for thing: '{}' allready registerd", thingID);
            }

        }
    }

    public void unregisterThingListener(VitotronicThingHandler thingHandler) {
        if (thingHandler != null) {
            String thingID = thingHandler.getThing().getUID().getId();
            if (thingHandlerMap.remove(thingID) == null) {
                logger.trace("thingHandler for thing: {} not registered", thingID);
            } else {
                updateThingHandlerStatus(thingHandler, ThingStatus.OFFLINE);
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

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
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

        }

    };

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void refreshData() {
        logger.trace("Job: refresh Data...");
        String thingId;
        String channelList;
        for (Map.Entry<String, VitotronicThingHandler> entry : thingHandlerMap.entrySet()) {
            channelList = entry.getValue().getActiveChannelListAsString();
            thingId = entry.getValue().getThing().getUID().getId();
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
        logger.debug("Initializing Vitotronic bridge handler {}", this.toString());
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
        logger.debug("Dispose Vitottronic bridge handler{}", this.toString());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        updateStatus(ThingStatus.OFFLINE); // Set all State to offline
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

    Runnable socketReceiverRunnable = new Runnable() {

        @Override
        public void run() {
            logger.trace("Start Background Thread for recieving data from adapter");
            try {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(new xmlHandler());
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
                if (!socket.isClosed())
                    socket.close();
            } catch (Exception e) {
            }
            logger.trace("Connection to optolink adapter is died ... wait for restart");
        }

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
            if (isConnect)
                out.write((message + "\n").getBytes());
        } catch (IOException e) {
            logger.error("Error in sending data to optolink addapter");
            logger.trace("Diagnostic: ", e);
        }

    }

    // Handles all data what received from optolink adapter

    public class xmlHandler implements ContentHandler {

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
        Set<String> channels = new HashSet<String>();

        @Override
        public void startElement(String uri, String localName, String pName, Attributes attr) throws SAXException {
            try {

                // logger.trace("StartElement: {}", localName);
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
                        ;
                        break;
                    case "thing":
                        isThing = true;
                        if (isDefine)
                            thingType = attr.getValue("type");
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
            // logger.trace("StartElement: {}", localName);
            switch (localName) {
                case "description":
                    isDescription = false;
                    ;
                    break;
                case "thing":
                    if (isDefine)
                        createThing(thingType, thingID);
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

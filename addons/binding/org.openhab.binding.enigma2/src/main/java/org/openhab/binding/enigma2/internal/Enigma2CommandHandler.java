/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.enigma2.internal.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@link Enigma2CommandHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2CommandHandler {
    // Source found on
    // https://dream.reichholf.net/wiki/Enigma2:WebInterface#RemoteControl

    private Logger logger = LoggerFactory.getLogger(Enigma2CommandHandler.class);

    private static final String SUFFIX_REMOTE_CONTROL = "/web/remotecontrol?command=";
    private static final String SUFFIX_VOLUME = "/web/vol";
    private static final String SUFFIX_VOLUME_SET = "/web/vol?set=set";
    private static final String SUFFIX_ZAP = "/web/zap?sRef=";
    private static final String SUFFIX_CHANNEL = "/web/subservices";
    private static final String SUFFIX_POWERSTATE = "/web/powerstate";
    private static final String SUFFIX_DOWNMIX = "/web/downmix";

    private static final String SUFFIX_EPG = "/web/epgservice?sRef=";

    private static final int timeout = 5000;

    // private Map<String, String> mapOfServices = new HashMap<>();
    private Enigma2ServiceContainer serviceContainer = new Enigma2ServiceContainer();
    // private Map<String, Integer> mapOfServicesName = new HashMap<>();

    private String hostName;
    private String userName;
    private String password;

    public Enigma2CommandHandler(String hostName, String userName, String password) {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Toggles on and off
     */
    public void togglePowerState(Command command) {
        if (command instanceof OnOffType) {
            try {
                String com = createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE + "?newstate="
                        + Enigma2PowerState.TOGGLE_STANDBY.getValue();
                HttpUtil.executeUrl("GET", com, timeout);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets the volume
     * either an int value
     * or an IncreaseDecreaseType
     */
    public void setVolume(Command command) {
        // up or down one step
        if (command instanceof IncreaseDecreaseType) {
            sendRcCommand(((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE ? Enigma2RemoteKey.VOLUME_UP
                    : Enigma2RemoteKey.VOLUME_DOWN);
        } else if (command instanceof DecimalType) {
            // set absolute value
            int value = ((DecimalType) command).intValue();
            try {
                HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_VOLUME_SET + value, timeout);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type");
        }
    }

    /**
     * Toggles mute
     */
    public void setMute(Command command) {
        if (command instanceof OnOffType) {
            if (isMuted() && command == OnOffType.ON) {
                sendRcCommand(Enigma2RemoteKey.MUTE);
            }
            if (!isMuted() && command == OnOffType.ON) {
                sendRcCommand(Enigma2RemoteKey.MUTE);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets downmix
     */
    public void setDownmix(Command command) {
        if (command instanceof OnOffType) {
            String enable = (OnOffType) command == OnOffType.ON ? "True" : "False";
            try {
                HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_DOWNMIX + "?enable=" + enable,
                        timeout);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets play command
     * either PlayPauseType
     * or NextPreviousType
     */
    public void setPlayControl(Command command) {
        if (command instanceof NextPreviousType) {
            NextPreviousType type = (NextPreviousType) command;
            if (type == NextPreviousType.NEXT) {
                sendRcCommand(Enigma2RemoteKey.CHANNEL_UP);
            }
            if (type == NextPreviousType.PREVIOUS) {
                sendRcCommand(Enigma2RemoteKey.CHANNEL_DOWN);
            }
        } else if (command instanceof StringType) {
            String cmd = command.toString();
            if (cmd.equals("NEXT")) {
                sendRcCommand(Enigma2RemoteKey.CHANNEL_UP);
            } else if (cmd.equals("PREVIOUS")) {
                sendRcCommand(Enigma2RemoteKey.CHANNEL_DOWN);
            } else if (cmd.equals("0")) {
                sendRcCommand(Enigma2RemoteKey.KEY0);
            } else {
                logger.warn("Invalid command type: " + command.getClass() + ": " + command);
            }
        } else {
            logger.warn("Invalid command type: " + command.getClass() + ": " + command);
        }
        command = null;
    }

    /**
     * Sets the channel number
     * either a StringType
     * or an IncreaseDecreaseType
     */
    public void setChannel(Command command) {
        if (command instanceof StringType) {
            if (command.toString().toLowerCase().equals("0")) {
                sendRcCommand(Enigma2RemoteKey.KEY0);
            }
            String servicereference = serviceContainer.get(command.toString());
            if ((servicereference == null) || (servicereference.length() == 0)) {
                logger.error("Can not find Channel {}", command.toString());
            } else {
                try {
                    HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_ZAP + servicereference,
                            timeout);
                } catch (IOException e) {
                    logger.error("Error during send Command: {}", e);
                }
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Requests, whether the device is on or off
     *
     * @return OnOffType
     */
    public State getPowerState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE,
                    timeout);
            content = XmlUtil.getContentOfElement(content, "e2instandby");
            State returnState = content.contains("true") ? OnOffType.OFF : OnOffType.ON;
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the current value of the volume
     *
     * @return StringType(vol/e2current)
     */
    public State getVolumeState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_VOLUME, timeout);
            content = XmlUtil.getContentOfElement(content, "e2current");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the current channel name
     *
     * @return StringType(channel/e2servicename)
     */
    public State getChannelState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = XmlUtil.getContentOfElement(content, "e2servicename");
            content = Enigma2ServiceContainer.cleanString(content);
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests, whether the device is muted or unmuted
     *
     * @return OnOffType
     */
    public State getMutedState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_VOLUME, timeout);
            content = XmlUtil.getContentOfElement(content, "e2ismuted");
            State returnState = content.toLowerCase().equals("true") ? OnOffType.ON : OnOffType.OFF;
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests, if downmix is active
     *
     * @return OnOffType
     */
    public State isDownmixActiveState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_DOWNMIX, timeout);
            content = XmlUtil.getContentOfElement(content, "e2state");
            State returnState = content.toLowerCase().equals("true") ? OnOffType.ON : OnOffType.OFF;
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the now playing title
     *
     * @return StringType(e2eventtitle)
     */
    public State getNowPlayingTitle() {
        try {
            String content = HttpUtil.executeUrl("GET",
                    createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference(), timeout);
            content = XmlUtil.getContentOfElement(content, "e2eventtitle");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the now playing description
     *
     * @return StringType(e2eventdescription)
     */
    public State getNowPlayingDescription() {
        try {
            String content = HttpUtil.executeUrl("GET",
                    createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference(), timeout);
            content = XmlUtil.getContentOfElement(content, "e2eventdescription");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the now playing description extended
     *
     * @return StringType(e2eventdescriptionExtended)
     */
    public State getNowPlayingDescriptionExtended() {
        try {
            String content = HttpUtil.executeUrl("GET",
                    createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference(), timeout);
            content = XmlUtil.getContentOfElement(content, "e2eventdescriptionextended");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Scans "http://enigma2/web/getallservices" and generates map
     */
    public void generateServiceMaps() {
        try {
            File inputFile = new File("services.xml");
            BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile));

            String content = HttpUtil.executeUrl("GET", "http://192.168.0.166/web/getallservices", 5000);
            writer.write(content);
            writer.close();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList listOfBouquets = doc.getElementsByTagName("e2bouquet");
            for (int bouquetIndex = 0; bouquetIndex < listOfBouquets.getLength(); bouquetIndex++) {
                NodeList listOfServices = doc.getElementsByTagName("e2servicelist");
                for (int serviceIndex = 0; serviceIndex < listOfServices.getLength(); serviceIndex++) {
                    NodeList serviceList = doc.getElementsByTagName("e2service");
                    for (int i = 0; i < serviceList.getLength(); i++) {
                        Node service = serviceList.item(i);
                        if (service.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) service;
                            String e2servicereference = eElement.getElementsByTagName("e2servicereference").item(0)
                                    .getTextContent();
                            String e2servicename = eElement.getElementsByTagName("e2servicename").item(0)
                                    .getTextContent();

                            serviceContainer.add(e2servicename, e2servicereference);
                        }
                    }
                }
            }
            inputFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests the current channel ServiceReference
     *
     * @return StringType(channel/e2servicereference)
     */
    private String getChannelServiceReference() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = XmlUtil.getContentOfElement(content, "e2servicereference");
            return content;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    private boolean isMuted() {
        return (getMutedState() == OnOffType.ON);
    }

    private String createUserPasswordHostnamePrefix() {
        String returnString;
        if ((userName == null) || (userName.length() == 0)) {
            returnString = new StringBuffer("http://" + hostName).toString();
        } else {
            returnString = new StringBuffer("http://" + userName).append(":").append(password).append("@")
                    .append(hostName).toString();
        }
        return returnString;
    }

    /**
     * Sends any custom rc command
     */
    private void sendRcCommand(Enigma2RemoteKey commandValue) {
        if (commandValue == null) {
            logger.error("Error in item configuration. No remote control code provided (third part of item config)");
        } else {
            sendRcCommand(commandValue.getValue());
        }
    }

    private void sendRcCommand(int key) {
        try {
            HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_REMOTE_CONTROL + key, timeout);
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
    }

    public void sendRemoteKey(Command command) {
        if (command instanceof StringType) {
            try {
                int key = Integer.parseInt(command.toString());
                sendRcCommand(key);
            } catch (Exception e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

}

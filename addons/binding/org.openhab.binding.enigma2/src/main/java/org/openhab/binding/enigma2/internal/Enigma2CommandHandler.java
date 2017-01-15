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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.enigma2.handler.Enigma2Handler;
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

    private Logger logger = LoggerFactory.getLogger(Enigma2CommandHandler.class);

    private static final String SUFFIX_REMOTE_CONTROL = "/web/remotecontrol?command=";
    private static final String SUFFIX_VOLUME = "/web/vol";
    private static final String SUFFIX_VOLUME_SET = "/web/vol?set=set";
    private static final String SUFFIX_CHANNEL = "/web/subservices";
    private static final String SUFFIX_POWERSTATE = "/web/powerstate";
    private static final String SUFFIX_DOWNMIX = "/web/downmix";

    private static final String SUFFIX_EPG = "/web/epgservice?sRef=";

    private static final int timeout = 5000;

    private Map<String, String> mapOfServices = new HashMap<>();
    private Map<String, Integer> mapOfServicesName = new HashMap<>();

    private Enigma2Handler handler;
    private Thing thing;

    public Enigma2CommandHandler(Enigma2Handler handler) {
        this.handler = handler;
        thing = handler.getThing();
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
                logger.error(thing + ": Error during send Command: ", e);
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
                logger.error(thing + ": Error during send Command: ", e);
            }
        } else {
            logger.error("Unsupported command type");
        }
    }

    /**
     * Sets the channel number
     * either an int value
     * or an IncreaseDecreaseType
     */
    public void setChannelNumber(Command command) {
        if (command instanceof IncreaseDecreaseType) {
            sendRcCommand(((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE
                    ? Enigma2RemoteKey.CHANNEL_UP : Enigma2RemoteKey.CHANNEL_DOWN);
        } else if (command instanceof StringType) {
            String cmd = command.toString();
            Enigma2RemoteKey[] keyMap = convertToKeyMap(cmd);
            for (int i = 0; i < keyMap.length; i++) {
                sendRcCommand(keyMap[i]);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
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
                logger.error(thing + ": Error during send Command: ", e);
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
        if (command instanceof PlayPauseType) {
            PlayPauseType type = (PlayPauseType) command;
            if (type == PlayPauseType.PLAY) {
                sendRcCommand(Enigma2RemoteKey.PLAY);
            }
            if (type == PlayPauseType.PAUSE) {
                sendRcCommand(Enigma2RemoteKey.PAUSE);
            }
        } else if (command instanceof NextPreviousType) {
            NextPreviousType type = (NextPreviousType) command;
            if (type == NextPreviousType.NEXT) {
                sendRcCommand(Enigma2RemoteKey.ARROW_RIGHT);
            }
            if (type == NextPreviousType.PREVIOUS) {
                sendRcCommand(Enigma2RemoteKey.ARROW_LEFT);
            }
        } else if (command instanceof StringType) {
            String cmd = command.toString();
            if (cmd.equals("PLAY")) {
                sendRcCommand(Enigma2RemoteKey.PLAY);
            }
            if (cmd.equals("PAUSE")) {
                sendRcCommand(Enigma2RemoteKey.PAUSE);
            }
            if (cmd.equals("NEXT")) {
                sendRcCommand(Enigma2RemoteKey.ARROW_RIGHT);
            }
            if (cmd.equals("PREVIOUS")) {
                sendRcCommand(Enigma2RemoteKey.ARROW_LEFT);
            }
        } else {
            logger.warn("Invalid command type: " + command.getClass() + ": " + command);
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
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * Requests the current channel name
     *
     * @return StringType(channel/e2servicename)
     */
    public State getChannelNameState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = XmlUtil.getContentOfElement(content, "e2servicename");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * Requests the current channel ServiceReference
     *
     * @return StringType(channel/e2servicereference)
     */
    public String getChannelServiceReference() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = XmlUtil.getContentOfElement(content, "e2servicereference");
            return content;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * Requests the current channel number
     *
     * @return StringType(map(channel/e2servicereference))
     */
    public State getChannelNumberState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = XmlUtil.getContentOfElement(content, "e2servicename");
            Integer number = mapOfServicesName.get(content);
            if (number != null) {
                String asdf = number.toString();
                State returnState = new StringType(asdf);
                return returnState;
            }
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
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
            logger.error(thing + ": Error during send Command: ", e);
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
                            int number = mapOfServices.size() + 1;
                            String e2servicereference = eElement.getElementsByTagName("e2servicereference").item(0)
                                    .getTextContent();
                            String e2servicename = eElement.getElementsByTagName("e2servicename").item(0)
                                    .getTextContent();

                            if (mapOfServices.get(e2servicename) == null) {
                                mapOfServices.put(e2servicename, e2servicereference);
                                mapOfServicesName.put(e2servicename, number);
                            }
                        }
                    }
                }
            }
            inputFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMuted() {
        return (getMutedState() == OnOffType.ON);
    }

    private String createUserPasswordHostnamePrefix() {
        String returnString;
        if ((handler.getUserName() == null) || (handler.getUserName().length() == 0)) {
            returnString = new StringBuffer("http://" + handler.getHostName()).toString();
        } else {
            returnString = new StringBuffer("http://" + handler.getUserName()).append(":").append(handler.getPassword())
                    .append("@").append(handler.getHostName()).toString();
        }
        return returnString;
    }

    private Enigma2RemoteKey[] convertToKeyMap(String str) {
        Enigma2RemoteKey[] returnArray = new Enigma2RemoteKey[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                returnArray[i] = Enigma2RemoteKey.KEY1;
            }
            if (str.charAt(i) == '2') {
                returnArray[i] = Enigma2RemoteKey.KEY2;
            }
            if (str.charAt(i) == '3') {
                returnArray[i] = Enigma2RemoteKey.KEY3;
            }
            if (str.charAt(i) == '4') {
                returnArray[i] = Enigma2RemoteKey.KEY4;
            }
            if (str.charAt(i) == '5') {
                returnArray[i] = Enigma2RemoteKey.KEY5;
            }
            if (str.charAt(i) == '6') {
                returnArray[i] = Enigma2RemoteKey.KEY6;
            }
            if (str.charAt(i) == '7') {
                returnArray[i] = Enigma2RemoteKey.KEY7;
            }
            if (str.charAt(i) == '8') {
                returnArray[i] = Enigma2RemoteKey.KEY8;
            }
            if (str.charAt(i) == '9') {
                returnArray[i] = Enigma2RemoteKey.KEY9;
            }
            if (str.charAt(i) == '0') {
                returnArray[i] = Enigma2RemoteKey.KEY0;
            }
        }
        returnArray[str.length()] = Enigma2RemoteKey.OK;
        return returnArray;
    }

    /**
     * Sends any custom rc command
     */
    private void sendRcCommand(Enigma2RemoteKey commandValue) {
        if (commandValue == null) {
            logger.error("Error in item configuration. No remote control code provided (third part of item config)");
        }
        try {
            if (commandValue != null) {
                HttpUtil.executeUrl("GET",
                        createUserPasswordHostnamePrefix() + SUFFIX_REMOTE_CONTROL + commandValue.toString(), timeout);
            } else {
                logger.error(thing + ": Error during send null-Command: ");
            }
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
    }

}

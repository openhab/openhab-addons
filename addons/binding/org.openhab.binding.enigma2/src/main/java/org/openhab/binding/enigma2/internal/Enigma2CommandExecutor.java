/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enigma2.internal.tool.Enigma2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The {@link Enigma2CommandExecutor} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2CommandExecutor {
    // Source found on
    // https://dream.reichholf.net/wiki/Enigma2:WebInterface#RemoteControl

    private Logger logger = LoggerFactory.getLogger(Enigma2CommandExecutor.class);

    private static final String SUFFIX_REMOTE_CONTROL = "/web/remotecontrol?command=";
    private static final String SUFFIX_VOLUME = "/web/vol";
    private static final String SUFFIX_VOLUME_SET = "/web/vol?set=set";
    private static final String SUFFIX_ZAP = "/web/zap?sRef=";
    private static final String SUFFIX_CHANNEL = "/web/subservices";
    private static final String SUFFIX_POWERSTATE = "/web/powerstate";
    private static final String SUFFIX_DOWNMIX = "/web/downmix";
    private static final String SUFFIX_MESSAGE = "/web/message?type=1&TIMEOUT=10&text=";
    private static final String SUFFIX_WARNING = "/web/message?type=2&TIMEOUT=30&text=";
    private static final String SUFFIX_QUESTION = "/web/message?type=0&text=";
    private static final String SUFFIX_ANSWER = "/web/messageanswer?getanswer=now";
    private static final String SUFFIX_EPG = "/web/epgservice?sRef=";

    private Enigma2ServiceContainer serviceContainer;

    private String hostName;
    private String userName;
    private String password;

    public Enigma2CommandExecutor(String hostName, String userName, String password) {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
    }

    public void initialize() {
        try {
            serviceContainer = Enigma2Util.generateServiceMaps();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("Error during initialization: {}", e);
        }
    }

    /**
     * Sets the PowerState
     *
     * @param command, OnOffType
     */
    public void setPowerState(Command command) {
        if (command instanceof OnOffType) {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE + "?newstate="
                    + Enigma2PowerState.TOGGLE_STANDBY.getValue();
            try {
                OnOffType currentState = (OnOffType) getPowerState();
                OnOffType onOffType = (OnOffType) command;
                if (currentState == OnOffType.OFF && onOffType == OnOffType.ON) {
                    Enigma2Util.executeUrl(url);
                }
                if (currentState == OnOffType.ON && onOffType == OnOffType.OFF) {
                    Enigma2Util.executeUrl(url);
                }
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets the volume
     *
     * @param command, IncreaseDecreaseType or DecimalType
     */
    public void setVolume(Command command) {
        if (command instanceof IncreaseDecreaseType) {
            sendRcCommand(((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE ? Enigma2RemoteKey.VOLUME_UP
                    : Enigma2RemoteKey.VOLUME_DOWN);
        } else if (command instanceof DecimalType) {
            int value = ((DecimalType) command).intValue();
            try {
                String url = createUserPasswordHostnamePrefix() + SUFFIX_VOLUME_SET + value;
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type");
        }
    }

    /**
     * Sets mute
     *
     * @param command, OnOffType
     */
    public void setMute(Command command) {
        if (command instanceof OnOffType) {
            OnOffType currentState = (OnOffType) getMutedState();
            OnOffType onOffType = (OnOffType) command;
            if (currentState == OnOffType.OFF && onOffType == OnOffType.ON) {
                sendRcCommand(Enigma2RemoteKey.MUTE);
            }
            if (currentState == OnOffType.ON && onOffType == OnOffType.OFF) {
                sendRcCommand(Enigma2RemoteKey.MUTE);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets downmix
     *
     * @param command, OnOffType
     */
    public void setDownmix(Command command) {
        if (command instanceof OnOffType) {
            String enable = (OnOffType) command == OnOffType.ON ? "True" : "False";
            try {
                String url = createUserPasswordHostnamePrefix() + SUFFIX_DOWNMIX + "?enable=" + enable;
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sets PlayControl
     *
     * @param command, NextPreviousType or StringType
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
     * Sets Channel
     *
     * @param command, IncreaseDecreaseType
     */
    public void setChannel(Command command) {
        if (command instanceof StringType) {
            String servicereference = serviceContainer.get(command.toString());
            if ((servicereference == null) || (servicereference.length() == 0)) {
                logger.error("Can not find Channel {}", command.toString());
            } else {
                try {
                    String url = createUserPasswordHostnamePrefix() + SUFFIX_ZAP + servicereference;
                    Enigma2Util.executeUrl(url);
                } catch (IOException e) {
                    logger.error("Error during send Command: {}", e);
                }
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Sends RemoteKey
     *
     * @param command, StringType
     */
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

    /**
     * Sends Message
     *
     * @param command, StringType
     */
    public void sendMessage(Command command) {
        if (command instanceof StringType) {
            try {
                String url = createUserPasswordHostnamePrefix() + SUFFIX_MESSAGE + command.toString();
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        }
    }

    /**
     * Sends Warning
     *
     * @param command, StringType
     */
    public void sendWarning(Command command) {
        if (command instanceof StringType) {
            try {
                String url = createUserPasswordHostnamePrefix() + SUFFIX_WARNING + command.toString();
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        }
    }

    /**
     * Sends Question
     *
     * @param command, StringType
     */
    public void sendQuestion(Command command) {
        if (command instanceof StringType) {
            try {
                String url = createUserPasswordHostnamePrefix() + SUFFIX_QUESTION + command.toString();
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.error("Error during send Command: {}", e);
            }
        }
    }

    /**
     * Requests, whether the device is on or off
     *
     * @return OnOffType
     */
    public State getPowerState() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2instandby");
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
     * @return StringType
     */
    public State getVolumeState() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_VOLUME;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2current");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the current channel
     *
     * @return StringType
     */
    public State getChannelState() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2servicename");
            content = Enigma2ServiceContainer.cleanString(content);
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    /**
     * Requests the last answer of a question
     *
     * @return OnOffType
     */
    public State getAnswerState() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_ANSWER;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2statetext");
            State returnState = null;
            if ((content.toLowerCase().contains("yes") || content.toLowerCase().contains("ja"))) {
                returnState = OnOffType.ON;
            }
            if ((content.toLowerCase().contains("no") || content.toLowerCase().contains("nein"))) {
                returnState = OnOffType.OFF;
            }
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
            String url = createUserPasswordHostnamePrefix() + SUFFIX_VOLUME;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2ismuted");
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
            String url = createUserPasswordHostnamePrefix() + SUFFIX_DOWNMIX;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2state");
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
     * @return StringType
     */
    public State getNowPlayingTitle() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2eventtitle");
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
     * @return StringType
     */
    public State getNowPlayingDescription() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2eventdescription");
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
     * @return StringType
     */
    public State getNowPlayingDescriptionExtended() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2eventdescriptionextended");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
    }

    private String getChannelServiceReference() {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfElement(content, "e2servicereference");
            return content;
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
        return null;
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

    private void sendRcCommand(Enigma2RemoteKey commandValue) {
        if (commandValue == null) {
            logger.error("Error in item configuration. No remote control code provided (third part of item config)");
        } else {
            sendRcCommand(commandValue.getValue());
        }
    }

    private void sendRcCommand(int key) {
        try {
            String url = createUserPasswordHostnamePrefix() + SUFFIX_REMOTE_CONTROL + key;
            Enigma2Util.executeUrl(url);
        } catch (IOException e) {
            logger.error("Error during send Command: {}", e);
        }
    }
}

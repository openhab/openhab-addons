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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enigma2.handler.Enigma2Handler;
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

    private static final String SUFFIX_POWERSTATE = "/web/powerstate";
    private static final String SUFFIX_VOLUME = "/web/vol";
    private static final String SUFFIX_SET_VOLUME = "/web/vol?set=set";
    private static final String SUFFIX_ZAP = "/web/zap?sRef=";
    private static final String SUFFIX_CHANNEL = "/web/subservices";
    private static final String SUFFIX_EPG = "/web/epgservice?sRef=";
    private static final String SUFFIX_TOGGLE_POWERSTATE = "/web/powerstate?newstate=0";
    private static final String SUFFIX_MESSAGE = "/web/message?type=1&TIMEOUT=10&text=";
    private static final String SUFFIX_WARNING = "/web/message?type=2&TIMEOUT=30&text=";
    private static final String SUFFIX_QUESTION = "/web/message?type=0&text=";
    private static final String SUFFIX_ANSWER = "/web/messageanswer?getanswer=now";

    private Enigma2ServiceContainer serviceContainer;
    private String deviceURL;

    private Enigma2Handler handler;

    public Enigma2CommandExecutor(Enigma2Handler handler) {
        this.handler = handler;
        this.initialize();
    }

    /**
     * Sets the PowerState
     *
     * @param command, OnOffType
     */
    public void setPowerState(OnOffType command) {
        String url = deviceURL + SUFFIX_TOGGLE_POWERSTATE;
        try {
            OnOffType currentState = (OnOffType) getPowerState();
            OnOffType newState = command;
            if (currentState != newState) {
                Enigma2Util.executeUrl(url);
            }
        } catch (IOException e) {
            logger.warn("Error during send Command");
        }
    }

    /**
     * Sets the volume
     *
     * @param command, PercentType
     */
    public void setVolume(PercentType command) {
        int value = ((DecimalType) command).intValue();
        try {
            String url = deviceURL + SUFFIX_SET_VOLUME + value;
            Enigma2Util.executeUrl(url);
        } catch (IOException e) {
            logger.warn("Error during send Command");
        }
    }

    /**
     * Sets the volume
     *
     * @param command, IncreaseDecreaseType
     */
    public void setVolume(IncreaseDecreaseType command) {
        sendRcCommand(
                (command) == IncreaseDecreaseType.INCREASE ? Enigma2RemoteKey.VOLUME_UP : Enigma2RemoteKey.VOLUME_DOWN);
    }

    /**
     * Sets mute
     *
     * @param command, OnOffType
     */
    public void setMute(OnOffType command) {
        OnOffType currentState = (OnOffType) getMutedState();
        OnOffType newState = command;
        if (currentState != newState) {
            sendRcCommand(Enigma2RemoteKey.MUTE);
        }
    }

    /**
     * Sets PlayControl
     *
     * @param command, PlayPauseType
     */
    public void setPlayControl(PlayPauseType command) {
        PlayPauseType type = command;
        if (type == PlayPauseType.PLAY) {
            sendRcCommand(Enigma2RemoteKey.PLAY);
        }
        if (type == PlayPauseType.PAUSE) {
            sendRcCommand(Enigma2RemoteKey.PAUSE);
        }
    }

    /**
     * Sets PlayControl
     *
     * @param command, NextPreviousType
     */
    public void setPlayControl(NextPreviousType command) {
        NextPreviousType type = command;
        if (type == NextPreviousType.NEXT) {
            sendRcCommand(Enigma2RemoteKey.CHANNEL_UP);
        }
        if (type == NextPreviousType.PREVIOUS) {
            sendRcCommand(Enigma2RemoteKey.CHANNEL_DOWN);
        }
    }

    /**
     * Sets Channel
     *
     * @param command, StringType
     */
    public void setChannel(StringType command) {
        String servicereference = serviceContainer.get(command.toString());
        if ((servicereference == null) || (servicereference.length() == 0)) {
            logger.warn("Can not find Channel {}", command.toString());
        } else {
            try {
                String url = deviceURL + SUFFIX_ZAP + servicereference;
                Enigma2Util.executeUrl(url);
            } catch (IOException e) {
                logger.warn("Error during send Command");
            }
        }
    }

    /**
     * Sends RemoteKey
     *
     * @param command, StringType
     */
    public void sendRemoteKey(DecimalType command) {
        sendRcCommand(command.intValue());
    }

    /**
     * Sends Message
     *
     * @param command, StringType
     */
    public void sendMessage(StringType command) {
        try {
            String url = deviceURL + SUFFIX_MESSAGE + command.toString();
            Enigma2Util.executeUrl(url);
        } catch (IOException e) {
            logger.warn("Error during send Command");
        }
    }

    /**
     * Sends Warning
     *
     * @param command, StringType
     */
    public void sendWarning(StringType command) {
        try {
            String url = deviceURL + SUFFIX_WARNING + command.toString();
            Enigma2Util.executeUrl(url);
        } catch (IOException e) {
            logger.warn("Error during send Command");
        }
    }

    /**
     * Sends Question
     *
     * @param command, StringType
     */
    public void sendQuestion(StringType command) {
        try {
            String url = deviceURL + SUFFIX_QUESTION + command.toString();
            Enigma2Util.executeUrl(url);
        } catch (IOException e) {
            logger.warn("Error during send Command");
        }
    }

    /**
     * Requests, whether the device is on or off
     *
     * @return OnOffType
     */
    public State getPowerState() {
        try {
            String url = deviceURL + SUFFIX_POWERSTATE;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2instandby");
            State returnState = content.contains("true") ? OnOffType.OFF : OnOffType.ON;
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the current value of the volume
     *
     * @return StringType
     */
    public State getVolumeState() {
        try {
            String url = deviceURL + SUFFIX_VOLUME;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2current");
            State returnState = new PercentType(content);
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the current channel
     *
     * @return StringType
     */
    public State getChannelState() {
        try {
            String url = deviceURL + SUFFIX_CHANNEL;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2servicename");
            content = Enigma2Util.cleanString(content);
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the last answer of a question
     *
     * @return OnOffType
     */
    public State getAnswerState() {
        try {
            String url = deviceURL + SUFFIX_ANSWER;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2statetext");
            State returnState = null;
            if ((content.toLowerCase().contains("yes") || content.toLowerCase().contains("ja"))) {
                returnState = OnOffType.ON;
            }
            if ((content.toLowerCase().contains("no") || content.toLowerCase().contains("nein"))) {
                returnState = OnOffType.OFF;
            }
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests, whether the device is muted or unmuted
     *
     * @return OnOffType
     */
    public State getMutedState() {
        try {
            String url = deviceURL + SUFFIX_VOLUME;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2ismuted");
            State returnState = content.toLowerCase().equals("true") ? OnOffType.ON : OnOffType.OFF;
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the now playing title
     *
     * @return StringType
     */
    public State getNowPlayingTitle() {
        try {
            String url = deviceURL + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2eventtitle");
            State returnState = new StringType("-");
            if (content != null) {
                returnState = new StringType(content);
            }
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the now playing description
     *
     * @return StringType
     */
    public State getNowPlayingDescription() {
        try {
            String url = deviceURL + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2eventdescription");
            State returnState = new StringType("-");
            if (content != null) {
                returnState = new StringType(content);
            }
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Requests the now playing description extended
     *
     * @return StringType
     */
    public State getNowPlayingDescriptionExtended() {
        try {
            String url = deviceURL + SUFFIX_EPG + getChannelServiceReference();
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2eventdescriptionextended");
            State returnState = new StringType("-");
            if (content != null) {
                returnState = new StringType(content);
            }
            return returnState;
        } catch (IOException e) {
            return UnDefType.UNDEF;
        }
    }

    private void initialize() {
        this.deviceURL = Enigma2Util.createUserPasswordHostnamePrefix(handler.getHostName(), handler.getUserName(),
                handler.getPassword());
        try {
            serviceContainer = Enigma2Util.generateServiceMaps(deviceURL);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("Error during initialization");
            handler.setOffline();
        }
    }

    private String getChannelServiceReference() {
        try {
            String url = deviceURL + SUFFIX_CHANNEL;
            String content = Enigma2Util.executeUrl(url);
            content = Enigma2Util.getContentOfFirstElement(content, "e2servicereference");
            String[] temp = content.split(" ");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < temp.length - 1; i++) {
                sb.append(temp[i]);
                sb.append("%20");
            }
            sb.append(temp[temp.length - 1]);
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private void sendRcCommand(Enigma2RemoteKey commandValue) {
        if (commandValue != null) {
            sendRcCommand(commandValue.getValue());
        }
    }

    private boolean sendRcCommand(int key) {
        try {
            String url = deviceURL + SUFFIX_REMOTE_CONTROL + key;
            Enigma2Util.executeUrl(url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

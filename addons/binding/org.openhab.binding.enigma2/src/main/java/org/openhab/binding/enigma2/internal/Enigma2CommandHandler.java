package org.openhab.binding.enigma2.internal;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.enigma2.handler.Enigma2Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enigma2CommandHandler {

    private Logger logger = LoggerFactory.getLogger(Enigma2CommandHandler.class);

    private static final String SUFFIX_REMOTE_CONTROL = "/web/remotecontrol?command=";
    private static final String SUFFIX_VOLUME = "/web/vol";
    private static final String SUFFIX_VOLUME_SET = "?set=set";
    private static final String SUFFIX_CHANNEL = "/web/subservices";
    private static final String SUFFIX_POWERSTATE = "/web/powerstate";
    private static final String SUFFIX_DOWNMIX = "/web/downmix";
    private static final String RC_CHANNEL_UP = "402";
    private static final String RC_CHANNEL_DOWN = "403";
    private static final String RC_VOLUME_DOWN = "114";
    private static final String RC_VOLUME_UP = "115";
    private static final String RC_PLAY_PAUSE = "164";
    private static final String RC_MUTE_UNMUTE = "113";
    private static final String RC_OK = "352";

    private static final String RC_KEY1 = "2";
    private static final String RC_KEY2 = "3";
    private static final String RC_KEY3 = "4";
    private static final String RC_KEY4 = "5";
    private static final String RC_KEY5 = "6";
    private static final String RC_KEY6 = "7";
    private static final String RC_KEY7 = "8";
    private static final String RC_KEY8 = "9";
    private static final String RC_KEY9 = "10";
    private static final String RC_KEY0 = "11";

    private static final int timeout = 5000;

    private Enigma2Handler handler;
    private Thing thing;

    public Enigma2CommandHandler(Enigma2Handler handler) {
        this.handler = handler;
        thing = handler.getThing();
    }

    /**
     * @return requests the current value the volume
     */
    public State getVolume() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_VOLUME, timeout);
            content = getContentOfElement(content, "e2current");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * @return requests the current channel name
     */
    public State getChannelName() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_CHANNEL, timeout);
            content = getContentOfElement(content, "e2servicename");
            State returnState = new StringType(content);
            return returnState;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * @return requests the current channel number
     */
    public State getChannelNumber() {
        // TODO find out the channel number
        State returnState = new StringType("-1");
        return returnState;
    }

    /**
     * Requests, whether the device is on or off
     *
     * @return <code>true</code>, if the device is on, else <code>false</code>
     */
    public State getPowerState() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE,
                    timeout);
            content = getContentOfElement(content, "e2instandby");
            State returnState = content.equals("true") ? OnOffType.OFF : OnOffType.ON;
            return returnState;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /**
     * Requests, whether the device is muted or unmuted
     *
     * @return <code>true</code>, if the device is muted, else
     *         <code>false</code>
     */
    public State isMuted() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_VOLUME, timeout);
            content = getContentOfElement(content, "e2ismuted");
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
     * @return <code>true</code>, if dowmix is active
     *         <code>false</code>
     */
    public State isDownmixActive() {
        try {
            String content = HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_DOWNMIX, timeout);
            content = getContentOfElement(content, "e2state");
            State returnState = content.toLowerCase().equals("true") ? OnOffType.ON : OnOffType.OFF;
            return returnState;
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
        return null;
    }

    /*
     * Setter
     */
    /**
     * Sets the volume
     */
    public void setVolume(Command command) {
        // up or down one step
        if (command instanceof IncreaseDecreaseType) {
            sendRcCommand(
                    ((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE ? RC_VOLUME_UP : RC_VOLUME_DOWN);
        } else if (command instanceof DecimalType) {
            // set absolute value
            int value = ((DecimalType) command).intValue();
            try {
                HttpUtil.executeUrl("GET",
                        createUserPasswordHostnamePrefix() + SUFFIX_VOLUME + SUFFIX_VOLUME_SET + value, timeout);
            } catch (IOException e) {
                logger.error(thing + ": Error during send Command: ", e);
            }
        } else {
            logger.error("Unsupported command type");
        }
    }

    /**
     * Sets the channel
     */
    public void setChannel(Command command) {
        if (command instanceof StringType) {
            String cmd = command.toString();
            String[] keyMap = convertToKeyMap(cmd);
            for (int i = 0; i < keyMap.length; i++) {
                sendRcCommand(keyMap[i]);
            }
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Toggles play and pause
     */
    public void sendPlayPause(Command command) {
        if (command instanceof OnOffType) {
            sendRcCommand(RC_PLAY_PAUSE);
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Toggles mute and unmute
     */
    public void sendMuteUnmute(Command command) {
        if (command instanceof OnOffType) {
            sendRcCommand(RC_MUTE_UNMUTE);
        } else {
            logger.error("Unsupported command type: {}", command.getClass().getName());
        }
    }

    /**
     * Toggles on and off
     */
    public void sendOnOff(Command command, Enigma2PowerState powerState) {
        if (command instanceof OnOffType) {
            try {
                String com = createUserPasswordHostnamePrefix() + SUFFIX_POWERSTATE + "?newstate="
                        + powerState.getValue();
                HttpUtil.executeUrl("GET", com, timeout);
            } catch (IOException e) {
                logger.error(thing + ": Error during send Command: ", e);
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
     * Sends any custom rc command
     */
    public void sendRcCommand(String commandValue) {
        if (commandValue == null) {
            logger.error("Error in item configuration. No remote control code provided (third part of item config)");
        }
        try {
            HttpUtil.executeUrl("GET", createUserPasswordHostnamePrefix() + SUFFIX_REMOTE_CONTROL + commandValue,
                    timeout);
        } catch (IOException e) {
            logger.error(thing + ": Error during send Command: ", e);
        }
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

    /**
     * Processes an string containing xml and returning the content of a
     * specific tag (alyways lowercase)
     */
    public static String getContentOfElement(String content, String element) {

        final String beginTag = "<" + element + ">";
        final String endTag = "</" + element + ">";

        final int startIndex = content.indexOf(beginTag) + beginTag.length();
        final int endIndex = content.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return content.substring(startIndex, endIndex);
        } else {
            return null;
        }
    }

    private String[] convertToKeyMap(String str) {
        String[] returnArray = new String[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                returnArray[i] = RC_KEY1;
            }
            if (str.charAt(i) == '2') {
                returnArray[i] = RC_KEY2;
            }
            if (str.charAt(i) == '3') {
                returnArray[i] = RC_KEY3;
            }
            if (str.charAt(i) == '4') {
                returnArray[i] = RC_KEY4;
            }
            if (str.charAt(i) == '5') {
                returnArray[i] = RC_KEY5;
            }
            if (str.charAt(i) == '6') {
                returnArray[i] = RC_KEY6;
            }
            if (str.charAt(i) == '7') {
                returnArray[i] = RC_KEY7;
            }
            if (str.charAt(i) == '8') {
                returnArray[i] = RC_KEY8;
            }
            if (str.charAt(i) == '9') {
                returnArray[i] = RC_KEY9;
            }
            if (str.charAt(i) == '0') {
                returnArray[i] = RC_KEY0;
            }
        }
        returnArray[str.length()] = RC_OK;
        return returnArray;
    }

}

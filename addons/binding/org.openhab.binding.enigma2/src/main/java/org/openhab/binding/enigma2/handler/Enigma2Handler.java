/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enigma2.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.Enigma2CommandExecutor;
import org.openhab.binding.enigma2.internal.Enigma2CommandExecutorListener;
import org.openhab.binding.enigma2.internal.Enigma2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2Handler extends BaseThingHandler implements Enigma2CommandExecutorListener {
    private Logger logger = LoggerFactory.getLogger(Enigma2Handler.class);

    private Enigma2CommandExecutor commandExecutor;
    private Enigma2Refresher refresher;

    private Map<String, String> stateMap;

    public Enigma2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        stateMap = new HashMap<>();
        commandExecutor = new Enigma2CommandExecutor(
                Enigma2Util.createUserPasswordHostnamePrefix(getHostName(), getUserName(), getPassword()));

        refresher = new Enigma2Refresher();
        refresher.addListener(this);
        refresher.start();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand() called, channelUID={}, command={}", channelUID, command);
        if (command instanceof RefreshType) {
            // getUpdate();
        } else {
            if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_POWER).getUID())) {
                commandExecutor.setPowerState(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_VOLUME).getUID())) {
                commandExecutor.setVolume(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_CHANNEL).getUID())) {
                commandExecutor.setChannel(command);
                refresher.callListener();
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_MUTE).getUID())) {
                commandExecutor.setMute(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_DOWNMIX).getUID())) {
                commandExecutor.setDownmix(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_PLAYER_CONTROL).getUID())) {
                commandExecutor.setPlayControl(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_REMOTE_KEY).getUID())) {
                commandExecutor.sendRemoteKey(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_SEND_MESSAGE).getUID())) {
                commandExecutor.sendMessage(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_SEND_WARNING).getUID())) {
                commandExecutor.sendWarning(command);
            } else if (channelUID.equals(thing.getChannel(Enigma2BindingConstants.CHANNEL_SEND_QUESTION).getUID())) {
                commandExecutor.sendQuestion(command);
            }
        }
    }

    @Override
    public void dispose() {
        refresher.removeListener(this);
    }

    @Override
    public void getUpdate() {
        OnOffType powerState = (OnOffType) commandExecutor.getPowerState();
        if (powerState != null) {
            updateState(Enigma2BindingConstants.CHANNEL_POWER, powerState);
            if (powerState == OnOffType.ON) {
                if (commandExecutor.getVolumeState() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_VOLUME, commandExecutor.getVolumeState());
                }
                if (commandExecutor.getMutedState() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_MUTE, commandExecutor.getMutedState());
                }
                if (commandExecutor.isDownmix() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_DOWNMIX, commandExecutor.isDownmix());
                }
                if (commandExecutor.getChannelState() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_CHANNEL, commandExecutor.getChannelState());
                }
                if (commandExecutor.getAnswerState() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_GET_ANSWER, commandExecutor.getAnswerState());
                }
                if (commandExecutor.getNowPlayingTitle() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_TITLE,
                            commandExecutor.getNowPlayingTitle());
                }
                if (commandExecutor.getNowPlayingDescription() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION,
                            commandExecutor.getNowPlayingDescription());
                }
                if (commandExecutor.getNowPlayingDescriptionExtended() != null) {
                    updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION_EXTENDED,
                            commandExecutor.getNowPlayingDescriptionExtended());
                }
            } else {
                updateState(Enigma2BindingConstants.CHANNEL_CHANNEL, new StringType(""));
                updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_TITLE, new StringType("-"));
                updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION, new StringType("-"));
                updateState(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION_EXTENDED, new StringType("-"));
            }
        }
    }

    @Override
    protected void updateState(String channelID, State state) {
        if (state != null) {
            String newState = state.toString();
            String oldState = stateMap.get(channelID);
            if (oldState == null) {
                stateMap.put(channelID, newState);
                super.updateState(channelID, state);
            } else {
                if (!newState.equals(oldState)) {
                    super.updateState(channelID, state);
                }
            }
        }
    }

    public String getUserName() {
        return (String) thing.getConfiguration().get(Enigma2BindingConstants.DEVICE_PARAMETER_USER);
    }

    public String getPassword() {
        return (String) thing.getConfiguration().get(Enigma2BindingConstants.DEVICE_PARAMETER_PASSWORD);
    }

    public String getHostName() {
        return (String) thing.getConfiguration().get(Enigma2BindingConstants.DEVICE_PARAMETER_HOST);
    }

    public int getRefreshInterval() {
        int returnVal;
        try {
            returnVal = Integer
                    .parseInt((String) thing.getConfiguration().get(Enigma2BindingConstants.DEVICE_PARAMETER_REFRESH));
        } catch (Exception e) {
            returnVal = 5000;
        }
        return returnVal;
    }

    private class Enigma2Refresher extends Thread {
        private ArrayList<Enigma2CommandExecutorListener> listOfListener;

        public Enigma2Refresher() {
            listOfListener = new ArrayList<Enigma2CommandExecutorListener>();
        }

        public void removeListener(Enigma2CommandExecutorListener listener) {
            for (int i = 0; i < listOfListener.size(); i++) {
                if (listOfListener.get(i) == listener) {
                    listOfListener.remove(i);
                }
            }
            listOfListener.add(listener);
        }

        public void addListener(Enigma2CommandExecutorListener listener) {
            listOfListener.add(listener);
        }

        public void callListener() {
            for (int i = 0; i < listOfListener.size(); i++) {
                listOfListener.get(i).getUpdate();
            }
        }

        @Override
        public void run() {
            // Wait for init finished
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                logger.error(thing + ": Error during refresh channels: {}", e);
            }
            while (true) {
                callListener();
                try {
                    Thread.sleep(getRefreshInterval());
                } catch (InterruptedException e) {
                    logger.error(thing + ": Error during refresh channels: {}", e);
                }
            }
        }
    }
}

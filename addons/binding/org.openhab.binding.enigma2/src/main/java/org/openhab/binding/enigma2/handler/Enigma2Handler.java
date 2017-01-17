/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.handler;

import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.enigma2.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.Enigma2CommandHandler;
import org.openhab.binding.enigma2.internal.Enigma2CommandHandlerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2Handler extends BaseThingHandler implements Enigma2CommandHandlerListener {

    private Logger logger = LoggerFactory.getLogger(Enigma2Handler.class);

    private ChannelUID channelPowerUID;
    private ChannelUID channelVolumeUID;
    private ChannelUID channelMuteUID;
    private ChannelUID channelPlayerControlUID;
    private ChannelUID channelChannelUID;

    private ChannelUID channelNowPlaylingTitleUID;
    private ChannelUID channelNowPlaylingDescriptionUID;
    private ChannelUID channelNowPlaylingDescriptionExtendedUID;

    private Enigma2CommandHandler commandHandler;

    private Refresher refresher;
    boolean hadWarned = false;

    public Enigma2Handler(Thing thing) {
        super(thing);
        commandHandler = new Enigma2CommandHandler(getHostName(), getUserName(), getPassword());
    }

    @Override
    public void initialize() {
        channelPowerUID = getChannelUID(Enigma2BindingConstants.CHANNEL_POWER);
        channelVolumeUID = getChannelUID(Enigma2BindingConstants.CHANNEL_VOLUME);
        channelMuteUID = getChannelUID(Enigma2BindingConstants.CHANNEL_MUTE);
        channelPlayerControlUID = getChannelUID(Enigma2BindingConstants.CHANNEL_PLAYER_CONTROL);
        channelChannelUID = getChannelUID(Enigma2BindingConstants.CHANNEL_CHANNEL);

        channelNowPlaylingTitleUID = getChannelUID(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_TITLE);
        channelNowPlaylingDescriptionUID = getChannelUID(Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION);
        channelNowPlaylingDescriptionExtendedUID = getChannelUID(
                Enigma2BindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION_EXTENDED);

        refresher = new Refresher();
        refresher.addListener(this);
        commandHandler.generateServiceMaps();
        updateStatus(ThingStatus.ONLINE);
        refresher.start();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand() called, channelUID={}, command={}", channelUID, command);
        if (command instanceof RefreshType) {
            updateCurrentStates();
        } else {
            if (channelUID.equals(channelPowerUID)) {
                commandHandler.togglePowerState(command);
            } else if (channelUID.equals(channelVolumeUID)) {
                commandHandler.setVolume(command);
            } else if (channelUID.equals(channelChannelUID)) {
                commandHandler.setChannel(command);
            } else if (channelUID.equals(channelMuteUID)) {
                commandHandler.setMute(command);
            } else if (channelUID.equals(channelPlayerControlUID)) {
                commandHandler.setPlayControl(command);
            }
        }
    }

    @Override
    public void dispose() {
        refresher.removeListener(this);
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
            if (!hadWarned) {
                logger.warn(thing + ": Error during parsing refresh time. Set to default 5000", e);
                hadWarned = true;
            }
            returnVal = 5000;
        }
        return returnVal;
    }

    private ChannelUID getChannelUID(String channelId) {
        Channel chann = thing.getChannel(channelId);
        if (chann == null) {
            // refresh thing...
            Thing newThing = ThingFactory.createThing(TypeResolver.resolve(thing.getThingTypeUID()), thing.getUID(),
                    thing.getConfiguration());
            updateThing(newThing);
            chann = thing.getChannel(channelId);
        }
        return chann.getUID();
    }

    private void updateCurrentStates() {
        if (commandHandler.getPowerState() != null) {
            updateState(channelPowerUID, commandHandler.getPowerState());
        }
        if (commandHandler.getPowerState() == OnOffType.ON) {
            if (commandHandler.getVolumeState() != null) {
                updateState(channelVolumeUID, commandHandler.getVolumeState());
            }
            if (commandHandler.getMutedState() != null) {
                updateState(channelMuteUID, commandHandler.getMutedState());
            }
            if (commandHandler.getChannelState() != null) {
                updateState(channelChannelUID, commandHandler.getChannelState());
            }
            updateState(channelPlayerControlUID, new StringType(""));

            if (commandHandler.getNowPlayingTitle() != null) {
                updateState(channelNowPlaylingTitleUID, commandHandler.getNowPlayingTitle());
            }
            if (commandHandler.getNowPlayingDescription() != null) {
                updateState(channelNowPlaylingDescriptionUID, commandHandler.getNowPlayingDescription());
            }
            if (commandHandler.getNowPlayingDescriptionExtended() != null) {
                updateState(channelNowPlaylingDescriptionExtendedUID,
                        commandHandler.getNowPlayingDescriptionExtended());
            }
        } else {
            updateState(channelVolumeUID, new StringType(""));
            updateState(channelMuteUID, new StringType(""));
            updateState(channelChannelUID, new StringType(""));
            updateState(channelPlayerControlUID, new StringType(""));

            updateState(channelNowPlaylingTitleUID, new StringType(""));
            updateState(channelNowPlaylingDescriptionUID, new StringType(""));
            updateState(channelNowPlaylingDescriptionExtendedUID, new StringType(""));
        }
    }

    @Override
    public void getUpdate() {
        updateCurrentStates();
    }

    private class Refresher extends Thread {
        private ArrayList<Enigma2CommandHandlerListener> listOfListener;

        public Refresher() {
            listOfListener = new ArrayList<Enigma2CommandHandlerListener>();
        }

        public void removeListener(Enigma2CommandHandlerListener listener) {
            for (int i = 0; i < listOfListener.size(); i++) {
                if (listOfListener.get(i) == listener) {
                    listOfListener.remove(i);
                }
            }
            listOfListener.add(listener);
        }

        public void addListener(Enigma2CommandHandlerListener listener) {
            listOfListener.add(listener);
        }

        public void callAllListener() {
            for (int i = 0; i < listOfListener.size(); i++) {
                listOfListener.get(i).getUpdate();
            }
        }

        @Override
        public void run() {
            while (true) {
                callAllListener();

                try {
                    Thread.sleep(getRefreshInterval());
                } catch (InterruptedException e) {
                    logger.error(thing + ": Error during operation on database ", e);
                }
            }
        }

    }

}

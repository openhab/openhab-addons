/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.handler;

import java.util.ArrayList;

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
import org.openhab.binding.enigma2.internal.Enigma2PowerState;
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
    private ChannelUID channelChannelNameUID;
    private ChannelUID channelChannelNumberUID;
    private ChannelUID channelMuteUID;

    private Enigma2CommandHandler commandHandler;

    public Enigma2Handler(Thing thing) {
        super(thing);
        commandHandler = new Enigma2CommandHandler(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand() called, channelUID={}, command={}", channelUID, command);
        if (command instanceof RefreshType) {
            getCurrentValues();
        } else {
            if (channelUID.equals(channelPowerUID)) {
                commandHandler.sendOnOff(command, Enigma2PowerState.TOGGLE_STANDBY);
            } else if (channelUID.equals(channelVolumeUID)) {
                commandHandler.setVolume(command);
            } else if (channelUID.equals(channelChannelNumberUID)) {
                commandHandler.setChannel(command);
            } else if (channelUID.equals(channelMuteUID)) {
                commandHandler.sendMuteUnmute(command);
            }
            // sendRcCommand(command, bindingConfig.getCmdValue());
            // setDownmix(command);
        }
    }

    @Override
    public void initialize() {
        channelPowerUID = getChannelUID(Enigma2BindingConstants.CHANNEL_POWER);
        channelVolumeUID = getChannelUID(Enigma2BindingConstants.CHANNEL_VOLUME);
        channelChannelNameUID = getChannelUID(Enigma2BindingConstants.CHANNEL_CHANNEL_NAME);
        channelChannelNumberUID = getChannelUID(Enigma2BindingConstants.CHANNEL_CHANNEL_NUMBER);
        channelMuteUID = getChannelUID(Enigma2BindingConstants.CHANNEL_MUTE);

        Refresher refresher = new Refresher();
        refresher.addListener(this);
        updateStatus(ThingStatus.ONLINE);
        refresher.start();
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
        return Integer
                .parseInt((String) thing.getConfiguration().get(Enigma2BindingConstants.DEVICE_PARAMETER_REFRESH));
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

    private void getCurrentValues() {
        updateState(channelPowerUID, commandHandler.getPowerState());
        updateState(channelVolumeUID, commandHandler.getVolume());
        updateState(channelChannelNameUID, commandHandler.getChannelName());
        updateState(channelChannelNumberUID, commandHandler.getChannelNumber());
        updateState(channelMuteUID, commandHandler.isMuted());
    }

    @Override
    public void getUpdate() {
        getCurrentValues();
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

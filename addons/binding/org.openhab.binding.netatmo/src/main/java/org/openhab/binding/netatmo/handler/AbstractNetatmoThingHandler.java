/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RadioHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of all netatmo things
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class AbstractNetatmoThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    protected final MeasurableChannels measurableChannels = new MeasurableChannels();
    protected Optional<RadioHelper> radioHelper;
    protected Optional<BatteryHelper> batteryHelper;
    protected Configuration config;
    protected NetatmoBridgeHandler bridgeHandler;

    AbstractNetatmoThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration();

        radioHelper = thing.getProperties().containsKey(PROPERTY_SIGNAL_LEVELS)
                ? Optional.of(new RadioHelper(thing.getProperties().get(PROPERTY_SIGNAL_LEVELS)))
                : Optional.empty();
        batteryHelper = thing.getProperties().containsKey(PROPERTY_BATTERY_LEVELS)
                ? Optional.of(new BatteryHelper(thing.getProperties().get(PROPERTY_BATTERY_LEVELS)))
                : Optional.empty();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Pending parent object initialization");
    }

    protected State getNAThingProperty(String channelId) {
        Optional<State> result;

        result = batteryHelper.flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = radioHelper.flatMap(helper -> helper.getNAThingProperty(channelId));
        if (result.isPresent()) {
            return result.get();
        }
        result = measurableChannels.getNAThingProperty(channelId);

        return result.orElse(UnDefType.UNDEF);
    }

    protected void updateChannels() {
        getThing().getChannels().stream().filter(channel -> channel.getKind() != ChannelKind.TRIGGER)
                .forEach(channel -> {
                    String channelId = channel.getUID().getId();
                    State state = getNAThingProperty(channelId);
                    if (state != null) {
                        updateState(channel.getUID(), state);
                    }
                });
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        measurableChannels.addChannel(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        measurableChannels.removeChannel(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels();
        }
    }

    protected NetatmoBridgeHandler getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeHandler = (NetatmoBridgeHandler) bridge.getHandler();
            }
        }
        return bridgeHandler;
    }

    public boolean matchesId(String searchedId) {
        return searchedId != null ? searchedId.equalsIgnoreCase(getId()) : false;
    }

    protected String getId() {
        if (config != null) {
            String equipmentId = (String) config.get(EQUIPMENT_ID);
            return equipmentId.toLowerCase();
        } else {
            return null;
        }
    }

}

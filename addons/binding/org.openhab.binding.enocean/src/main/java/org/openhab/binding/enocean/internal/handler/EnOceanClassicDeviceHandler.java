/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.handler;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.THING_TYPE_CLASSICDEVICE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchListenerConfig;
import org.openhab.binding.enocean.internal.eep.EEPType;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 */
public class EnOceanClassicDeviceHandler extends EnOceanBaseActuatorHandler {

    // List of thing types which support sending of eep messages
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_CLASSICDEVICE));

    private Hashtable<String, EnOceanChannelRockerSwitchListenerConfig> channelConfigById;
    private String currentEnOceanId;

    public EnOceanClassicDeviceHandler(Thing thing) {
        super(thing);

        channelConfigById = new Hashtable<>();
    }

    @Override
    protected void updateChannels(EEPType eep, boolean removeUnsupportedChannels) {
        // do remove any channels here as this thing has predefined and extended channels
        return;
    }

    @Override
    public long getSenderIdToListenTo() {
        return Long.parseLong(currentEnOceanId, 16);
    }

    @Override
    public void channelLinked(@NonNull ChannelUID channelUID) {
        super.channelLinked(channelUID);

        // if linked channel is a listening channel => put listener
        String id = channelUID.getId();
        Channel channel = getLinkedChannels().get(id);
        addListener(id, channel);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        // it seems that there does not exist a channel update callback
        // => remove all listeners and add them again
        while (!channelConfigById.isEmpty()) {
            removeListener(channelConfigById.keys().nextElement());
        }

        getLinkedChannels().forEach((id, c) -> {
            if (!addListener(id, c)) {
                // Todo feedback for wrong channel configuration
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong channel configuration");
            }
        });
    }

    @Override
    public void channelUnlinked(@NonNull ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        // if unlinked channel is listening channel => remove listener
        String id = channelUID.getId();
        Channel channel = getLinkedChannels().get(id);
        if (channel != null && channel.getChannelTypeUID().getId().startsWith("rockerswitchListener")) {
            removeListener(id);
        }
    }

    protected boolean addListener(String channelId, Channel channel) {
        if (channel != null && channel.getChannelTypeUID().getId().startsWith("rockerswitchListener")) {
            EnOceanChannelRockerSwitchListenerConfig config = channel.getConfiguration()
                    .as(EnOceanChannelRockerSwitchListenerConfig.class);

            if (config != null) {
                try {
                    Long enoceanId = Long.parseLong(config.enoceanId, 16);
                    channelConfigById.putIfAbsent(channelId, config);
                    currentEnOceanId = config.enoceanId;
                    getBridgeHandler().addPacketListener(this);

                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        }
        return true;
    }

    protected void removeListener(String channelId) {
        if (channelConfigById.containsKey(channelId)) {
            currentEnOceanId = channelConfigById.get(channelId).enoceanId;
            channelConfigById.remove(channelId);
            getBridgeHandler().removePacketListener(this);
        }
    }

}

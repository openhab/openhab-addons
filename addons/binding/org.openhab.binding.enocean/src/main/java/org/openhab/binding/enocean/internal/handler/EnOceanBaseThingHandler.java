/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.enocean.internal.handler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.EnOceanChannelDescription;
import org.openhab.binding.enocean.internal.config.EnOceanBaseConfig;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class EnOceanBaseThingHandler extends ConfigStatusThingHandler {

    private EnOceanBridgeHandler gateway = null;
    protected Logger logger = LoggerFactory.getLogger(EnOceanBaseThingHandler.class);

    protected String configurationErrorDescription;

    private HashSet<Channel> linkedChannels = null;

    protected Hashtable<String, State> channelState = null;
    protected Hashtable<String, String> lastEvents = null;

    protected EnOceanBaseConfig config = null;

    public EnOceanBaseThingHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing enocean base thing handler.");
        this.gateway = null; // reset gateway in case we change the bridge
        this.config = null;
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

        if (getBridgeHandler() != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                initializeConfig();
                if (validateConfig()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            configurationErrorDescription);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "A bridge is required");
        }
    }

    protected boolean validateEnoceanId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        } else {
            if (id.length() != 8) {
                return false;
            }

            try {
                Integer.parseUnsignedInt(id, 16);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    abstract void initializeConfig();

    abstract boolean validateConfig();

    protected void updateChannels(EEPType eep, boolean removeUnsupportedChannels) {

        @NonNull
        List<@NonNull Channel> channelList = new LinkedList<@NonNull Channel>(this.getThing().getChannels());
        boolean channelListChanged = false;

        if (removeUnsupportedChannels) {
            channelListChanged = channelList.removeIf(channel -> !eep.isChannelSupported(channel));
        }

        for (Map.Entry<String, EnOceanChannelDescription> entry : eep.GetSupportedChannels().entrySet()) {

            String channelId = entry.getKey();
            EnOceanChannelDescription cd = entry.getValue();

            // if we do not need to auto create channel => skip
            if (!cd.autoCreate) {
                continue;
            }

            // if we already created a channel with the same type => skip
            if (channelList.stream().anyMatch(channel -> {
                ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                String id = channelTypeUID == null ? "" : channelTypeUID.getId();

                return cd.channelTypeUID.getId().equals(id);
            })) {
                continue;
            }

            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), channelId), cd.acceptedItemType)
                    .withConfiguration(eep.getChannelConfig(channelId)).withType(cd.channelTypeUID)
                    .withKind(cd.isStateChannel ? ChannelKind.STATE : ChannelKind.TRIGGER).withLabel(cd.label).build();

            channelList.add(channel);
            channelListChanged = true;
        }

        if (channelListChanged) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    protected HashSet<Channel> getLinkedChannels() {
        if (linkedChannels != null) {
            return linkedChannels;
        }

        linkedChannels = new HashSet<>();
        if (channelState == null) {
            channelState = new Hashtable<>();
            lastEvents = new Hashtable<>();
        }

        for (Channel c : this.getThing().getChannels()) {
            String id = c.getUID().getId();

            if (isLinked(id)) {
                {
                    linkedChannels.add(c);
                    channelState.putIfAbsent(id, UnDefType.UNDEF);
                }
            } else if (c.getKind() == ChannelKind.TRIGGER) {
                linkedChannels.add(c);
                lastEvents.putIfAbsent(id, "");
            }
        }

        return linkedChannels;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        String id = channelUID.getId();
        if (linkedChannels == null) {
            return;
        }

        linkedChannels.add(thing.getChannel(id));
        channelState.putIfAbsent(id, UnDefType.UNDEF);
        lastEvents.putIfAbsent(id, "");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        String id = channelUID.getId();
        if (linkedChannels == null) {
            return;
        }

        if (!isLinked(id)) {
            linkedChannels.remove(thing.getChannel(id));
            channelState.remove(id);
            lastEvents.remove(id);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
        // reset linkedChannels to get current config of channels
        linkedChannels = null;
    }

    protected State getCurrentState(String channelId) {
        return channelState.get(channelId);
    }

    protected void setCurrentState(String channelId, State state) {
        channelState.put(channelId, state);
    }

    protected synchronized EnOceanBridgeHandler getBridgeHandler() {
        if (this.gateway == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof EnOceanBridgeHandler) {
                this.gateway = (EnOceanBridgeHandler) handler;
            } else {
                return null;
            }
        }
        return this.gateway;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // TODO
        // Collection<ConfigStatusMessage> configStatusMessages;

        // The senderId must be provided
        /*
         * final EnOceanActuatorConfig config = getConfigAs(EnOceanActuatorConfig.class);
         * final String senderId = config.senderIdOffset;
         * if (senderId == null || senderId.isEmpty()) {
         * configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(SENDERID)
         * .withMessageKeySuffix(EnOceanConfigStatusMessage.SENDERID_MISSING.getMessageKey())
         * .withArguments(SENDERID).build());
         * } else {
         * try {
         * Integer.parseUnsignedInt(senderId, 16);
         * } catch (Exception e) {
         * configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(SENDERID)
         * .withMessageKeySuffix(EnOceanConfigStatusMessage.SENDERID_MALFORMED.getMessageKey())
         * .withArguments(SENDERID).build());
         * }
         * configStatusMessages = Collections.emptyList();
         * }
         *
         * return configStatusMessages;
         */

        return new LinkedList<ConfigStatusMessage>();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initializeThing(bridgeStatusInfo.getStatus());
        }
    }
}

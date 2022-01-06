/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.enocean.internal.EnOceanChannelDescription;
import org.openhab.binding.enocean.internal.config.EnOceanBaseConfig;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.items.Item;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ConfigStatusThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    // There is no data structure which holds the last triggered event, so we have to implement it by ourself
    // This is especially needed for press and release events
    protected Hashtable<String, String> lastEvents = new Hashtable<>();

    protected EnOceanBaseConfig config = null;

    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    protected @NonNull ChannelUID prepareAnswer;

    public EnOceanBaseThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        prepareAnswer = new ChannelUID(thing.getUID(), CHANNEL_STATUS_REQUEST_EVENT);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing enocean base thing handler.");
        this.gateway = null; // reset gateway in case we change the bridge
        this.config = null;
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "A bridge is required");
        } else {
            initializeThing(bridge.getStatus());
        }
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

        if (this.itemChannelLinkRegistry == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ItemChannelLinkRegistry could not be found");
        } else {
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

    abstract Collection<EEPType> getEEPTypes();

    protected void updateChannels() {
        @NonNull
        List<@NonNull Channel> channelList = new LinkedList<>(this.getThing().getChannels());
        Collection<EEPType> eeps = getEEPTypes();
        if (eeps == null) {
            return;
        }

        // First remove channels which are no longer supported by current selected eeps of thing
        AtomicBoolean channelListChanged = new AtomicBoolean(
                channelList.removeIf(channel -> !eeps.stream().anyMatch(eep -> eep.isChannelSupported(channel))));

        // Next create supported channels of each selected eep
        eeps.stream().flatMap(eep -> eep.GetSupportedChannels().keySet().stream().map(id -> new SimpleEntry<>(id, eep)))
                .forEach(entry -> {
                    String channelId = entry.getKey();
                    EnOceanChannelDescription cd = entry.getValue().GetSupportedChannels().get(channelId);

                    if (cd == null) {
                        return;
                    }

                    // if we do not need to auto create channel => skip
                    if (!cd.autoCreate) {
                        return;
                    }

                    // if we already created a channel with the same type and id => skip
                    if (channelList.stream().anyMatch(channel -> cd.channelTypeUID.equals(channel.getChannelTypeUID())
                            && channelId.equals(channel.getUID().getId()))) {
                        return;
                    }

                    // create channel and add it to the channelList
                    Channel channel = ChannelBuilder
                            .create(new ChannelUID(this.getThing().getUID(), channelId), cd.acceptedItemType)
                            .withConfiguration(entry.getValue().getChannelConfig(channelId)).withType(cd.channelTypeUID)
                            .withKind(cd.isStateChannel ? ChannelKind.STATE : ChannelKind.TRIGGER).withLabel(cd.label)
                            .build();

                    channelList.add(channel);
                    channelListChanged.set(true);

                    if (!cd.isStateChannel) {
                        lastEvents.putIfAbsent(channelId, "");
                    }
                });

        if (channelListChanged.get()) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    protected State getCurrentState(Channel channel) {
        if (channel != null) {
            Set<Item> items = itemChannelLinkRegistry.getLinkedItems(channel.getUID());
            for (Item item : items) {
                State state = item.getState();
                if (state != UnDefType.NULL && state != UnDefType.UNDEF) {
                    return state;
                }
            }
        }

        return UnDefType.UNDEF;
    }

    protected State getCurrentState(String channelId) {
        return getCurrentState(getThing().getChannel(channelId));
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

        return new LinkedList<>();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initializeThing(bridgeStatusInfo.getStatus());
        }
    }
}

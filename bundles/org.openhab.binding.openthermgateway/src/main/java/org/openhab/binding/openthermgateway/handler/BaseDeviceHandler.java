/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.openthermgateway.handler;

import static org.openhab.binding.openthermgateway.internal.OpenThermGatewayBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openthermgateway.internal.DataItem;
import org.openhab.binding.openthermgateway.internal.DataItemGroup;
import org.openhab.binding.openthermgateway.internal.Message;
import org.openhab.binding.openthermgateway.internal.TspFhbSizeDataItem;
import org.openhab.binding.openthermgateway.internal.TspFhbValueDataItem;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseDeviceHandler} is a base class for actual Things.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public abstract class BaseDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BaseDeviceHandler.class);

    public BaseDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            // note: the framework handles bridge configuration resp. offline errors
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();

        if (bridge != null) {
            OpenThermGatewayHandler handler = (OpenThermGatewayHandler) bridge.getHandler();
            if (handler != null) {
                handler.handleCommand(channelUID, command);
            }
        } else {
            logger.debug("Bridge is missing");
        }
    }

    public void receiveMessage(Message message) {
        DataItem[] dataItems = DataItemGroup.DATAITEMGROUPS.get(message.getID());

        if (dataItems == null) {
            logger.debug("No DataItem found for message id {}", message.getID());
            return;
        }

        for (DataItem dataItem : dataItems) {
            if (dataItem instanceof TspFhbSizeDataItem) {
                logger.debug("Received TSP or FHB size message {} ({})", message.getID(), dataItem.getSubject());

                verifyTspFhbChannels(((TspFhbSizeDataItem) dataItem).getValueId(),
                        message.getUInt(dataItem.getByteType()));
            } else {
                String channelId = dataItem.getChannelId(message);

                if (thing.getChannel(channelId) == null || !dataItem.hasValidCodeType(message)) {
                    continue;
                }

                State state = dataItem.createState(message);

                logger.debug("Received update for channel {}: {}", channelId, state);
                updateState(channelId, state);
            }
        }
    }

    private void verifyTspFhbChannels(int id, int size) {
        // Dynamically create TSP or FHB value channels based on TSP or FHB size message
        ThingHandlerCallback callback = getCallback();

        if (callback == null) {
            logger.debug("Unable to get thing handler callback");
            return;
        }

        DataItem[] dataItems = DataItemGroup.DATAITEMGROUPS.get(id);

        if (dataItems == null) {
            logger.debug("Unable to find dataItem for id {}", id);
            return;
        }

        if (dataItems.length != 1) {
            logger.debug("Found zero or multiple dataItems for id {}", id);
            return;
        }

        TspFhbValueDataItem dataItem = (TspFhbValueDataItem) dataItems[0];

        logger.debug("Checking number of TSP or FHB channels for DATA-ID {}: {}", id, size);

        // A generic Number:Dimensionless channel type for TSP and FHB values
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TSPFHB);

        List<Channel> channels = new ArrayList<>(getThing().getChannels());

        boolean changed = false;
        for (int i = 0; i < size; i++) {
            String channelId = dataItem.getChannelId(i);
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);

            if (!channels.stream().map(Channel::getUID).anyMatch(channelUID::equals)) {
                String label = dataItem.getLabel(i);

                logger.debug("Adding channel {}", channelId);

                channels.add(callback.createChannelBuilder(channelUID, channelTypeUID).withKind(ChannelKind.STATE)
                        .withLabel(label).build());
                changed = true;
            } else {
                logger.debug("Channel {} already exists", channelId);
            }
        }

        if (changed) {
            logger.debug("Updating Thing with new channels");
            updateThing(editThing().withChannels(channels).build());
        }
    }
}

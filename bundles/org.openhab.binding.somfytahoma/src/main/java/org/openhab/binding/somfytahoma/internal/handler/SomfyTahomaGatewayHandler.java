/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaStateDescriptionOptionProvider;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaActionGroup;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaStatus;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;

/**
 * The {@link SomfyTahomaGatewayHandler} is responsible for handling commands,
 * which are sent to one of the channels of the gateway thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaGatewayHandler extends SomfyTahomaBaseThingHandler {

    private final SomfyTahomaStateDescriptionOptionProvider stateDescriptionProvider;

    public SomfyTahomaGatewayHandler(Thing thing, SomfyTahomaStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initializeThing(@Nullable ThingStatus bridgeStatus) {
        if (bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                refresh(STATUS);
                refresh(SCENARIOS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void refresh(String channel) {
        if (channel.equals(STATUS)) {
            String id = getGateWayId();
            SomfyTahomaStatus status = getTahomaStatus(id);
            String tahomaStatus = status.getStatus();
            Channel ch = thing.getChannel(channel);
            if (ch != null) {
                updateState(ch.getUID(), new StringType(tahomaStatus));
            }
            // update the firmware property
            String fw = status.getProtocolVersion();
            updateProperty(PROPERTY_FIRMWARE_VERSION, fw);

            updateStatus("DISCONNECTED".equals(tahomaStatus) ? ThingStatus.OFFLINE : ThingStatus.ONLINE);
        } else if (channel.equals(SCENARIOS)) {
            SomfyTahomaBridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                List<StateOption> options = new ArrayList<>();
                for (SomfyTahomaActionGroup actionGroup : handler.listActionGroups()) {
                    options.add(new StateOption(actionGroup.getOid(), actionGroup.getLabel()));
                }
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channel), options);
            }
        }
    }

    public String getGateWayId() {
        return getThing().getConfiguration().get("id").toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        }
        if (channelUID.getId().equals(SCENARIOS)) {
            SomfyTahomaBridgeHandler handler = getBridgeHandler();
            if (handler != null && command instanceof StringType) {
                handler.executeActionGroup(command.toString());
            }
        }
    }
}

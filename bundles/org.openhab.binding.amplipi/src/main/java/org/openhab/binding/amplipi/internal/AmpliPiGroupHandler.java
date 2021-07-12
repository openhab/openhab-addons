/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.amplipi.internal;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.api.GroupApi;
import org.openhab.binding.amplipi.internal.model.Group;
import org.openhab.binding.amplipi.internal.model.GroupUpdate;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmpliPiGroupHandler} is responsible for handling commands, which are
 * sent to one of the AmpliPi Groups.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AmpliPiGroupHandler extends BaseThingHandler implements AmpliPiStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(AmpliPiGroupHandler.class);

    private final Integer id;
    private @Nullable GroupApi api;

    public AmpliPiGroupHandler(Thing thing) {
        super(thing);
        id = Integer.valueOf(thing.getConfiguration().get(AmpliPiBindingConstants.CFG_PARAM_ID).toString());
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AmpliPiHandler bridgeHandler = (AmpliPiHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                bridgeHandler.addStatusChangeListener(this);
                GroupApi api = bridgeHandler.getGroupApi();
                if (api != null) {
                    this.api = api;
                } else {
                    throw new IllegalStateException("Group API must not be null here!");
                }
            } else {
                throw new IllegalStateException("Bridge handler must not be null here!");
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            throw new IllegalStateException("Bridge must not be null here!");
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command == RefreshType.REFRESH) {
            // do nothing - we just wait for the next automatic refresh
        }
        GroupUpdate update = new GroupUpdate();
        switch (channelUID.getId()) {
            case AmpliPiBindingConstants.CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    update.setMute(command == OnOffType.ON);
                }
                break;
            case AmpliPiBindingConstants.CHANNEL_VOLUME:
                if (command instanceof PercentType) {
                    update.setVolDelta(AmpliPiUtils.percentTypeToVolume((PercentType) command));
                }
                break;
            case AmpliPiBindingConstants.CHANNEL_SOURCE:
                if (command instanceof DecimalType) {
                    update.setSourceId(((DecimalType) command).intValue());
                }
                break;
        }
        if (api != null) {
            api.setGroupApiGroupsGidPatch(id, update);
        } else {
            logger.error("API not available for executing group update!");
        }
    }

    @Override
    public void receive(@NonNull Status status) {
        Optional<Group> group = status.getGroups().stream().filter(z -> z.getId().equals(id)).findFirst();
        if (group.isPresent()) {
            Boolean mute = group.get().getMute();
            Integer volume = group.get().getVolDelta();
            Integer source = group.get().getSourceId();
            updateState(AmpliPiBindingConstants.CHANNEL_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
            updateState(AmpliPiBindingConstants.CHANNEL_VOLUME, AmpliPiUtils.volumeToPercentType(volume));
            updateState(AmpliPiBindingConstants.CHANNEL_SOURCE, new DecimalType(source));
        }
    }
}

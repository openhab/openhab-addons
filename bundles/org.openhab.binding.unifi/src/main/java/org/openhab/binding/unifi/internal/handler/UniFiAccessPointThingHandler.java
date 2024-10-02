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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_DISABLED;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiAccessPointThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiDevice;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An access point managed by the UniFi controller software.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class UniFiAccessPointThingHandler extends UniFiBaseThingHandler<UniFiDevice, UniFiAccessPointThingConfig> {

    private final Logger logger = LoggerFactory.getLogger(UniFiAccessPointThingHandler.class);

    private UniFiAccessPointThingConfig config = new UniFiAccessPointThingConfig();

    public UniFiAccessPointThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(final UniFiAccessPointThingConfig config) {
        this.config = config;
        if (!config.isValid()) {
            // ToDo Error message
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.ap.offline.configuration_error");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable UniFiDevice getEntity(final UniFiControllerCache cache) {
        return cache.getDevice(config.getMacAddress());
    }

    @Override
    protected State getChannelState(final UniFiDevice device, final String channelId) {
        final State state;

        switch (channelId) {
            case CHANNEL_DISABLED:
                state = OnOffType.from(device.isDisabled());
                break;
            default:
                state = UnDefType.UNDEF;
        }
        return state;
    }

    @Override
    protected boolean handleCommand(final UniFiController controller, final UniFiDevice device,
            final ChannelUID channelUID, final Command command) throws UniFiException {
        final String channelID = channelUID.getIdWithoutGroup();

        switch (channelID) {
            case CHANNEL_DISABLED:
                if (command instanceof OnOffType) {
                    return handleDisableCommand(controller, device, channelUID, command);
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private boolean handleDisableCommand(final UniFiController controller, final UniFiDevice device,
            final ChannelUID channelUID, final Command command) throws UniFiException {
        if (command instanceof OnOffType) {
            controller.disableAccessPoint(device, command == OnOffType.ON);
            refresh();
            return true;
        }
        return false;
    }
}

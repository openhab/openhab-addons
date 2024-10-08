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

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_AP_ENABLE;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.DEVICE_TYPE_UAP;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiAccessPointThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiDevice;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * An access point managed by the UniFi controller software.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class UniFiAccessPointThingHandler extends UniFiBaseThingHandler<UniFiDevice, UniFiAccessPointThingConfig> {

    private UniFiAccessPointThingConfig config = new UniFiAccessPointThingConfig();

    public UniFiAccessPointThingHandler(final Thing thing) {
        super(thing);
    }

    private static boolean belongsToSite(final UniFiDevice client, final String siteName) {
        boolean result = true;
        if (!siteName.isEmpty()) {
            final UniFiSite site = client.getSite();
            if (site == null || !site.matchesName(siteName)) {
                result = false;
            }
        }
        return result;
    }

    @Override
    protected boolean initialize(final UniFiAccessPointThingConfig config) {
        this.config = config;
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.ap.offline.configuration_error");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable UniFiDevice getEntity(final UniFiControllerCache cache) {
        final UniFiDevice device = cache.getDevice(config.getMacAddress());
        if (device == null || !belongsToSite(device, config.getSite()) || !DEVICE_TYPE_UAP.equals(device.getType())) {
            return null;
        }
        return device;
    }

    @Override
    protected State getChannelState(final UniFiDevice device, final String channelId) {
        State state = getDefaultState(channelId);

        switch (channelId) {
            case CHANNEL_AP_ENABLE:
                state = OnOffType.from(!device.isDisabled());
                break;
        }
        return state;
    }

    @Override
    protected void updateProperties(final UniFiDevice device) {
        updateProperties(Map.of( //
                Thing.PROPERTY_MODEL_ID, device.getModel(), //
                Thing.PROPERTY_FIRMWARE_VERSION, device.getVersion(), //
                Thing.PROPERTY_SERIAL_NUMBER, device.getSerial()));
    }

    @Override
    protected boolean handleCommand(final UniFiController controller, final UniFiDevice device,
            final ChannelUID channelUID, final Command command) throws UniFiException {
        final String channelID = channelUID.getIdWithoutGroup();

        if (CHANNEL_AP_ENABLE.equals(channelID) && command instanceof OnOffType onOffCommand) {
            return handleEnableCommand(controller, device, channelUID, onOffCommand);
        }
        return false;
    }

    private boolean handleEnableCommand(final UniFiController controller, final UniFiDevice device,
            final ChannelUID channelUID, final OnOffType command) throws UniFiException {
        controller.disableAccessPoint(device, command == OnOffType.OFF);
        refresh();
        return true;
    }
}

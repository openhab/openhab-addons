/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_ASSOCLIST;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_CHANNEL;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_CLIENT_COUNT;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_ENABLED;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_SSID;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTRadioConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.binding.ddwrt.internal.api.DDWRTRadio;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
 * Handler for a DD-WRT Radio thing.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTRadioThingHandler extends DDWRTBaseHandler<DDWRTRadio, DDWRTRadioConfiguration> {

    private final Logger logger = LoggerFactory.getLogger(DDWRTRadioThingHandler.class);

    private DDWRTRadioConfiguration config = new DDWRTRadioConfiguration();

    public DDWRTRadioThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(DDWRTRadioConfiguration config) {
        this.config = config;
        if (config.interfaceId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-interfaceid");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable DDWRTRadio getEntity(DDWRTNetworkCache cache) {
        return cache.getRadio(config.interfaceId);
    }

    @Override
    protected State getChannelState(DDWRTRadio radio, String channelId) {
        return switch (channelId) {
            case CHANNEL_ENABLED -> OnOffType.from(radio.isEnabled());
            case CHANNEL_CHANNEL -> new DecimalType(radio.getChannel());
            case CHANNEL_SSID -> radio.getSsid().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(radio.getSsid());
            case CHANNEL_MODE -> radio.getMode().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(radio.getMode());
            case CHANNEL_CLIENT_COUNT -> new DecimalType(radio.getClientCount());
            case CHANNEL_ASSOCLIST -> {
                String joined = String.join(",", radio.getAssoclist());
                yield joined.isEmpty() ? UnDefType.UNDEF : StringType.valueOf(joined);
            }
            default -> UnDefType.NULL;
        };
    }

    @Override
    protected boolean handleCommand(DDWRTNetwork network, DDWRTRadio radio, ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (CHANNEL_ENABLED.equals(channelId) && command instanceof OnOffType onOff) {
            boolean enabled = onOff == OnOffType.ON;
            logger.debug("{} radio {}", enabled ? "Enabling" : "Disabling", radio.getInterfaceId());

            boolean success = network.setRadioEnabled(radio.getParentDeviceMac(), radio.getInterfaceId(), enabled);
            if (success) {
                logger.debug("Radio {} command sent successfully", radio.getInterfaceId());
                // State will be updated on next network refresh cycle
            } else {
                logger.warn("Failed to {} radio {}", enabled ? "enable" : "disable", radio.getInterfaceId());
            }
            return true;
        }
        return false;
    }

    @Override
    protected List<String> getCacheKeys() {
        if (!config.interfaceId.isEmpty()) {
            return List.of(config.interfaceId.toLowerCase(Locale.ROOT));
        }
        return List.of();
    }
}

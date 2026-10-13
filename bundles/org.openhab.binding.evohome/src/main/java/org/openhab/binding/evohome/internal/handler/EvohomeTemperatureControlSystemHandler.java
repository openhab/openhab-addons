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
package org.openhab.binding.evohome.internal.handler;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.action.EvohomeSystemActions;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.SystemModeStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystemStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a temperature control system. Gets and sets global system mode.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Explicit system reset to schedule
 * @author Kovacs Istvan - Temporary system modes and expiry state
 *
 */
@NonNullByDefault
public class EvohomeTemperatureControlSystemHandler extends BaseEvohomeHandler {
    private final Logger logger = LoggerFactory.getLogger(EvohomeTemperatureControlSystemHandler.class);

    public EvohomeTemperatureControlSystemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EvohomeSystemActions.class);
    }

    public void update(@Nullable GatewayStatus gatewayStatus, @Nullable TemperatureControlSystemStatus tcsStatus) {
        if (tcsStatus == null || gatewayStatus == null) {
            logger.trace("Thing {}: missing controller or gateway status for configured display ID {}",
                    getThing().getUID(), getId());
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Status not found, check the display id");
        } else if (!handleActiveFaults(gatewayStatus)) {
            updateEvohomeThingStatus(ThingStatus.ONLINE);
            SystemModeStatus status = tcsStatus.getMode();
            String mode = status == null ? null : status.getMode();
            Boolean permanent = status == null ? null : status.getIsPermanent();
            Instant until = status != null && Boolean.FALSE.equals(permanent) ? parseUntil(status.getTimeUntil())
                    : null;
            State modeState = mode == null || mode.isBlank() ? UnDefType.UNDEF : new StringType(mode);
            State permanentState = permanent == null ? UnDefType.UNDEF : OnOffType.from(permanent);
            State untilState = until == null ? UnDefType.UNDEF : new DateTimeType(until.atZone(ZoneOffset.UTC));
            updateState(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_CHANNEL, modeState);
            updateState(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_PERMANENT_CHANNEL, permanentState);
            updateState(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_UNTIL_CHANNEL, untilState);
            logger.trace("Thing {}: published SystemMode={}, permanent={}, until={}", getThing().getUID(), modeState,
                    permanent, until);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestUpdate();
        } else if (channelUID.getId().equals(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_CHANNEL)) {
            setSystemMode(command.toString());
        } else if (channelUID.getId().equals(EvohomeBindingConstants.DISPLAY_RESET_CHANNEL)
                && command == OnOffType.ON) {
            resetSystem();
        }
    }

    public boolean resetSystem() {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.resetSystem(getId());
    }

    public boolean setSystemMode(String mode) {
        if ("AutoWithReset".equals(mode)) {
            return resetSystem();
        }
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.setTcsMode(getId(), mode);
    }

    public boolean setTemporarySystemMode(String mode, Instant until) {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.setTemporaryTcsMode(getId(), mode, until);
    }

    private @Nullable Instant parseUntil(@Nullable String until) {
        if (until != null) {
            try {
                return OffsetDateTime.parse(until).toInstant();
            } catch (DateTimeException e) {
                logger.debug("Thing {}: system mode expiry is not a valid offset date/time", getThing().getUID());
            }
        }
        return null;
    }

    private boolean handleActiveFaults(GatewayStatus gatewayStatus) {
        if (gatewayStatus.hasActiveFaults()) {
            logger.trace(
                    "Thing {}: skipping controller channel updates because of active gateway faults; firstFault={}",
                    getThing().getUID(), gatewayStatus.getActiveFault(0).getFaultType());
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    gatewayStatus.getActiveFault(0).getFaultType());
            return true;
        }
        return false;
    }
}

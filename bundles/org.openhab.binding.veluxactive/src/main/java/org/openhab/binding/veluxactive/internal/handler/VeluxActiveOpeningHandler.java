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
package org.openhab.binding.veluxactive.internal.handler;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.VeluxType;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reports NXO windows and covers and routes signed and unsigned movement commands.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveOpeningHandler extends VeluxActiveModuleHandler {

    private final Logger logger = LoggerFactory.getLogger(VeluxActiveOpeningHandler.class);

    private @Nullable VeluxType veluxType;

    public VeluxActiveOpeningHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        synchronized (this) {
            veluxType = null;
        }
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            super.handleCommand(channelUID, command);
            return;
        }
        if (!CHANNEL_POSITION.equals(channelUID.getId())) {
            return;
        }
        ModuleRequestContext requestContext;
        VeluxType type;
        String gateway;
        synchronized (this) {
            requestContext = captureRequestContext();
            type = veluxType;
            ModuleRequestContext parent = requestContext == null ? null : requestContext.parent();
            gateway = parent == null ? null : parent.moduleId();
            if (requestContext == null || getThing().getStatus() != ThingStatus.ONLINE) {
                return;
            }
        }
        VeluxActiveAccountHandler account = account();
        if (account == null) {
            return;
        }
        // The app's stop command applies to every opening on the gateway.
        if (command == StopMoveType.STOP) {
            if (gateway != null && !gateway.isBlank()) {
                account.stopMovements(requestContext, gateway);
            } else {
                logger.warn("Cannot stop movements for {} until its gateway is known", getThing().getUID());
            }
            return;
        }
        int position;
        if (command instanceof PercentType percent) {
            position = percent.intValue();
        } else if (command instanceof UpDownType direction) {
            position = direction == UpDownType.UP ? 0 : 100;
        } else {
            return;
        }
        if (type == null) {
            logger.warn("Cannot control {} until its window/cover type is known", getThing().getUID());
            return;
        }
        int apiPosition = type.isInverted() ? 100 - position : position;
        if (gateway == null || gateway.isBlank()) {
            logger.warn("Cannot control {} until its gateway is known", getThing().getUID());
            return;
        }
        if (type == VeluxType.WINDOW && apiPosition != 0) {
            ModuleRequestContext parent = requestContext.parent();
            if (parent != null && parent.handler() instanceof VeluxActiveGatewayHandler handler && handler.canSign()) {
                account.setSignedPosition(requestContext, apiPosition);
            } else {
                logger.warn("Pair the gateway before opening windows for {}", getThing().getUID());
            }
        } else {
            account.setPosition(requestContext, gateway, apiPosition);
        }
    }

    @Override
    synchronized void update(ModuleRequestContext requestContext, HomeStatus status) {
        if (!isRequestContextCurrent(requestContext)) {
            return;
        }
        Module module = status.module(requestContext.moduleId());
        if (module != null && !checkGateway(requestContext, module)) {
            return;
        }
        if (status.error(requestContext.moduleId()) != null
                || module != null && Boolean.FALSE.equals(module.reachable)) {
            failed(requestContext, "@text/offline.comm-error.opening.unreachable");
            return;
        }
        if (module == null) {
            missingStatus("@text/status.opening.missing");
            return;
        }
        Map<String, String> properties = editProperties();
        VeluxType type = module.veluxType;
        if (type != null) {
            veluxType = type;
            properties.put(PROPERTY_VELUX_TYPE, type.toCamelCase());
        }
        Integer firmware = module.firmwareRevision;
        if (firmware != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmware.toString());
        }
        String manufacturer = module.manufacturer;
        if (manufacturer != null) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
        }
        updateProperties(properties);
        Integer position = position(module.currentPosition);
        updateState(CHANNEL_POSITION, position == null ? UnDefType.UNDEF : new PercentType(position));
        String mode = module.mode;
        updateState(CHANNEL_MODE, mode == null ? UnDefType.UNDEF : new StringType(mode.replace('_', '-')));
        String batteryState = module.batteryState;
        updateState(CHANNEL_BATTERY_STATE,
                batteryState == null ? UnDefType.UNDEF : new StringType(batteryState.replace('_', '-')));
        Long seen = module.lastSeen;
        try {
            updateState(CHANNEL_LAST_SEEN,
                    seen == null ? UnDefType.UNDEF : new DateTimeType(Instant.ofEpochSecond(seen)));
        } catch (DateTimeException e) {
            updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    synchronized void positionAccepted(ModuleRequestContext requestContext, int apiPosition) {
        if (isRequestContextCurrent(requestContext)) {
            Integer expected = position(apiPosition);
            if (expected != null) {
                // Acceptance predicts the destination; status polls reconcile the actual position.
                updateState(CHANNEL_POSITION, new PercentType(expected));
            }
        }
    }

    private @Nullable Integer position(@Nullable Integer value) {
        VeluxType veluxType = this.veluxType;
        if (value == null || value < 0 || value > 100 || veluxType == null) {
            return null;
        }
        // VELUX uses 0 = closed for windows and 100 = closed for covers.
        return veluxType.isInverted() ? 100 - value : value;
    }

    @Override
    protected void clearState() {
        updateState(CHANNEL_POSITION, UnDefType.UNDEF);
        updateState(CHANNEL_MODE, UnDefType.UNDEF);
        updateState(CHANNEL_BATTERY_STATE, UnDefType.UNDEF);
        updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
    }
}

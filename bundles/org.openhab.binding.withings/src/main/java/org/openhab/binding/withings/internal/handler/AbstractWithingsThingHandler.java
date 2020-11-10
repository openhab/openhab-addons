/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.withings.internal.api.WithingsDataModel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractWithingsThingHandler extends BaseThingHandler implements WithingsThingHandler {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private final Logger logger = LoggerFactory.getLogger(AbstractWithingsThingHandler.class);

    public AbstractWithingsThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Optional<Bridge> bridgeOptional = getBridgeOptional();
        if (bridgeOptional.isPresent()) {
            updateStatusDependingFromBridgeStatus(bridgeOptional.get().getStatus());

            registerAtBridge();
        } else {
            logger.error("The bridge couldn't get found!");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            getBridgeHandlerOptional().ifPresent(bridge -> bridge.notifyThingHandler(this));
        }
    }

    @Override
    public final void updateData(WithingsDataModel model) {
        boolean isUpdated = updateThingData(model);

        if (isUpdated) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    protected abstract boolean updateThingData(WithingsDataModel model);

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateStatusDependingFromBridgeStatus(bridgeStatusInfo.getStatus());
    }

    protected DateTimeType createDateTimeType(Date date) {
        return new DateTimeType(DATE_FORMAT.format(date));
    }

    private void registerAtBridge() {
        Optional<WithingsBridgeHandler> bridgeHandlerOptional = getBridgeHandlerOptional();
        if (bridgeHandlerOptional.isPresent()) {
            bridgeHandlerOptional.get().registerThingHandler(this);
        } else {
            logger.error("The bridge handler couldn't get found!");
        }
    }

    private void updateStatusDependingFromBridgeStatus(ThingStatus bridgeStatus) {
        if (!ThingStatus.ONLINE.equals(bridgeStatus) && !ThingStatus.INITIALIZING.equals(bridgeStatus)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private Optional<Bridge> getBridgeOptional() {
        @Nullable
        Bridge bridge = super.getBridge();
        if (bridge != null) {
            return Optional.of(bridge);
        }
        return Optional.empty();
    }

    private Optional<WithingsBridgeHandler> getBridgeHandlerOptional() {
        return getBridgeOptional().map(bridge -> (WithingsBridgeHandler) bridge.getHandler());
    }
}

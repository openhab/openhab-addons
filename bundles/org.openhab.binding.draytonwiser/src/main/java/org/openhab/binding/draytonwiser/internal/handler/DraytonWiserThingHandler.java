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
package org.openhab.binding.draytonwiser.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.DraytonWiserRefreshListener;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApi;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Moved generic code from subclasses to this class
 */
@NonNullByDefault
abstract class DraytonWiserThingHandler<T> extends BaseThingHandler implements DraytonWiserRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @Nullable DraytonWiserApi api;
    private @Nullable T data;
    private @Nullable DraytonWiserDTO draytonWiserDTO;
    private @Nullable ScheduledFuture<?> handleCommandRefreshFuture;

    protected DraytonWiserThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final HeatHubHandler bridgeHandler = getHeatHubHandler();

        if (bridgeHandler == null) {
            api = null;
        } else {
            api = bridgeHandler.getApi();
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public final void handleCommand(final ChannelUID channelUID, final Command command) {
        final HeatHubHandler heatHubHandler = getHeatHubHandler();

        if (heatHubHandler == null) {
            return; // if null status will be updated to offline
        }
        if (command instanceof RefreshType) {
            heatHubHandler.refresh();
        } else {
            final DraytonWiserApi api = this.api;

            if (api != null && data != null) {
                try {
                    handleCommand(channelUID.getId(), command);
                    // cancel previous refresh, but wait for it to finish, so no forced cancel
                    disposehandleCommandRefreshFuture(false);
                    // update the state after the heathub has had time to react
                    handleCommandRefreshFuture = scheduler.schedule(heatHubHandler::refresh, 5, TimeUnit.SECONDS);
                } catch (final DraytonWiserApiException e) {
                    logger.warn("Failed to handle command {} for channel {}: {}", command, channelUID, e.getMessage());
                    logger.trace("DraytonWiserApiException", e);
                }
            }
        }
    }

    private void disposehandleCommandRefreshFuture(final boolean force) {
        final ScheduledFuture<?> future = handleCommandRefreshFuture;

        if (future != null) {
            future.cancel(force);
        }
    }

    @Override
    public final void dispose() {
        disposehandleCommandRefreshFuture(true);
    }

    /**
     * Performs the actual command. This method is only called when api and device cache are not null.
     *
     * @param channelId Channel id part of the Channel UID
     * @param command the command to perform
     * @throws DraytonWiserApiException
     */
    protected abstract void handleCommand(String channelId, Command command) throws DraytonWiserApiException;

    @Override
    public final void onRefresh(final DraytonWiserDTO draytonWiserDTO) {
        this.draytonWiserDTO = draytonWiserDTO;
        try {
            if (ThingHandlerHelper.isHandlerInitialized(this)) {
                data = api == null ? null : collectData(draytonWiserDTO);
                refresh();
                if (data == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "No data received");
                } else {
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                }
            }
        } catch (final RuntimeException | DraytonWiserApiException e) {
            logger.debug("Exception occurred during refresh: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Called to refresh the channels state.
     */
    protected abstract void refresh();

    /**
     * Conditionally updates the state. If no data or no api set the state will be set to UNDEF.
     *
     * @param channelId String id of the channel to update
     * @param stateFunction function to return the state, called when api and data are available
     */
    protected void updateState(final String channelId, final Supplier<State> stateFunction) {
        final State state = api == null || data == null ? UnDefType.UNDEF : stateFunction.get();

        updateState(channelId, state);
    }

    /**
     * Returns the handler specific data object only if all data is available.
     * If not all data is available it should return null.
     *
     * @param draytonWiserDTO data object with domain data as received from the hub
     * @return handler data object if available else null
     * @throws DraytonWiserApiException
     */
    protected abstract @Nullable T collectData(DraytonWiserDTO draytonWiserDTO) throws DraytonWiserApiException;

    protected DraytonWiserApi getApi() {
        final DraytonWiserApi api = this.api;

        if (api == null) {
            throw new IllegalStateException("API not set");
        }
        return api;
    }

    protected T getData() {
        final @Nullable T data = this.data;

        if (data == null) {
            throw new IllegalStateException("Data not set");
        }
        return data;
    }

    protected DraytonWiserDTO getDraytonWiserDTO() {
        final DraytonWiserDTO draytonWiserDTO = this.draytonWiserDTO;

        if (draytonWiserDTO == null) {
            throw new IllegalStateException("DraytonWiserDTO not set");
        }
        return draytonWiserDTO;
    }

    @Override
    public void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                final HeatHubHandler bridgeHandler = getHeatHubHandler();

                api = bridgeHandler == null ? null : bridgeHandler.getApi();
                updateStatus(ThingStatus.UNKNOWN);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private @Nullable HeatHubHandler getHeatHubHandler() {
        final Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        } else {
            return (HeatHubHandler) bridge.getHandler();
        }
    }
}

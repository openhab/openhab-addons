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
package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * KVVStopHandler represents a stop and holds information about the trains
 * which will arrive soon.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVStopHandler extends BaseThingHandler {

    @Nullable
    private ScheduledFuture<?> pollingJob;

    @Nullable
    private KVVBridgeHandler bridgeHandler;

    private KVVStopConfig config;

    private boolean wasOffline;

    public KVVStopHandler(final Thing thing) {
        super(thing);
        this.config = new KVVStopConfig();
        this.wasOffline = false;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        init(true);
        updateStatus(ThingStatus.ONLINE);
    }

    private synchronized void init(final boolean createChannels) {
        this.config = getConfigAs(KVVStopConfig.class);
        if (config.stopId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to get stop configuration");
            return;
        }

        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Failed to get bridge");
            return;
        }

        final KVVBridgeHandler bridgeHandler = (KVVBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "Failed to get bridge handler");
            return;
        }

        // create channels
        if (createChannels) {
            final ChannelTypeUID nameType = new ChannelTypeUID(this.thing.getBridgeUID().getBindingId(), "name");
            final ChannelTypeUID destType = new ChannelTypeUID(this.thing.getBridgeUID().getBindingId(), "destination");
            final ChannelTypeUID etaType = new ChannelTypeUID(this.thing.getBridgeUID().getBindingId(), "eta");

            final List<Channel> channels = new ArrayList<Channel>();
            for (int i = 0; i < bridgeHandler.getBridgeConfig().maxTrains; i++) {
                channels.add(ChannelBuilder.create(new ChannelUID(this.thing.getUID(), "train" + i + "-name"), "String")
                        .withType(nameType).build());
                channels.add(ChannelBuilder
                        .create(new ChannelUID(this.thing.getUID(), "train" + i + "-destination"), "String")
                        .withType(destType).build());
                channels.add(ChannelBuilder.create(new ChannelUID(this.thing.getUID(), "train" + i + "-eta"), "String")
                        .withType(etaType).build());
            }
            this.updateThing(this.editThing().withChannels(channels).build());

        }

        this.pollingJob = this.scheduler.scheduleWithFixedDelay(new UpdateTask(bridgeHandler, this.config), 0,
                bridgeHandler.getBridgeConfig().updateInterval, TimeUnit.SECONDS);

        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void dispose() {
        if (this.pollingJob != null) {
            this.pollingJob.cancel(false);
        }
    }

    /**
     * Updates the local list of departures.
     *
     * @param departures the new list of departures
     */
    private synchronized void setDepartures(final DepartureResult departures, final int maxTrains) {
        int i = 0;
        for (; i < departures.departures.size(); i++) {
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-name"),
                    new StringType(departures.departures.get(i).route));
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-destination"),
                    new StringType(departures.departures.get(i).destination));
            String eta = departures.departures.get(i).time;
            if (eta.equals("0")) {
                eta += " min";
            }
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-eta"), new StringType(eta));
        }
        for (; i < maxTrains; i++) {
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-name"), StringType.EMPTY);
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-destination"), StringType.EMPTY);
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-eta"), StringType.EMPTY);
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command == RefreshType.REFRESH) {
            init(false);
        }
    }

    /**
     * Holds a single {@link TimerTask} to fetch the latest departure data.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    @NonNullByDefault
    public class UpdateTask extends TimerTask {

        private final KVVBridgeHandler bridgeHandler;

        private final KVVStopConfig stopConfig;

        public UpdateTask(final KVVBridgeHandler bridgeHandler, final KVVStopConfig stopConfig) {
            this.bridgeHandler = bridgeHandler;
            this.stopConfig = stopConfig;
        }

        @Override
        public void run() {
            final DepartureResult departures = this.bridgeHandler.queryKVV(this.stopConfig);
            if (departures == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to KVV API");
            } else {
                if (wasOffline) {
                    updateStatus(ThingStatus.ONLINE);
                }
                setDepartures(departures, this.bridgeHandler.getBridgeConfig().maxTrains);
            }
        }
    }
}

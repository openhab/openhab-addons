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
package org.openhab.binding.nzwateralerts.internal.handler;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.CHANNEL_ALERTLEVEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.nzwateralerts.internal.NZWaterAlertsConfiguration;
import org.openhab.binding.nzwateralerts.internal.binder.NZWaterAlertsBinder;
import org.openhab.binding.nzwateralerts.internal.binder.NZWaterAlertsBinderListener;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * The {@link NZWaterAlertsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class NZWaterAlertsHandler extends BaseThingHandler implements NZWaterAlertsBinderListener {

    private @Nullable NZWaterAlertsConfiguration config = null;
    private HttpClient httpClient;
    private @Nullable NZWaterAlertsBinder binder = null;

    public NZWaterAlertsHandler(Thing thing, HttpClient httpClient) {
        super(thing);

        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final NZWaterAlertsBinder localBinder = binder;
        if (CHANNEL_ALERTLEVEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                if (localBinder != null) {
                    localBinder.update();
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(NZWaterAlertsConfiguration.class);

        NZWaterAlertsBinder localBinder = binder = new NZWaterAlertsBinder(httpClient, config, scheduler);

        updateStatus(ThingStatus.UNKNOWN);
        localBinder.registerListener(this);
    }

    @Override
    public void dispose() {
        NZWaterAlertsBinder localBinder = binder;
        if (localBinder != null) {
            localBinder.unregisterListener(this);
        }

        super.dispose();
    }

    @Override
    public void handleRemoval() {
        NZWaterAlertsBinder localBinder = binder;
        if (localBinder != null) {
            localBinder.unregisterListener(this);
        }

        super.handleRemoval();
    }

    @Override
    public void updateWaterLevel(int level) {
        if (level == -1) {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ALERTLEVEL), UnDefType.UNDEF);
        } else {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ALERTLEVEL), new DecimalType(level));
        }
    }

    @Override
    public void updateBindingStatus(ThingStatus thingStatus) {
        updateStatus(thingStatus);
    }

    @Override
    public void updateBindingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String description) {
        updateStatus(thingStatus, thingStatusDetail, description);
    }
}

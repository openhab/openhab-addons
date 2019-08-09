/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hpprinter.internal.handler;

import org.eclipse.jetty.client.HttpClient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hpprinter.internal.HPPrinterConfiguration;
import org.openhab.binding.hpprinter.internal.binder.HPPrinterBinder;
import org.openhab.binding.hpprinter.internal.binder.HPPrinterBinderEvent;

/**
 * The {@link HPPrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterHandler extends BaseThingHandler implements HPPrinterBinderEvent {

    private @Nullable HPPrinterConfiguration config = null;
    private @Nullable HttpClient httpClient = null;
    private @Nullable HPPrinterBinder binder = null;

    public HPPrinterHandler(Thing thing, @Nullable HttpClient httpClient) {
        super(thing);

        this.httpClient = httpClient;
    }

    @Override
    public void handleRemoval() {
        binder.close();

        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            binder.update();
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(HPPrinterConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        if (config != null && config.ipAddress != "") {
            binder = new HPPrinterBinder(this, httpClient, scheduler, config);
            binder.open();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "You must set an IP Address");
        }

    }

    @Override
    public void binderStatus(ThingStatus status) {
        updateStatus(status);
    }

    @Override
    public void binderChannel(String group, String channel, State state) {
        updateState(new ChannelUID(thing.getUID(), group, channel), state);
    }
}

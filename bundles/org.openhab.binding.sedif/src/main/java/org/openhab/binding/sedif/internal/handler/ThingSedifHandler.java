/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.handler;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingSedifHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingSedifHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingSedifHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable SedifHttpApi sedifApi;

    private @Nullable ScheduledFuture<?> pollingJob = null;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

    }

    @Override
    public synchronized void initialize() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    private void pollingCode() {
    }

}

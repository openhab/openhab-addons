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
package org.openhab.binding.tibber.internal;

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.binding.tibber.internal.history.TibberHistory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.osgi.framework.BundleContext;

/**
 * The {@link TibberHandlerMock} sets the PriceCalculator for unit testing.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Add history channel group
 * @author Bernd Weymann - Add history stub injection
 */
@NonNullByDefault
public class TibberHandlerMock extends TibberHandler {

    private @Nullable TibberHistory historyOverride;

    public TibberHandlerMock(Thing thing, HttpClient httpClient, CronScheduler cron, BundleContext bundleContext,
            TimeZoneProvider timeZoneProvider, StorageService storageService) {
        super(thing, httpClient, cron, bundleContext, timeZoneProvider, storageService);
    }

    public TibberHandlerMock() {
        super(mock(Thing.class), mock(HttpClient.class), mock(CronScheduler.class), mock(BundleContext.class),
                mock(TimeZoneProvider.class), mock(StorageService.class));
    }

    public void setPriceCalculator(PriceCalculator calc) {
        super.calculator = calc;
    }

    /**
     * Replaces the history instance created by {@link TibberHandler#initialize()} with a stub.
     */
    public void setHistoryOverride(TibberHistory history) {
        this.historyOverride = history;
    }

    @Override
    protected TibberHistory createHistory() {
        TibberHistory historyOverride = this.historyOverride;
        return historyOverride != null ? historyOverride : super.createHistory();
    }

    /**
     * Triggers the hourly stale history check for unit tests.
     */
    public void checkStaleHistory() {
        super.refreshStaleHistory();
    }

    @Override
    public void managePriceChannels(boolean add) {
        super.managePriceChannels(add);
    }
}

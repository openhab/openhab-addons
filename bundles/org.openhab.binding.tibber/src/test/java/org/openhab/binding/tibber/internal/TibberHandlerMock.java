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
package org.openhab.binding.tibber.internal;

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;
import org.osgi.framework.BundleContext;

/**
 * The {@link TibberHandlerMock} sets the PriceCalculator for unit testing.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHandlerMock extends TibberHandler {

    public TibberHandlerMock() {
        super(mock(Thing.class), mock(HttpClient.class), mock(CronScheduler.class), mock(BundleContext.class),
                mock(TimeZoneProvider.class));
    }

    public void setPriceCalculator(PriceCalculator calc) {
        super.calculator = calc;
    }
}

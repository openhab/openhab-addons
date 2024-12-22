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
package org.openhab.binding.smaenergymeter.internal.packet;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeter;

/**
 * Payload handler which defer publishing of meter data by given amount of time.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
@NonNullByDefault
public class ThrottlingPayloadHandler implements PayloadHandler {

    private final PayloadHandler delegate;
    private final long pollingPeriodMs;
    private long publishTime = 0;

    public ThrottlingPayloadHandler(PayloadHandler delegate, long pollingPeriodMs) {
        this.delegate = delegate;
        this.pollingPeriodMs = pollingPeriodMs;
    }

    @Override
    public void handle(EnergyMeter energyMeter) throws IOException {
        long ts = System.currentTimeMillis();
        if (publishTime <= ts) {
            delegate.handle(energyMeter);
            publishTime = ts + pollingPeriodMs;
        }
    }
}

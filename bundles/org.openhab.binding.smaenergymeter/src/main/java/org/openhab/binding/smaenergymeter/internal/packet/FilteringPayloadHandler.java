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
 * Payload handler which define acceptance criteria for received meter data.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
@NonNullByDefault
public class FilteringPayloadHandler implements PayloadHandler {

    private final PayloadHandler delegate;
    private final String serialNumber;

    public FilteringPayloadHandler(PayloadHandler delegate, String serialNumber) {
        this.delegate = delegate;
        this.serialNumber = serialNumber;
    }

    @Override
    public void handle(EnergyMeter energyMeter) throws IOException {
        if (this.serialNumber.equals(energyMeter.getSerialNumber())) {
            delegate.handle(energyMeter);
        }
    }
}

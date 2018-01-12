/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_LOG_INTERVAL_SET_REQUEST;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Sets the interval of historic power consumption and production measurements. These historic measurements are
 * returned by the {@link PowerBufferRequestMessage}.
 *
 * @author Wouter Born - Initial contribution
 */
public class PowerLogIntervalSetRequestMessage extends Message {

    private Duration consumptionInterval;
    private Duration productionInterval;

    public PowerLogIntervalSetRequestMessage(MACAddress macAddress, Duration consumptionInterval,
            Duration productionInterval) {
        super(POWER_LOG_INTERVAL_SET_REQUEST, macAddress);
        this.consumptionInterval = consumptionInterval;
        this.productionInterval = productionInterval;
    }

    @Override
    protected String payloadToHexString() {
        String consumptionIntervalHex = String.format("%04X", consumptionInterval.toMinutes());
        String productionIntervalHex = String.format("%04X", productionInterval.toMinutes());
        return consumptionIntervalHex + productionIntervalHex;
    }

}

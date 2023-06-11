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
package org.openhab.binding.enocean.internal.eep.A5_30;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_30_03 extends _4BSMessage {

    protected static final byte ALL_DIGITALPINS_HIGH = 0x0F;
    protected static final byte WAKEUPPIN_HIGH = 0x10;

    public A5_30_03() {
        super();

        this.supportsTeachInVariation3 = true;
    }

    public A5_30_03(ERP1Message packet) {
        super(packet);

        this.supportsTeachInVariation3 = true;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                double temp = (getDB_2Value() - 255) / -6.375;
                return new QuantityType<>(temp, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }
}

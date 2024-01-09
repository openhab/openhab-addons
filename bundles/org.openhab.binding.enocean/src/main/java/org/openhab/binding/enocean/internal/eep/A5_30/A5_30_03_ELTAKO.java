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
package org.openhab.binding.enocean.internal.eep.A5_30;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_SMOKEDETECTION;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * EEP used for Smoke Detectors by Eltako
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_30_03_ELTAKO extends A5_30_03 {

    protected static final byte ALARM_ON = 0x0F;
    protected static final byte ALARM_OFF = 0x1F;

    public A5_30_03_ELTAKO() {
        super();

        this.supportsTeachInVariation3 = true;
    }

    public A5_30_03_ELTAKO(ERP1Message packet) {
        super(packet);

        this.supportsTeachInVariation3 = true;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        byte db1 = getDB1();
        switch (channelId) {
            case CHANNEL_SMOKEDETECTION:
                return db1 == ALARM_ON ? OnOffType.ON : (db1 == ALARM_OFF ? OnOffType.OFF : UnDefType.UNDEF);
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}

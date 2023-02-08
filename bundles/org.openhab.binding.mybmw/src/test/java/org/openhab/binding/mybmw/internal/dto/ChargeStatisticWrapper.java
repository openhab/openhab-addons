/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import java.util.List;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeStatisticsContainer;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * The {@link ChargeStatisticWrapper} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ChargeStatisticWrapper {
    private ChargeStatisticsContainer chargeStatisticContainer;

    public ChargeStatisticWrapper(String content) {
        ChargeStatisticsContainer fromJson = Converter.getGson().fromJson(content, ChargeStatisticsContainer.class);
        if (fromJson != null) {
            chargeStatisticContainer = fromJson;
        } else {
            chargeStatisticContainer = new ChargeStatisticsContainer();
        }
    }

    /**
     * Test results auctomatically against json values
     *
     * @param channels
     * @param states
     * @return
     */
    public boolean checkResults(@Nullable List<ChannelUID> channels, @Nullable List<State> states) {
        assertNotNull(channels);
        assertNotNull(states);
        assertTrue(channels.size() == states.size(), "Same list sizes");
        for (int i = 0; i < channels.size(); i++) {
            checkResult(channels.get(i), states.get(i));
        }
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void checkResult(ChannelUID channelUID, State state) {
        String cUid = channelUID.getIdWithoutGroup();
        String gUid = channelUID.getGroupId();
        StringType st;
        DecimalType dt;
        QuantityType<Energy> qte;
        switch (cUid) {
            case TITLE:
                assertTrue(state instanceof StringType);
                st = (StringType) state;
                switch (gUid) {
                    case CHANNEL_GROUP_CHARGE_STATISTICS:
                        assertEquals(chargeStatisticContainer.description, st.toString(), "Statistics name");
                        break;
                    default:
                        assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                        break;
                }
                break;
            case SESSIONS:
                assertTrue(state instanceof DecimalType);
                dt = ((DecimalType) state);
                assertEquals(chargeStatisticContainer.statistics.numberOfChargingSessions, dt.intValue(),
                        "Charge Sessions");
                break;
            case ENERGY:
                assertTrue(state instanceof QuantityType);
                qte = ((QuantityType) state);
                assertEquals(Units.KILOWATT_HOUR, qte.getUnit(), "kwh");
                assertEquals(chargeStatisticContainer.statistics.totalEnergyCharged, qte.intValue(), "Energy");
                break;
            default:
                // fail in case of unknown update
                assertFalse(true, "Channel " + channelUID + " " + state + " not found");
                break;
        }
    }
}

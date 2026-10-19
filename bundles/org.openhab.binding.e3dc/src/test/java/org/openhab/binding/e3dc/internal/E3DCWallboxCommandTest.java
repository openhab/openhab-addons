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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;

/**
 * Tests the commands on the wallbox channels that report a value.
 *
 * <p>
 * A wallbox value is written as the parameter of the request that names it, so the channel that reports a
 * value is the one that sets it and no second channel for the same value exists. The request therefore has
 * to carry the index of the addressed wallbox and that one parameter.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCWallboxCommandTest {

    private static final int WALLBOX_INDEX = 2;

    private static E3DCCommandSpec spec(String channel) {
        E3DCCommandSpec spec = E3DCCommandSpec.wallbox().spec(E3DCBindingConstants.GROUP_WALLBOX, channel);
        assertNotNull(spec, "the wallbox channel " + channel + " must be writable");
        return spec;
    }

    /** Builds a wallbox request that must be constructible, sent as one element. */
    private static RscpValue command(E3DCCommandHandler handler, E3DCCommandSpec spec, Command value) {
        List<RscpValue> request = handler.buildRequest(spec, value, WALLBOX_INDEX);
        assertNotNull(request, "the command must be representable: " + value);
        assertEquals(1, request.size(), "a command is sent as one element");
        return request.get(0);
    }

    @Test
    void theChargingModeIsWrittenAsItsOwnParameter() {
        RscpValue request = command(new E3DCCommandHandler(), spec("sun-mode"), OnOffType.ON);

        assertEquals(RscpTag.WB_REQ_DATA, request.getTag());
        Number index = request.findNumber(RscpTag.WB_INDEX);
        assertNotNull(index, "the addressed wallbox must be selected by index");
        assertEquals(WALLBOX_INDEX, index.intValue());
        Number mode = request.findNumber(RscpTag.WB_REQ_SET_SUN_MODE_ACTIVE);
        assertNotNull(mode, "the request names the parameter of the field");
        assertEquals(1, mode.intValue());
        assertEquals(0, command(new E3DCCommandHandler(), spec("sun-mode"), OnOffType.OFF)
                .findNumber(RscpTag.WB_REQ_SET_SUN_MODE_ACTIVE));
    }

    @Test
    void theMaximumCurrentIsWrittenAsItsOwnParameter() {
        RscpValue request = command(new E3DCCommandHandler(), spec("max-charge-current"), new DecimalType(10));

        Number current = request.findNumber(RscpTag.WB_REQ_SET_MAX_CHARGE_CURRENT);
        assertNotNull(current, "the request names the parameter of the field");
        assertEquals(10, current.intValue());
    }

    /**
     * A write names one parameter, so it does not depend on the values the other fields of the wallbox
     * happen to have.
     */
    @Test
    void aWriteDoesNotNeedTheStateOfTheOtherFields() {
        E3DCCommandHandler handler = new E3DCCommandHandler();

        assertNotNull(handler.buildRequest(spec("sun-mode"), OnOffType.ON, WALLBOX_INDEX));
        assertNotNull(handler.buildRequest(spec("max-charge-current"), new DecimalType(10), WALLBOX_INDEX));
    }

    /**
     * One channel per value: the channel that reports the value is the one that sets it, so the writable
     * channels are the values the wallbox reports and accepts, and there is no second channel for a value.
     */
    @Test
    void theReportedValuesAreTheWritableChannels() {
        E3DCCommandRegistry registry = E3DCCommandSpec.wallbox();

        assertEquals(6, registry.all().size(), "one channel per settable value and no separate set channel");
        for (String channel : List.of("sun-mode", "suspended", "auto-phase-switch", "min-charge-current",
                "max-charge-current", "number-phases")) {
            assertNotNull(registry.spec(E3DCBindingConstants.GROUP_WALLBOX, channel),
                    "the channel " + channel + " must be writable");
        }
    }

    /** An item with a unit sends a quantity, which must be written as the plain number of amperes. */
    @Test
    void aCurrentWithAUnitIsWrittenInAmperes() {
        E3DCCommandHandler handler = new E3DCCommandHandler();

        RscpValue request = command(handler, spec("max-charge-current"), new QuantityType<>(16, Units.AMPERE));
        Number current = request.findNumber(RscpTag.WB_REQ_SET_MAX_CHARGE_CURRENT);
        assertNotNull(current);
        assertEquals(16, current.intValue());

        RscpValue minimum = command(handler, spec("min-charge-current"),
                new QuantityType<>(6000, MetricPrefix.MILLI(Units.AMPERE)));
        Number minimumCurrent = minimum.findNumber(RscpTag.WB_REQ_SET_MIN_CHARGE_CURRENT);
        assertNotNull(minimumCurrent);
        assertEquals(6, minimumCurrent.intValue());
    }

    @Test
    void aQuantityOfAnotherKindIsRejected() {
        E3DCCommandHandler handler = new E3DCCommandHandler();

        assertNull(handler.buildRequest(spec("max-charge-current"), new QuantityType<>(16, Units.VOLT), WALLBOX_INDEX));
    }

    @Test
    void aCurrentThatDoesNotFitIntoOneByteIsRejected() {
        E3DCCommandHandler handler = new E3DCCommandHandler();

        assertNull(handler.buildRequest(spec("max-charge-current"), new DecimalType(256), WALLBOX_INDEX));
        assertNull(handler.buildRequest(spec("max-charge-current"), new DecimalType(-1), WALLBOX_INDEX));
    }
}

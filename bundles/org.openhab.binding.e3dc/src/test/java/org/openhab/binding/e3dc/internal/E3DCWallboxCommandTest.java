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
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests the packed wallbox command.
 *
 * <p>
 * A wallbox is not configured field by field: it expects one complete command payload, so the byte
 * layout is the contract. These tests pin every offset, because a shifted byte changes the charging
 * behaviour of the device rather than failing loudly.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCWallboxCommandTest {

    private static final int WALLBOX_INDEX = 2;

    /** Builds a status response the way the device reports the wallbox. */
    private static RscpValue statusResponse(boolean sunMode, int maxCurrent) {
        byte[] externData = new byte[10];
        // the status byte carries the mode in bit 7, the current follows in the next byte
        externData[2] = (byte) (sunMode ? 0x80 : 0x00);
        externData[3] = (byte) maxCurrent;
        return RscpValue.container(0, List.of(RscpValue.of(RscpTag.WB_EXTERN_DATA, RscpType.BYTEARRAY, externData)));
    }

    private static E3DCCommandHandler handlerWithStatus(boolean sunMode, int maxCurrent) {
        E3DCCommandHandler handler = new E3DCCommandHandler();
        handler.updateFromResponse(statusResponse(sunMode, maxCurrent));
        return handler;
    }

    private static E3DCCommandSpec spec(String channel) {
        E3DCCommandSpec spec = E3DCCommandSpec.wallbox().spec(E3DCBindingConstants.GROUP_WALLBOX, channel);
        assertNotNull(spec, "the wallbox channel " + channel + " must be writable");
        return spec;
    }

    /** Builds a wallbox request that must be constructible. */
    private static RscpValue command(E3DCCommandHandler handler, E3DCCommandSpec spec,
            org.openhab.core.types.Command value) {
        RscpValue request = handler.buildRequest(spec, value, WALLBOX_INDEX);
        assertNotNull(request, "the command must be representable: " + value);
        return request;
    }

    /** The payload of a built wallbox request. */
    private static byte[] payloadOf(RscpValue request) {
        RscpValue data = request.find(RscpTag.WB_EXTERN_DATA);
        assertNotNull(data, "the request must carry the packed payload");
        byte[] payload = data.asBytes();
        assertNotNull(payload, "the payload must be a byte array");
        return payload;
    }

    // ------------------------------------------------------------------ packed layout

    @Test
    void thePackedCommandHasTheFixedLengthTheDeviceExpects() {
        E3DCCommandHandler handler = handlerWithStatus(false, 16);
        byte[] payload = payloadOf(command(handler, spec("set-sun-mode"), OnOffType.ON));

        assertEquals(E3DCWallboxCommand.PAYLOAD_LENGTH, payload.length);
        assertEquals(E3DCWallboxCommand.PAYLOAD_LENGTH,
                payloadOf(command(handler, spec("set-max-charge-current"), new DecimalType(10))).length);
    }

    @Test
    void theSunModeFlagIsWrittenToTheFirstByte() {
        E3DCCommandHandler handler = handlerWithStatus(false, 16);

        assertEquals(1, payloadOf(command(handler, spec("set-sun-mode"), OnOffType.ON))[0],
                "sun mode is selected with 1");
        assertEquals(2, payloadOf(command(handler, spec("set-sun-mode"), OnOffType.OFF))[0],
                "normal mode is selected with 2, not with 0");
    }

    @Test
    void theChargingCurrentIsWrittenToTheSecondByte() {
        E3DCCommandHandler handler = handlerWithStatus(false, 16);
        byte[] payload = payloadOf(command(handler, spec("set-max-charge-current"), new DecimalType(20)));

        assertEquals(20, payload[1]);
    }

    @Test
    void theChargingCurrentIsClampedToTheRangeTheDeviceAccepts() {
        E3DCCommandHandler handler = handlerWithStatus(false, 16);

        assertEquals(E3DCWallboxCommand.MIN_CURRENT,
                payloadOf(command(handler, spec("set-max-charge-current"), new DecimalType(0)))[1],
                "zero ampere is not a valid charging current");
        assertEquals(E3DCWallboxCommand.MAX_CURRENT,
                payloadOf(command(handler, spec("set-max-charge-current"), new DecimalType(99)))[1],
                "more than 32 ampere is not accepted");
    }

    @Test
    void onlyTheFirstFiveBytesAreUsedAndTheRestStayZero() {
        E3DCCommandHandler handler = handlerWithStatus(true, 16);
        byte[] payload = payloadOf(command(handler, spec("set-sun-mode"), OnOffType.ON));

        for (int i = 5; i < payload.length; i++) {
            assertEquals(0, payload[i], "byte " + i + " is reserved and must stay zero");
        }
        assertEquals(0, payload[2], "byte 2 is reserved and must stay zero");
        assertEquals(0, payload[3], "byte 3 is reserved and must stay zero");
    }

    // ------------------------------------------------------------------ carrying the other fields

    @Test
    void aWriteCarriesTheCurrentValuesOfTheFieldsItDoesNotChange() {
        E3DCCommandHandler handler = handlerWithStatus(true, 24);

        // change only the current, the sun mode has to be carried over
        byte[] payload = payloadOf(command(handler, spec("set-max-charge-current"), new DecimalType(10)));
        assertEquals(1, payload[0], "sun mode was active and must stay active");
        assertEquals(10, payload[1]);
    }

    @Test
    void switchingSunModeOffKeepsTheConfiguredCurrent() {
        E3DCCommandHandler handler = handlerWithStatus(true, 24);

        byte[] payload = payloadOf(command(handler, spec("set-sun-mode"), OnOffType.OFF));
        assertEquals(2, payload[0], "normal mode");
        assertEquals(24, payload[1], "the configured current must survive");
    }

    // ------------------------------------------------------------------ request shape and safety

    @Test
    void theCommandIsNestedInTheWallboxDataContainerWithItsIndex() {
        E3DCCommandHandler handler = handlerWithStatus(false, 16);
        RscpValue request = command(handler, spec("set-sun-mode"), OnOffType.ON);
        assertNotNull(request);

        RscpValue wallboxData = request.find(RscpTag.WB_REQ_DATA);
        assertNotNull(wallboxData, "the command must target the wallbox data container");
        Number reportedIndex = wallboxData.findNumber(RscpTag.WB_INDEX);
        assertNotNull(reportedIndex, "the addressed wallbox must be selected by index");
        assertEquals(WALLBOX_INDEX, reportedIndex.intValue());
        assertNotNull(request.find(RscpTag.WB_REQ_SET_EXTERN), "the packed command uses the extern container");
        Number reportedLength = request.findNumber(RscpTag.WB_EXTERN_DATA_LEN);
        assertNotNull(reportedLength, "the device needs the payload length");
        assertEquals(E3DCWallboxCommand.PAYLOAD_LENGTH, reportedLength.intValue());
    }

    @Test
    void aWriteIsRefusedWhileTheWallboxStateIsUnknown() {
        E3DCCommandHandler handler = new E3DCCommandHandler();

        assertNull(handler.buildRequest(spec("set-sun-mode"), OnOffType.ON, WALLBOX_INDEX),
                "without a known state the remaining fields would silently change");
        assertNull(handler.buildRequest(spec("set-max-charge-current"), new DecimalType(10), WALLBOX_INDEX));
    }

    @Test
    void theReportedStateIsReadFromTheSameOffsetsTheCommandUses() {
        E3DCCommandHandler handler = handlerWithStatus(true, 18);

        E3DCWallboxCommand state = handler.getWallboxState();
        assertNotNull(state);
        assertTrue(state.sunMode());
        assertEquals(18, state.maxCurrent());
    }

    @Test
    void theWritableWallboxChannelsAreRegistered() {
        E3DCCommandRegistry registry = E3DCCommandSpec.wallbox();

        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_WALLBOX, "set-sun-mode"));
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_WALLBOX, "set-max-charge-current"));
    }

    @Test
    void everyWallboxChannelProducesAPackedCommand() {
        for (E3DCCommandSpec spec : E3DCCommandSpec.wallbox().all()) {
            assertTrue(spec.isWallboxCommand(), RscpTag.nameOf(spec.tag()) + " must use the packed form");
        }
    }
}

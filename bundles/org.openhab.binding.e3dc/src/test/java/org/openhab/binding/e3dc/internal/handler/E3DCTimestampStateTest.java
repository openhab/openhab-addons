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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests how a moment reported by the device becomes the state of a DateTime channel.
 *
 * <p>
 * The start of the last manual charge is the only moment the binding publishes. The device may report it in
 * its timestamp type or as a plain number, so each shape is pinned here.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCTimestampStateTest {

    private static final long SECONDS = 1_761_428_400L;

    private static RscpValue timestamp(long seconds) {
        return RscpValue.timestamp(RscpTag.EMS_MANUAL_CHARGE_LASTSTART, Instant.ofEpochSecond(seconds));
    }

    private static Instant instantOf(State state) {
        return ((DateTimeType) state).getInstant();
    }

    @Test
    void aTimestampValueIsReadAsTheMomentItNames() {
        State state = E3DCThingHandler.timestampState(timestamp(SECONDS));

        assertNotNull(state);
        assertEquals(Instant.ofEpochSecond(SECONDS), instantOf(state));
    }

    @Test
    void aPlainNumberIsTakenAsSeconds() {
        State state = E3DCThingHandler
                .timestampState(RscpValue.of(RscpTag.EMS_MANUAL_CHARGE_LASTSTART, RscpType.UINT64, SECONDS));

        assertNotNull(state);
        assertEquals(Instant.ofEpochSecond(SECONDS), instantOf(state));
    }

    @Test
    void aNumberTooLargeForSecondsIsTakenAsMilliseconds() {
        State state = E3DCThingHandler
                .timestampState(RscpValue.of(RscpTag.EMS_MANUAL_CHARGE_LASTSTART, RscpType.UINT64, SECONDS * 1000));

        assertNotNull(state);
        assertEquals(Instant.ofEpochSecond(SECONDS), instantOf(state));
    }

    @Test
    void aMomentOfZeroIsUndefinedBecauseNothingHappenedYet() {
        assertEquals(UnDefType.UNDEF, E3DCThingHandler.timestampState(timestamp(0)));
        assertEquals(UnDefType.UNDEF, E3DCThingHandler
                .timestampState(RscpValue.of(RscpTag.EMS_MANUAL_CHARGE_LASTSTART, RscpType.UINT32, 0L)));
    }

    @Test
    void aMomentBeforeTheYear2000IsUndefined() {
        assertEquals(UnDefType.UNDEF, E3DCThingHandler.timestampState(timestamp(86_400)));
        assertEquals(UnDefType.UNDEF, E3DCThingHandler.timestampState(timestamp(-1)));
    }

    @Test
    void aPlaceholderFarInTheFutureIsUndefined() {
        // the device answered a charge that never started with its placeholder, 290309-12-21 20:58:50 local time
        RscpValue placeholder = RscpValue.timestamp(RscpTag.EMS_MANUAL_CHARGE_LASTSTART,
                Instant.ofEpochSecond(9_099_130_622_330L, 224_192_000));

        assertEquals(UnDefType.UNDEF, E3DCThingHandler.timestampState(placeholder));
        assertEquals(UnDefType.UNDEF, E3DCThingHandler
                .timestampState(RscpValue.of(RscpTag.EMS_MANUAL_CHARGE_LASTSTART, RscpType.UINT64, Long.MAX_VALUE)));
    }

    @Test
    void aValueThatIsNoMomentCarriesNoState() {
        assertNull(E3DCThingHandler.timestampState(RscpValue.bool(RscpTag.EMS_MANUAL_CHARGE_ACTIVE, true)));
        assertNull(E3DCThingHandler.timestampState(RscpValue.none(RscpTag.EMS_MANUAL_CHARGE_LASTSTART)));
    }
}

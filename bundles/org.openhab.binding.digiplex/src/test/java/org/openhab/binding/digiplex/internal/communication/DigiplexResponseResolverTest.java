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
package org.openhab.binding.digiplex.internal.communication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.digiplex.internal.communication.events.GenericEvent;

/**
 * Tests for {@link DigiplexResponseResolver}
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DigiplexResponseResolverTest {
    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsErroneousResponseWhenMessageIsMalformed")
    void resolveResponseReturnsErroneousResponseWhenMessageIsMalformed(String message) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(ErroneousResponse.class)));
        if (actual instanceof ErroneousResponse erroneousResponse) {
            assertThat(erroneousResponse.message, is(equalTo(message)));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsErroneousResponseWhenMessageIsMalformed() {
        return Stream.of( //
                Arguments.of("CO&"), Arguments.of("ZL&fail"), Arguments.of("ZL12"), Arguments.of("AL&fail"),
                Arguments.of("AL12"), Arguments.of("RZZZ3COOOO&fail"), Arguments.of("RZ123C"),
                Arguments.of("RZ123COOO"), Arguments.of("RA&fail"), Arguments.of("RA123DOOXOO"),
                Arguments.of("AA&fail"), Arguments.of("GGGGGGGGGGGG"), Arguments.of("G1234567890"));
    }

    @Test
    void resolveResponseReturnsCommunicationStatusSuccessWhenWellformed() {
        String message = "CO&ok";
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(CommunicationStatus.class)));
        if (actual instanceof CommunicationStatus communicationStatus) {
            assertThat(communicationStatus.success, is(true));
        }
    }

    @Test
    void resolveResponseReturnsCommunicationStatusFailureWhenMessageContainsFail() {
        String message = "CO&fail";
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(CommunicationStatus.class)));
        if (actual instanceof CommunicationStatus communicationStatus) {
            assertThat(communicationStatus.success, is(false));
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsZoneLabelResponse")
    void resolveResponseReturnsZoneLabelResponse(String message, boolean expectedSuccess, int expectedZoneNo,
            String expectedName) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(ZoneLabelResponse.class)));
        if (actual instanceof ZoneLabelResponse zoneLabelResponse) {
            assertThat(zoneLabelResponse.success, is(expectedSuccess));
            assertThat(zoneLabelResponse.zoneNo, is(expectedZoneNo));
            assertThat(zoneLabelResponse.zoneName, is(expectedName));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsZoneLabelResponse() {
        return Stream.of( //
                Arguments.of("ZL123", true, 123, ""), Arguments.of("ZL123test ", true, 123, "test"),
                Arguments.of("ZL123&fail", false, 123, null), Arguments.of("ZL123test&fail", false, 123, null));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsAreaLabelResponse")
    void resolveResponseReturnsAreaLabelResponse(String message, boolean expectedSuccess, int expectedAreaNo,
            String expectedName) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(AreaLabelResponse.class)));
        if (actual instanceof AreaLabelResponse areaLabelResponse) {
            assertThat(areaLabelResponse.success, is(expectedSuccess));
            assertThat(areaLabelResponse.areaNo, is(expectedAreaNo));
            assertThat(areaLabelResponse.areaName, is(expectedName));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsAreaLabelResponse() {
        return Stream.of( //
                Arguments.of("AL123", true, 123, ""), Arguments.of("AL123test ", true, 123, "test"),
                Arguments.of("AL123&fail", false, 123, null), Arguments.of("AL123test&fail", false, 123, null));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsZoneStatusResponse")
    void resolveResponseReturnsZoneStatusResponse(String message, boolean expectedSuccess, int expectedZoneNo,
            ZoneStatus expectedZoneStatus, boolean expectedAlarm, boolean expectedFireAlarm,
            boolean expectedSupervisionLost, boolean expectedLowBattery) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(ZoneStatusResponse.class)));
        if (actual instanceof ZoneStatusResponse zoneStatusResponse) {
            assertThat(zoneStatusResponse.success, is(expectedSuccess));
            assertThat(zoneStatusResponse.zoneNo, is(expectedZoneNo));
            assertThat(zoneStatusResponse.status, is(expectedZoneStatus));
            assertThat(zoneStatusResponse.alarm, is(expectedAlarm));
            assertThat(zoneStatusResponse.fireAlarm, is(expectedFireAlarm));
            assertThat(zoneStatusResponse.supervisionLost, is(expectedSupervisionLost));
            assertThat(zoneStatusResponse.lowBattery, is(expectedLowBattery));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsZoneStatusResponse() {
        return Stream.of( //
                Arguments.of("RZ123COOOO", true, 123, ZoneStatus.CLOSED, false, false, false, false),
                Arguments.of("RZ123OOOOO", true, 123, ZoneStatus.OPEN, false, false, false, false),
                Arguments.of("RZ123TOOOO", true, 123, ZoneStatus.TAMPERED, false, false, false, false),
                Arguments.of("RZ123FOOOO", true, 123, ZoneStatus.FIRE_LOOP_TROUBLE, false, false, false, false),
                Arguments.of("RZ123uOOOO", true, 123, ZoneStatus.UNKNOWN, false, false, false, false),
                Arguments.of("RZ123cOOOO", true, 123, ZoneStatus.UNKNOWN, false, false, false, false),
                Arguments.of("RZ123cXOOO", true, 123, ZoneStatus.UNKNOWN, true, false, false, false),
                Arguments.of("RZ123cOXOO", true, 123, ZoneStatus.UNKNOWN, false, true, false, false),
                Arguments.of("RZ123cOOXO", true, 123, ZoneStatus.UNKNOWN, false, false, true, false),
                Arguments.of("RZ123cOOOX", true, 123, ZoneStatus.UNKNOWN, false, false, false, true),
                Arguments.of("RZ123&fail", false, 123, null, false, false, false, false),
                Arguments.of("RZ123COOOO&fail", false, 123, null, false, false, false, false));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsAreaStatusResponse")
    void resolveResponseReturnsAreaStatusResponse(String message, boolean expectedSuccess, int expectedAreaNo,
            AreaStatus expectedAreaStatus, boolean expectedZoneInMemory, boolean expectedTrouble, boolean expectedReady,
            boolean expectedInProgramming, boolean expectedAlarm, boolean expectedStrobe) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(AreaStatusResponse.class)));
        if (actual instanceof AreaStatusResponse areaStatusResponse) {
            assertThat(areaStatusResponse.success, is(expectedSuccess));
            assertThat(areaStatusResponse.areaNo, is(expectedAreaNo));
            assertThat(areaStatusResponse.status, is(expectedAreaStatus));
            assertThat(areaStatusResponse.zoneInMemory, is(expectedZoneInMemory));
            assertThat(areaStatusResponse.trouble, is(expectedTrouble));
            assertThat(areaStatusResponse.ready, is(expectedReady));
            assertThat(areaStatusResponse.inProgramming, is(expectedInProgramming));
            assertThat(areaStatusResponse.alarm, is(expectedAlarm));
            assertThat(areaStatusResponse.strobe, is(expectedStrobe));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsAreaStatusResponse() {
        return Stream.of( //
                Arguments.of("RA123DOOXOOO", true, 123, AreaStatus.DISARMED, false, false, false, false, false, false),
                Arguments.of("RA123AOOXOOO", true, 123, AreaStatus.ARMED, false, false, false, false, false, false),
                Arguments.of("RA123FOOXOOO", true, 123, AreaStatus.ARMED_FORCE, false, false, false, false, false,
                        false),
                Arguments.of("RA123SOOXOOO", true, 123, AreaStatus.ARMED_STAY, false, false, false, false, false,
                        false),
                Arguments.of("RA123IOOXOOO", true, 123, AreaStatus.ARMED_INSTANT, false, false, false, false, false,
                        false),
                Arguments.of("RA123uOOXOOO", true, 123, AreaStatus.UNKNOWN, false, false, false, false, false, false),
                Arguments.of("RA123dOOXOOO", true, 123, AreaStatus.UNKNOWN, false, false, false, false, false, false),
                Arguments.of("RA123dXOXOOO", true, 123, AreaStatus.UNKNOWN, true, false, false, false, false, false),
                Arguments.of("RA123dOXxOOO", true, 123, AreaStatus.UNKNOWN, false, true, false, false, false, false),
                Arguments.of("RA123dOOOOOO", true, 123, AreaStatus.UNKNOWN, false, false, true, false, false, false),
                Arguments.of("RA123dOOXXOO", true, 123, AreaStatus.UNKNOWN, false, false, false, true, false, false),
                Arguments.of("RA123dOOXOXO", true, 123, AreaStatus.UNKNOWN, false, false, false, false, true, false),
                Arguments.of("RA123dOOXOOX", true, 123, AreaStatus.UNKNOWN, false, false, false, false, false, true),
                Arguments.of("RA123&fail", false, 123, null, false, false, false, false, false, false),
                Arguments.of("RA123DOOXOOO&fail", false, 123, null, false, false, false, false, false, false));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForResolveResponseReturnsAreaArmDisarmResponse")
    void resolveResponseReturnsAreaArmDisarmResponse(String message, boolean expectedSuccess, int expectedAreaNo,
            ArmDisarmType expectedType) {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse(message);
        assertThat(actual, is(instanceOf(AreaArmDisarmResponse.class)));
        if (actual instanceof AreaArmDisarmResponse armDisarmResponse) {
            assertThat(armDisarmResponse.success, is(expectedSuccess));
            assertThat(armDisarmResponse.areaNo, is(expectedAreaNo));
            assertThat(armDisarmResponse.type, is(expectedType));
        }
    }

    private static Stream<Arguments> provideTestCasesForResolveResponseReturnsAreaArmDisarmResponse() {
        return Stream.of( //
                Arguments.of("AA123", true, 123, ArmDisarmType.ARM),
                Arguments.of("AQ123", true, 123, ArmDisarmType.QUICK_ARM),
                Arguments.of("AD123", true, 123, ArmDisarmType.DISARM),
                Arguments.of("AA123&fail", false, 123, ArmDisarmType.ARM),
                Arguments.of("AQ123&fail", false, 123, ArmDisarmType.QUICK_ARM),
                Arguments.of("AD123&fail", false, 123, ArmDisarmType.DISARM));
    }

    @Test
    void resolveResponseReturnsGenericEventWhenWellformed() {
        DigiplexResponse actual = DigiplexResponseResolver.resolveResponse("G123 456 789");
        assertThat(actual, is(instanceOf(GenericEvent.class)));
        if (actual instanceof GenericEvent genericEvent) {
            assertThat(genericEvent.getEventGroup(), is(123));
            assertThat(genericEvent.getEventNumber(), is(456));
            assertThat(genericEvent.getAreaNo(), is(789));
        }
    }
}

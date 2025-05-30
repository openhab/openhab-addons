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
package org.openhab.binding.boschshc.internal.console;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SoftwareUpdateState;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;

/**
 * Unit tests for Console command to list Bosch SHC devices and openhab support.
 *
 * @author Gerd Zanker - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class BoschShcCommandExtensionTest {

    private @NonNullByDefault({}) BoschShcCommandExtension fixture;

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;

    @BeforeEach
    void setUp() {
        fixture = new BoschShcCommandExtension(thingRegistry);
    }

    @Test
    void execute() {
        // only sanity checks, content is tested with the functions called by execute
        Console consoleMock = mock(Console.class);
        when(thingRegistry.getAll()).thenReturn(Collections.emptyList());

        fixture.execute(new String[] {}, consoleMock);
        verify(consoleMock, times(5)).printUsage(any());
        fixture.execute(new String[] { "" }, consoleMock);
        verify(consoleMock, times(10)).printUsage(any());

        fixture.execute(new String[] { BoschShcCommandExtension.SHOW_BINDINGINFO }, consoleMock);
        verify(consoleMock, atLeastOnce()).print(any());
        fixture.execute(new String[] { BoschShcCommandExtension.SHOW_DEVICES }, consoleMock);
        verify(consoleMock, atLeastOnce()).print(any());
        fixture.execute(new String[] { BoschShcCommandExtension.SHOW_SERVICES }, consoleMock);
        verify(consoleMock, atLeastOnce()).print(any());

        fixture.execute(new String[] { BoschShcCommandExtension.GET_BRIDGEINFO }, consoleMock);
        verify(consoleMock, atLeastOnce()).print(any());
        fixture.execute(new String[] { BoschShcCommandExtension.GET_DEVICES }, consoleMock);
        verify(consoleMock, atLeastOnce()).print(any());
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getBoschShcAndExecutionAndTimeoutExceptionArguments()")
    void executeHandleExceptions(Exception exception)
            throws InterruptedException, BoschSHCException, ExecutionException, TimeoutException {
        Console console = mock(Console.class);
        Bridge bridge = mock(Bridge.class);
        BridgeHandler bridgeHandler = mock(BridgeHandler.class);
        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridgeHandler.getPublicInformation()).thenThrow(exception);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        List<Thing> things = List.of(bridge);
        when(thingRegistry.getAll()).thenReturn(things);

        fixture.execute(new String[] { BoschShcCommandExtension.GET_BRIDGEINFO }, console);

        verify(console).print(anyString());
    }

    @Test
    void getCompleter() {
        assertThat(fixture.getCompleter(), is(fixture));
    }

    @Test
    void getUsages() {
        List<String> strings = fixture.getUsages();
        assertThat(strings.size(), is(5));
        assertThat(strings.get(0), is("boschshc showBindingInfo - list detailed information about this binding"));
        assertThat(strings.get(1), is("boschshc showDevices - list all devices supported by this binding"));
    }

    @Test
    void complete() {
        ArrayList<String> candidates = new ArrayList<>();
        assertThat(fixture.complete(new String[] { "" }, 1, 0, candidates), is(false));
        assertThat(fixture.complete(new String[] { "" }, 0, 0, candidates), is(true));
        // for empty arguments, the completer suggest all usage commands
        assertThat(candidates.size(), is(fixture.getUsages().size()));
    }

    @Test
    void printBridgeInfo() throws BoschSHCException, ExecutionException, InterruptedException, TimeoutException {
        // no bridge
        when(thingRegistry.getAll()).thenReturn(Collections.emptyList());
        assertThat(fixture.buildBridgeInfo(), is(""));

        // one bridge
        PublicInformation publicInformation = new PublicInformation();
        publicInformation.shcGeneration = "Gen-T";
        publicInformation.shcIpAddress = "1.2.3.4";
        publicInformation.softwareUpdateState = new SoftwareUpdateState();
        Bridge mockBridge = mock(Bridge.class);
        when(mockBridge.getLabel()).thenReturn("TestLabel");
        BridgeHandler mockBridgeHandler = mock(BridgeHandler.class);
        when(mockBridgeHandler.getThing()).thenReturn(mockBridge);
        when(mockBridgeHandler.getPublicInformation()).thenReturn(publicInformation);
        Thing mockBridgeThing = mock(Thing.class);
        when(mockBridgeThing.getHandler()).thenReturn(mockBridgeHandler);
        when(thingRegistry.getAll()).thenReturn(Collections.singletonList(mockBridgeThing));
        assertThat(fixture.buildBridgeInfo(),
                allOf(containsString("Bridge: TestLabel"), containsString("access possible: false"),
                        containsString("SHC Generation: Gen-T"), containsString("IP Address: 1.2.3.4")));

        // two bridges
        PublicInformation publicInformation2 = new PublicInformation();
        publicInformation2.shcGeneration = "Gen-U";
        publicInformation2.shcIpAddress = "11.22.33.44";
        publicInformation2.softwareUpdateState = new SoftwareUpdateState();
        Bridge mockBridge2 = mock(Bridge.class);
        when(mockBridge2.getLabel()).thenReturn("Bridge  2");
        BridgeHandler mockBridgeHandler2 = mock(BridgeHandler.class);
        when(mockBridgeHandler2.getThing()).thenReturn(mockBridge2);
        when(mockBridgeHandler2.getPublicInformation()).thenReturn(publicInformation2);
        Thing mockBridgeThing2 = mock(Thing.class);
        when(mockBridgeThing2.getHandler()).thenReturn(mockBridgeHandler2);
        when(thingRegistry.getAll()).thenReturn(Arrays.asList(mockBridgeThing, mockBridgeThing2));
        assertThat(fixture.buildBridgeInfo(),
                allOf(containsString("Bridge: TestLabel"), containsString("access possible: false"),
                        containsString("SHC Generation: Gen-T"), containsString("IP Address: 1.2.3.4"),
                        containsString("Bridge: Bridge  2"), containsString("access possible: false"),
                        containsString("SHC Generation: Gen-U"), containsString("IP Address: 11.22.33.44")));
    }

    @Test
    void printDeviceInfo() throws InterruptedException {
        // no bridge
        when(thingRegistry.getAll()).thenReturn(Collections.emptyList());
        assertThat(fixture.buildDeviceInfo(), is(""));

        // One bridge, No device
        BridgeHandler mockBridgeHandler = mock(BridgeHandler.class);
        Thing mockBridgeThing = mock(Thing.class);
        when(mockBridgeThing.getLabel()).thenReturn("TestLabel");
        when(mockBridgeThing.getHandler()).thenReturn(mockBridgeHandler);
        when(thingRegistry.getAll()).thenReturn(Collections.singletonList(mockBridgeThing));
        assertThat(fixture.buildDeviceInfo(), allOf(containsString("thing: TestLabel"), containsString("devices (0)")));

        // One bridge, One UNsupported device
        Device mockShcDevice = mock(Device.class);
        mockShcDevice.deviceModel = "";
        mockShcDevice.deviceServiceIds = Collections.emptyList();
        when(mockBridgeHandler.getDevices()).thenReturn(List.of(mockShcDevice));
        assertThat(fixture.buildDeviceInfo(), allOf(containsString("thing: TestLabel"), containsString("devices (1)"),
                containsString("!UNSUPPORTED!")));

        // One bridge, One supported device
        mockShcDevice.deviceModel = "TWINGUARD";
        mockShcDevice.deviceServiceIds = Collections.emptyList();
        when(mockBridgeHandler.getDevices()).thenReturn(List.of(mockShcDevice));
        assertThat(fixture.buildDeviceInfo(), allOf(containsString("thing: TestLabel"), containsString("devices (1)"),
                containsString("TWINGUARD -> twinguard")));

        // One bridge, One supported device with services
        mockShcDevice.deviceModel = "TWINGUARD";
        mockShcDevice.deviceServiceIds = List.of("unknownService", "batterylevel");
        when(mockBridgeHandler.getDevices()).thenReturn(List.of(mockShcDevice));
        assertThat(fixture.buildDeviceInfo(), allOf(containsString("thing: TestLabel"), containsString("devices (1)"),
                containsString("TWINGUARD -> twinguard"), containsString("service: unknownService -> !UNSUPPORTED!"),
                containsString("batterylevel -> batterylevel")));
    }

    @Test
    void printBindingInfo() {
        assertThat(fixture.buildBindingInfo(), containsString("Bosch SHC Binding"));
    }

    @Test
    void printSupportedDevices() {
        assertThat(fixture.buildSupportedDeviceStatus(),
                allOf(containsString("Supported Devices"), containsString("BBL = boschshc:shutter-control")));
    }

    @Test
    void printSupportedServices() {
        assertThat(fixture.buildSupportedServiceStatus(),
                allOf(containsString("Supported Services"), containsString("airqualitylevel")));
    }

    /**
     * The list of services returned by getAllBoschShcServices() shall match
     * the implemented services in org.openhab.bindings.boschshc.internal.services.
     * Because reflection doesn't return all services classes during runtime
     * this test supports consistency between the lists of services and the implemented services.
     */
    @Test
    void getAllBoschShcServices() throws IOException {
        List<String> services = Files
                .walk(Paths.get("src/main/java/org/openhab/binding/boschshc/internal/services").toAbsolutePath(), 1)
                .filter(Files::isDirectory).map(Path::getFileName).map(Path::toString)
                // exclude folders which no service implementation
                .filter(name -> !name.equals("dto")).filter(name -> !name.equals("services")).sorted().toList();
        assertThat(services, is(fixture.getAllBoschShcServices()));
    }
}

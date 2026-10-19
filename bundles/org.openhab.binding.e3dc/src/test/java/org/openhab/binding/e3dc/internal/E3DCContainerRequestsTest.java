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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * Guards the requests of the containers that answer only to a request stating what to read.
 *
 * <p>
 * Such a container carries the data points to read as its parameters, and the index of the device where it
 * addresses one: a request without the index is answered with a format error, one with the index only with
 * the empty container. These tests keep the parameters in step with the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCContainerRequestsTest {

    @Test
    void theRequestCarriesTheIndexBeforeTheParameters() {
        E3DCContainerRequests.Container wallbox = container(E3DCBindingConstants.GROUP_WALLBOX);

        RscpValue request = wallbox.request(3, Set.of());

        assertEquals(RscpTag.WB_REQ_DATA, request.getTag());
        List<RscpValue> children = request.getChildren();
        assertEquals(wallbox.parameters().size() + 1, children.size());
        assertEquals(RscpTag.WB_INDEX, children.get(0).getTag(), "the device index has to come first");
        assertEquals(3, children.get(0).getValue());
        assertEquals(RscpTag.WB_REQ_ENERGY_ALL, children.get(1).getTag());
        assertEquals(RscpType.NONE, children.get(1).getType(), "a parameter is sent without a value");
    }

    /** A data point the device has reported as unsupported is left out of the request. */
    @Test
    void anUnsupportedParameterIsNoLongerRequested() {
        E3DCContainerRequests.Container inverter = container(E3DCBindingConstants.GROUP_INVERTER);

        RscpValue request = inverter.request(0, Set.of(RscpTag.PVI_REQ_AC_ENERGY_DAY));

        assertNull(request.find(RscpTag.PVI_REQ_AC_ENERGY_DAY));
        assertNotNull(request.find(RscpTag.PVI_REQ_AC_VOLTAGE), "the other data points stay in the request");
        assertEquals(RscpTag.PVI_INDEX, request.getChildren().get(0).getTag());
    }

    /** A container that addresses no device takes its parameters alone, without an index. */
    @Test
    void aContainerWithoutADeviceIsRequestedWithItsParametersOnly() {
        E3DCContainerRequests.Container reserve = container(E3DCBindingConstants.GROUP_RESERVE);

        RscpValue request = reserve.request(0, Set.of());

        assertEquals(RscpTag.SE_REQ_EP_RESERVE, request.getTag());
        assertEquals(reserve.parameters().size(), request.getChildren().size());
        assertEquals(RscpTag.SE_PARAM_EP_RESERVE, request.getChildren().get(0).getTag());
    }

    /**
     * Every wallbox channel has to be part of the wallbox request. The packed status struct is the exception:
     * it is not read through a request tag of its own but asked for as the extern data of all sources.
     */
    @Test
    void everyWallboxChannelIsRequested() {
        assertRequestsEveryChannel(E3DCBindingConstants.GROUP_WALLBOX,
                Map.of(RscpTag.WB_EXTERN_DATA, RscpTag.WB_REQ_EXTERN_DATA_ALL), Set.of());
    }

    @Test
    void everyMeterChannelIsRequested() {
        assertRequestsEveryChannel(E3DCBindingConstants.GROUP_METER, Map.of(), Set.of());
    }

    /**
     * Every inverter channel has to be part of the inverter request. The number of inverters is the
     * exception: the device answers that data point on its own, outside of the container.
     */
    @Test
    void everyInverterChannelIsRequested() {
        assertRequestsEveryChannel(E3DCBindingConstants.GROUP_INVERTER, Map.of(), Set.of(RscpTag.PVI_INVERTER_COUNT));
    }

    @Test
    void everyReserveChannelIsRequested() {
        assertRequestsEveryChannel(E3DCBindingConstants.GROUP_RESERVE, Map.of(), Set.of());
    }

    /**
     * The module values arrive in the answer of the battery request, which is why the request asks for the
     * module values instead of naming the module container itself.
     */
    @Test
    void theBatteryIsAskedForTheModuleAndBatteryValues() {
        E3DCContainerRequests.Container battery = container(E3DCBindingConstants.GROUP_MODULE);

        assertEquals(RscpTag.BAT_REQ_DATA, battery.containerTag());
        assertEquals(RscpTag.BAT_INDEX, battery.indexTag());
        assertEquals(RscpTag.BAT_DCB_INFO, battery.responseTag(), "the module channels read the module container");
        assertTrue(battery.parameters().contains(RscpTag.BAT_REQ_DCB_INFO));
        assertTrue(battery.parameters().contains(RscpTag.BAT_REQ_DCB_COUNT));
        assertTrue(battery.parameters().contains(RscpTag.BAT_REQ_RSOC),
                "the state of charge the battery reports is a parameter as well");
    }

    @Test
    void theBatteryAndItsModulesShareOneRequest() {
        E3DCContainerRequests.Container pack = container(E3DCBindingConstants.GROUP_PACK);
        E3DCContainerRequests.Container module = container(E3DCBindingConstants.GROUP_MODULE);

        assertEquals(RscpTag.BAT_DATA, pack.responseTag(), "the battery channels read the battery container");
        assertEquals(module.request(0, java.util.Set.of()).describeTree(),
                pack.request(0, java.util.Set.of()).describeTree(),
                "both ask the same, so the request is sent once for a battery index");
        assertTrue(pack.parameters().contains(RscpTag.requestFor(RscpTag.BAT_MODULE_VOLTAGE)));
    }

    @Test
    void everyContainerIsFoundByItsTags() {
        for (E3DCContainerRequests.Container container : E3DCContainerRequests.all()) {
            assertSame(container, E3DCContainerRequests.forGroup(container.groupId()));
            assertSame(container, E3DCContainerRequests.forResponseTag(container.responseTag()));
            // the battery and its modules are asked for with the same request
            E3DCContainerRequests.Container byTag = E3DCContainerRequests.forContainerTag(container.containerTag());
            assertEquals(container.containerTag(), byTag == null ? 0 : byTag.containerTag());
        }
        assertNull(E3DCContainerRequests.forResponseTag(RscpTag.EMS_GET_POWER_SETTINGS),
                "a container that answers everything it has is not part of this set");
        assertNull(E3DCContainerRequests.forGroup(E3DCBindingConstants.GROUP_POWER));
    }

    /**
     * A data point of one of these containers is read through the request of its container, so the poll of
     * the bridge asks for neither the container nor the data point on its own. Which tags it skips is decided
     * by exactly these two lookups.
     */
    @Test
    void noDataPointOfAContainerRequestIsPolledOnItsOwn() {
        assertNotNull(E3DCContainerRequests.forContainerTag(RscpTag.pollTagFor(RscpTag.WB_EXTERN_DATA)),
                "the wallbox status is read through the wallbox request");
        assertTrue(E3DCContainerRequests.isParameter(RscpTag.BAT_REQ_RSOC),
                "the battery state of charge is a parameter of the battery request");
        for (int responseTag : List.of(RscpTag.WB_DATA, RscpTag.PM_DATA, RscpTag.BAT_DCB_INFO, RscpTag.PVI_DATA,
                RscpTag.SE_EP_RESERVE)) {
            assertNotNull(E3DCContainerRequests.forResponseTag(responseTag),
                    RscpTag.nameOf(responseTag) + " is answered only to a request that names its parameters");
        }
        assertFalse(E3DCContainerRequests.isParameter(RscpTag.PVI_REQ_INVERTER_COUNT),
                "the number of inverters is not part of the inverter request");
    }

    /**
     * Every data point the channels of the group read has to be a parameter of the group's request, unless it
     * is one the device answers outside of the container.
     */
    private static void assertRequestsEveryChannel(String groupId, Map<Integer, Integer> exceptions,
            Set<Integer> answeredOutside) {
        E3DCContainerRequests.Container container = container(groupId);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, E3DCChannelSpec> channel : registry(groupId).entrySet()) {
            E3DCChannelSpec spec = channel.getValue();
            if (answeredOutside.contains(spec.tag())) {
                continue;
            }
            Integer exception = exceptions.get(spec.tag());
            int parameter;
            if (exception != null) {
                parameter = exception;
            } else if (spec.nestedTag() != 0) {
                // the value sits in a container of its own, which is asked for by its own parameter
                parameter = RscpTag.pollTagFor(spec.nestedTag());
            } else {
                parameter = RscpTag.pollTagFor(spec.tag());
            }
            if (!container.parameters().contains(parameter)) {
                missing.add(channel.getKey() + " -> " + RscpTag.nameOf(parameter));
            }
        }
        assertTrue(missing.isEmpty(), "these channels are not requested: " + missing);
    }

    private static Map<String, E3DCChannelSpec> registry(String groupId) {
        if (E3DCBindingConstants.GROUP_WALLBOX.equals(groupId)) {
            return E3DCChannelSpec.wallbox().channels(groupId);
        }
        if (E3DCBindingConstants.GROUP_METER.equals(groupId)) {
            return E3DCChannelSpec.powerMeter().channels(groupId);
        }
        if (E3DCBindingConstants.GROUP_MODULE.equals(groupId)) {
            return E3DCChannelSpec.batteryModule().channels(groupId);
        }
        return E3DCChannelSpec.powerPlant().channels(groupId);
    }

    private static E3DCContainerRequests.Container container(String groupId) {
        E3DCContainerRequests.Container container = E3DCContainerRequests.forGroup(groupId);
        assertNotNull(container, groupId + " has no container request");
        return Objects.requireNonNull(container);
    }
}

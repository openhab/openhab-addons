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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * The containers the device answers only to a request that states what to read.
 *
 * <p>
 * Such a container carries the data points to read as parameters of its own, and - where it addresses one
 * of several devices - the index of the addressed device as its first child. Asking for the container alone
 * is answered with a format error, asking for it with the index only with the empty container, so every
 * data point of these groups has to be requested this way.
 *
 * <p>
 * The parameters are the request variants of the tags the channels of the group read. {@code WB_EXTERN_DATA}
 * is the exception: its packed status struct is read through the extern data of all sources.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class E3DCContainerRequests {

    /**
     * One container that is requested per device, with the group whose channels it serves.
     *
     * @param groupId the channel group whose values live in this container
     * @param containerTag the tag the request is sent with
     * @param indexTag the tag the index of the addressed device is written with, or {@code 0} when this
     *            container addresses no device
     * @param responseTag the container the channels read their values from, which the request is answered
     *            with
     * @param parameters the data points to read, each one a parameter of the request
     */
    record Container(String groupId, int containerTag, int indexTag, int responseTag, List<Integer> parameters) {

        /**
         * The request for one device: the index first when the container addresses a device, then the data
         * points to read.
         *
         * @param index the device the container addresses
         * @param unsupported data points the device has already reported as unsupported, which are left out
         */
        RscpValue request(int index, Set<Integer> unsupported) {
            List<RscpValue> items = new ArrayList<>(parameters.size() + 1);
            if (indexTag != 0) {
                items.add(RscpValue.uchar8(indexTag, index));
            }
            for (int parameter : parameters) {
                if (!unsupported.contains(parameter)) {
                    items.add(RscpValue.none(parameter));
                }
            }
            return RscpValue.container(containerTag, items);
        }
    }

    private static final Container WALLBOX = new Container(E3DCBindingConstants.GROUP_WALLBOX, RscpTag.WB_REQ_DATA,
            RscpTag.WB_INDEX, RscpTag.WB_DATA,
            List.of(RscpTag.WB_REQ_ENERGY_ALL, RscpTag.WB_REQ_ENERGY_SOLAR, RscpTag.WB_REQ_SOC,
                    RscpTag.WB_REQ_PM_POWER_L1, RscpTag.WB_REQ_PM_POWER_L2, RscpTag.WB_REQ_PM_POWER_L3,
                    RscpTag.WB_REQ_PM_ACTIVE_PHASES, RscpTag.WB_REQ_PM_ENERGY_L1, RscpTag.WB_REQ_PM_ENERGY_L2,
                    RscpTag.WB_REQ_PM_ENERGY_L3, RscpTag.WB_REQ_NUMBER_PHASES, RscpTag.WB_REQ_MAX_CHARGE_CURRENT,
                    RscpTag.WB_REQ_MIN_CHARGE_CURRENT, RscpTag.WB_REQ_KEY_STATE, RscpTag.WB_REQ_SUN_MODE_ACTIVE,
                    RscpTag.WB_REQ_ABORT_CHARGING, RscpTag.WB_REQ_AUTO_PHASE_SWITCH_ENABLED,
                    RscpTag.WB_REQ_EXTERN_DATA_ALL));

    private static final Container METER = new Container(E3DCBindingConstants.GROUP_METER, RscpTag.PM_REQ_DATA,
            RscpTag.PM_INDEX, RscpTag.PM_DATA,
            List.of(RscpTag.PM_REQ_POWER_L1, RscpTag.PM_REQ_POWER_L2, RscpTag.PM_REQ_POWER_L3,
                    RscpTag.PM_REQ_VOLTAGE_L1, RscpTag.PM_REQ_VOLTAGE_L2, RscpTag.PM_REQ_VOLTAGE_L3,
                    RscpTag.PM_REQ_ENERGY_L1, RscpTag.PM_REQ_ENERGY_L2, RscpTag.PM_REQ_ENERGY_L3,
                    RscpTag.PM_REQ_ACTIVE_PHASES, RscpTag.PM_REQ_MAX_PHASE_POWER));

    /**
     * The battery of the cell modules, asked for with the index of the battery rather than of a module: the
     * answer holds the container of every module, which is what the module channels read.
     */
    /**
     * The request of a battery: the answer holds the values of the battery itself and the container of every
     * cell module. The battery and its modules share this request, so it is sent once when both are present.
     */
    private static final List<Integer> BATTERY_PARAMETERS = List.of(RscpTag.BAT_REQ_RSOC, RscpTag.BAT_REQ_DCB_INFO,
            RscpTag.BAT_REQ_DCB_COUNT, RscpTag.requestFor(RscpTag.BAT_MODULE_VOLTAGE),
            RscpTag.requestFor(RscpTag.BAT_CURRENT), RscpTag.requestFor(RscpTag.BAT_CHARGE_CYCLES),
            RscpTag.requestFor(RscpTag.BAT_STATUS_CODE), RscpTag.requestFor(RscpTag.BAT_ERROR_CODE),
            RscpTag.requestFor(RscpTag.BAT_DEVICE_NAME), RscpTag.requestFor(RscpTag.BAT_ASOC),
            RscpTag.requestFor(RscpTag.BAT_TRAINING_MODE), RscpTag.requestFor(RscpTag.BAT_USABLE_CAPACITY),
            RscpTag.requestFor(RscpTag.BAT_USABLE_REMAINING_CAPACITY), RscpTag.requestFor(RscpTag.BAT_DESIGN_CAPACITY));

    private static final Container BATTERY = new Container(E3DCBindingConstants.GROUP_PACK, RscpTag.BAT_REQ_DATA,
            RscpTag.BAT_INDEX, RscpTag.BAT_DATA, BATTERY_PARAMETERS);

    /**
     * The battery of the cell modules, asked for with the index of the battery rather than of a module: the
     * answer holds the container of every module, which is what the module channels read.
     */
    private static final Container BATTERY_MODULE = new Container(E3DCBindingConstants.GROUP_MODULE,
            RscpTag.BAT_REQ_DATA, RscpTag.BAT_INDEX, RscpTag.BAT_DCB_INFO, BATTERY_PARAMETERS);

    /** The inverter, whose tracker values the answer reports once per tracker. */
    private static final Container INVERTER = new Container(E3DCBindingConstants.GROUP_INVERTER, RscpTag.PVI_REQ_DATA,
            RscpTag.PVI_INDEX, RscpTag.PVI_DATA,
            List.of(RscpTag.PVI_REQ_AC_POWER, RscpTag.PVI_REQ_AC_VOLTAGE, RscpTag.PVI_REQ_AC_CURRENT,
                    RscpTag.PVI_REQ_AC_APPARENT_POWER, RscpTag.PVI_REQ_AC_REACTIVE_POWER, RscpTag.PVI_REQ_AC_FREQUENCY,
                    RscpTag.PVI_REQ_AC_ENERGY_ALL, RscpTag.PVI_REQ_AC_ENERGY_DAY, RscpTag.PVI_REQ_DC_POWER,
                    RscpTag.PVI_REQ_DC_VOLTAGE, RscpTag.PVI_REQ_DC_CURRENT, RscpTag.PVI_REQ_TEMPERATURE,
                    RscpTag.PVI_REQ_ON_GRID, RscpTag.PVI_REQ_USED_STRING_COUNT));

    /**
     * The reserve kept for emergency power, which addresses no device and therefore carries no index. The
     * device adds the parameters it reports by itself to the answer of a request that names them.
     */
    private static final Container RESERVE = new Container(E3DCBindingConstants.GROUP_RESERVE,
            RscpTag.SE_REQ_EP_RESERVE, 0, RscpTag.SE_EP_RESERVE, List.of(RscpTag.SE_PARAM_EP_RESERVE,
                    RscpTag.SE_PARAM_LAST_SOC, RscpTag.SE_PARAM_EP_RESERVE_W, RscpTag.SE_PARAM_EP_RESERVE_MAX_W));

    private static final List<Container> CONTAINERS = List.of(WALLBOX, METER, BATTERY, BATTERY_MODULE, INVERTER,
            RESERVE);

    /** The container of one channel group, or {@code null} when the group is requested as a bare container. */
    static @Nullable Container forGroup(String groupId) {
        for (Container container : CONTAINERS) {
            if (container.groupId().equals(groupId)) {
                return container;
            }
        }
        return null;
    }

    /** The container whose values arrive in the given response container, or {@code null}. */
    static @Nullable Container forResponseTag(int responseTag) {
        for (Container container : CONTAINERS) {
            if (container.responseTag() == responseTag) {
                return container;
            }
        }
        return null;
    }

    /** The container a request tag belongs to, or {@code null} when the tag addresses no such container. */
    static @Nullable Container forContainerTag(int containerTag) {
        for (Container container : CONTAINERS) {
            if (container.containerTag() == containerTag) {
                return container;
            }
        }
        return null;
    }

    /**
     * Whether the tag is a parameter of one of these containers, so the value it stands for arrives with the
     * request of that container and is not asked for on its own.
     */
    static boolean isParameter(int requestTag) {
        for (Container container : CONTAINERS) {
            if (container.parameters().contains(requestTag)) {
                return true;
            }
        }
        return false;
    }

    static List<Container> all() {
        return CONTAINERS;
    }

    private E3DCContainerRequests() {
    }
}

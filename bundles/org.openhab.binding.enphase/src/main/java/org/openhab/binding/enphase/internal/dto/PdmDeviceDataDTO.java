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
package org.openhab.binding.enphase.internal.dto;

/**
 * Data class for a single device entry of the Envoy {@code /ivp/pdm/device_data} response.
 *
 * @author Cedric Boon - Added support for detailed inverter stats
 */
public class PdmDeviceDataDTO {

    public static class WattHoursDTO {
        public int today;
        public int week;
    }

    public static class WattsDTO {
        public int now;
    }

    public static class LifetimeDTO {
        public long joulesProduced;
    }

    public static class ChannelDataDTO {
        public WattHoursDTO wattHours;
        public WattsDTO watts;
        public LifetimeDTO lifetime;
    }

    public String sn;
    public ChannelDataDTO[] channels;
}

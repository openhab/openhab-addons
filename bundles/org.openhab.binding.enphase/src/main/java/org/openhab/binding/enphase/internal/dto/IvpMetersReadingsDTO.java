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
 * Data class for a meter reading as returned by the ivp/meters/readings api call. The {@code eid} maps to a meter from
 * the ivp/meters call. {@code activePower} is the instantaneous power in Watt and {@code actEnergyDlvd} the cumulative
 * delivered energy in Watt hour.
 *
 * @author Andre Lackmann - Initial contribution
 */
public class IvpMetersReadingsDTO {
    public long eid;
    public long timestamp;
    public double actEnergyDlvd;
    public double actEnergyRcvd;
    public double activePower;
}

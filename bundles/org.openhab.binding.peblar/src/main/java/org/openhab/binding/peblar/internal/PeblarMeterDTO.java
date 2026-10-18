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
package org.openhab.binding.peblar.internal;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
class PeblarMeterDTO {

    public Long currentPhase1;
    public Long currentPhase2;
    public Long currentPhase3;

    public Integer voltagePhase1;
    public Integer voltagePhase2;
    public Integer voltagePhase3;

    public Long powerPhase1;
    public Long powerPhase2;
    public Long powerPhase3;

    public Long powerTotal;

    /** Lifetime energy in watt-hours */
    public Long energyTotal;

    /** Session energy in watt-hours */
    public Long energySession;
}

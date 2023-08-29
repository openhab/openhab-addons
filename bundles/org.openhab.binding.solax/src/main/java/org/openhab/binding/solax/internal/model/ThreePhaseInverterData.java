/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ThreePhaseInverterData} interface should be implemented for any particular bean that returns the parsed
 * data in a human readable code and format for a three-phased inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface ThreePhaseInverterData extends InverterData {

    double getVoltagePhase1();

    double getVoltagePhase2();

    double getVoltagePhase3();

    double getCurrentPhase1();

    double getCurrentPhase2();

    double getCurrentPhase3();

    short getOutputPowerPhase1();

    short getOutputPowerPhase2();

    short getOutputPowerPhase3();

    short getTotalOutputPower();

    double getFrequencyPhase1();

    double getFrequencyPhase2();

    double getFrequencyPhase3();
}

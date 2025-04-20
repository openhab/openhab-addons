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
package org.openhab.binding.enphase.internal.dto;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class PdmEnergyDTO {
    public class PdmProductionDTO {
        public EnvoyEnergyDTO pcu;
        public EnvoyEnergyDTO rgm;
        public EnvoyEnergyDTO eim;
    }

    public class PdmConsumptionDTO {
        public EnvoyEnergyDTO eim;
    }

    public PdmProductionDTO production;
    public PdmConsumptionDTO consumption;
}

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
package org.openhab.binding.solarwatt.internal.domain.dto;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DTO class for the complete structure delivered by the energy manager.
 *
 * Properties without setters are only filled by gson JSON parsing.
 *
 * @author Sven Carstens - Initial contribution
 */
public class EnergyManagerDTO {
    private Result result;

    public Collection<DeviceDTO> getItems() {
        return this.result.getItems();
    }

    public static class Result {
        public Collection<DeviceDTO> getItems() {
            return this.items;
        }

        private Collection<DeviceDTO> items = new ArrayList<>();
    }
}

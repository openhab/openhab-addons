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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author MikeTheTux - Initial contribution
 */
public class Capabilities {
    private boolean canConfirmError;
    private boolean headlights;
    private boolean position;
    private boolean stayOutZones;
    private boolean workAreas;

    public boolean canConfirmError() {
        return canConfirmError;
    }

    public boolean hasHeadlights() {
        return headlights;
    }

    public boolean hasPosition() {
        return position;
    }

    public boolean hasStayOutZones() {
        return stayOutZones;
    }

    public boolean hasWorkAreas() {
        return workAreas;
    }
}

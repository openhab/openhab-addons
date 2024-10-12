/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * @author Markus Pfleger - Initial contribution
 */
public class Capabilities {
    private Boolean canConfirmError;
    private Boolean headlights;
    private Boolean position;
    private Boolean stayOutZones;
    private Boolean workAreas;

    public Boolean canConfirmError() {
        return canConfirmError;
    }

    public Boolean hasHeadlights() {
        return headlights;
    }

    public Boolean hasPosition() {
        return position;
    }

    public Boolean hasStayOutZones() {
        return stayOutZones;
    }

    public Boolean hasWorkAreas() {
        return workAreas;
    }
}

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
package org.openhab.io.hueemulation.internal.dto;

/**
 * Hue API scan result object.
 * Enpoint: /api/{username}/lights/new
 *
 * @author David Graeff - Initial contribution
 */
public class HueNewLights {
    enum ScanType {
        none,
        active
    }

    /** Either none, active or a timestamp since the last search */
    public String lastscan = ScanType.none.name();
}

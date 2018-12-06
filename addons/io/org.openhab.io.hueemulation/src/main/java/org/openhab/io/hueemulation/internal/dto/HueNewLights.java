/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    };

    /** Either none, active or a timestamp since the last search */
    public String lastscan = ScanType.none.name();
}

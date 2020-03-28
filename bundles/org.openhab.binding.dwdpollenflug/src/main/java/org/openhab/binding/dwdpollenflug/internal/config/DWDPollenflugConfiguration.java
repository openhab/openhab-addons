/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DWDPollenflugConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugConfiguration {
    private int regionId;
    private int refresh;

    public int getRegionId() {
        return regionId;
    }

    public int getRefresh() {
        return refresh;
    }

    public boolean isRegion() {
        return regionId % 10 == 0;
    }

    public boolean isPartregion() {
        return !isRegion();
    }

    public boolean isValid() {
        return regionId > 0 && refresh >= 1;
    }
}

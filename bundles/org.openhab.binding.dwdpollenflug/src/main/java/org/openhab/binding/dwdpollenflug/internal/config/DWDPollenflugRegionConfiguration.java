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
package org.openhab.binding.dwdpollenflug.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the {@link org.openhab.binding.dwdpollenflug.internal.handler.DWDPollenflugRegionHandler}
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugRegionConfiguration {
    public int regionID;

    public boolean isValid() {
        return regionID > 0;
    }
}

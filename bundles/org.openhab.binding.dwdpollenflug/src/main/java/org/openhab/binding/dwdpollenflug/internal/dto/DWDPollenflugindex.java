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
package org.openhab.binding.dwdpollenflug.internal.dto;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class to hold Response of Http Request
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
public class DWDPollenflugindex {
    private @Nullable String sender;
    private @Nullable String name;
    private @Nullable String next_update;
    private @Nullable String last_update;

    private @Nullable Map<String, String> legend;

    private @Nullable Set<DWDRegion> content;

    public DWDRegion getRegion(int id) {
        if (content == null) {
            return null;
        }

        for (DWDRegion region : content) {
            if (region.getId() == id) {
                return region;
            }
        }

        return null;
    }

    public String getSender() {
        return sender;
    }

    public String getName() {
        return name;
    }

    public String getNext_update() {
        return next_update;
    }

    public String getLast_update() {
        return last_update;
    }

    public Map<String, String> getLegend() {
        return legend;
    }
}

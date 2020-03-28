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

/**
 * Class to hold Response of Http Request
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
public class DWDPollenflugindex {
    private String sender;
    private String name;
    private String next_update;
    private String last_update;

    private Map<String, String> legend;

    private Set<DWDRegion> content;

    public DWDRegion getRegion(int id) {
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

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
package org.openhab.binding.airquality.internal.json;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link AirQualityJsonCity} is responsible for storing
 * the "city" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityJsonCity {

    private String name;
    private String url;
    private List<Double> geo;

    public AirQualityJsonCity() {
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getGeo() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < geo.size(); i++) {
            list.add(geo.get(i).toString());
        }
        return String.join(",", list);
    }

}

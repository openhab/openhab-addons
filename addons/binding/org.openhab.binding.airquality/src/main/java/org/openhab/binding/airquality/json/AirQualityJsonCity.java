/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

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
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < geo.size(); i++) {
            list.add(geo.get(i).toString());
        }
        return String.join(",", list);
    }

}

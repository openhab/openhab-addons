/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

/**
 * The {@link AirQualityJsonResponse} is the Java class used to map the JSON
 * response to the aqicn.org request.
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityJsonResponse {

    private String status;

    private AirQualityJsonData data;

    public AirQualityJsonResponse() {
    }

    public String getStatus() {
        return status;
    }

    public AirQualityJsonData getData() {
        return data;
    }

}

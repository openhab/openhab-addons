/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.co2signal.internal.json;

/**
 * The {@link CO2SignalJsonResponse} is the Java class used to map the JSON
 * response to the co2signal.com request.
 *
 * @author Jens Viebig - Initial contribution
 */
public class CO2SignalJsonResponse {

    private String status;

    private String countryCode;

    private CO2SignalJsonData data;

    public CO2SignalJsonResponse() {
    }

    public String getStatus() {
        return status;
    }

    public CO2SignalJsonData getData() {
        return data;
    }

    public String getCountryCode() {
        return countryCode;
    }
}

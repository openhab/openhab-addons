/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.openuv.internal;

/**
 * The {@link ReportConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L"hopital - Initial contribution
 */
public class ReportConfiguration {
    String[] elements = null;

    private String location;
    public Integer refresh;

    public String getLatitude() {
        return getElement(0);
    }

    public String getLongitude() {
        return getElement(1);
    }

    public String getAltitude() {
        return getElement(2);
    }

    private String getElement(int index) {
        if (elements == null) {
            elements = location.split(",");
        }
        if (index < elements.length) {
            return elements[index].trim();
        } else {
            return null;
        }
    }
}

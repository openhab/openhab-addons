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

/**
 * Class to hold Response of Region
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
public class DWDPollen {
    private String today;
    private String tomorrow;
    private String dayafter_to;

    public String getToday() {
        return today;
    }

    public String getTomorrow() {
        return tomorrow;
    }

    public String getDayAfterTomorrow() {
        return dayafter_to;
    }
}

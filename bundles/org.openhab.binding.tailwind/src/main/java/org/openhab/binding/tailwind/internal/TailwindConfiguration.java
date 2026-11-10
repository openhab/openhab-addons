/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TailwindConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindConfiguration {

    /**
     * The number of garage doors connected to the controller
     */
    public int doorCount = 1; // 1=default (can be 1-3)

    /**
     * Holds either the IP address or HTTP URL of the TailWind controller web server (API)
     * Format for URL is tailwind-MAC address.local (i.e. tailwind-aa0b0cd0e0f0.local)
     */
    public String webServerAddress = ""; // Controller API IP Address or HTTP URL

    /**
     * Authorization token for the local API server on the controller
     */
    public String authToken = ""; // token from TailWind mobile application

    /**
     * OpenHab Host primary IP address
     */
    public String openHabHostAddress = ""; // primary IP address for OH server used for UDP connection listener

    /**
     * Door one partial open time in seconds
     */
    public float doorOnePartialOpen = 2.5f; // 2.5 is default (can be 0.5-15)

    /**
     * Door one name - allows user to provide a more meaningful name
     */
    public String doorOneName = "Door 1"; // Door 1 is default (must not be blank or duplicate)

    /**
     * Door one partial open time in seconds
     */
    public float doorTwoPartialOpen = 2.5f; // 2.5 is default (can be 0.5-15)

    /**
     * Door one name - allows user to provide a more meaningful name
     */
    public String doorTwoName = "Door 2"; // Door 1 is default (must not be blank or duplicate)

    /**
     * Door one partial open time in seconds
     */
    public float doorThreePartialOpen = 2.5f; // 2.5 is default (can be 0.5-15)

    /**
     * Door one name - allows user to provide a more meaningful name
     */
    public String doorThreeName = "Door 3"; // Door 1 is default (must not be blank or duplicate)

    /**
     * @return doorCount to be used to dynamically configure the door channels
     */
    public int getDoorCount() {
        return doorCount;
    }

    /**
     * @return the webServerAddress
     */
    public String getWebServerAddress() {
        return webServerAddress;
    }

    /**
     * @param webServerAddress the webServerAddress to set
     */
    public void setWebServerAddress(String webServerAddress) {
        this.webServerAddress = webServerAddress;
    }

    /**
     * @return authToken to be used with the requests to the API server on the controller
     */
    public String getAuthToken() {
        return authToken;
    }

    public String getOpenHabHostAddress() {
        return openHabHostAddress;
    }

    public void setOpenHabHostAddress(String openHabHostAddress) {
        this.openHabHostAddress = openHabHostAddress;
    }

    /**
     * @return the doorOnePartialOpen
     */
    public float getDoorOnePartialOpen() {
        return doorOnePartialOpen;
    }

    /**
     * @return the doorOneName
     */
    public String getDoorOneName() {
        return doorOneName;
    }

    /**
     * @return the doorTwoPartialOpen
     */
    public float getDoorTwoPartialOpen() {
        return doorTwoPartialOpen;
    }

    /**
     * @return the doorTwoName
     */
    public String getDoorTwoName() {
        return doorTwoName;
    }

    /**
     * @return the doorThreePartialOpen
     */
    public float getDoorThreePartialOpen() {
        return doorThreePartialOpen;
    }

    /**
     * @return the doorThreeName
     */
    public String getDoorThreeName() {
        return doorThreeName;
    }

    @Override
    public String toString() {
        return "TailwindConfiguration [doorCount=" + doorCount + ", authToken=" + authToken + "]";
    }
}

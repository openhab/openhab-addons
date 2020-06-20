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
package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Relay} class models the Relay response message
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Relay {

    public Integer relayId;

    public Integer relay;

    public String name;

    public String icon;

    public String lastwater;

    public Integer time;

    public Integer type;

    @SerializedName("run")
    public String runTime;

    @SerializedName("run_seconds")
    public Integer runTimeSeconds;

    public String nicetime;

    public String id;

    /**
     * Returns back the actual relay number when multiple controllers are chained.
     *
     * @return
     */
    public int getRelayNumber() {
        int quotient = relay / 100;
        return (relay - (quotient * 100)) + (quotient * 12);
    }
}

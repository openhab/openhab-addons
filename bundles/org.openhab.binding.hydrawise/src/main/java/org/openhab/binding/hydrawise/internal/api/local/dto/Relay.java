/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api.local.dto;

/**
 * The {@link Relay} class models the Relay response message
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Relay {

    public Integer relayId;

    public Integer time;

    public Integer type;

    public Integer relay;

    public String name;

    public Integer frequency;

    public String timestr;

    public Integer runSeconds;

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

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
package org.openhab.binding.hydrawise.internal.api.model;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Running {

    private String relay;

    private String relayId;

    private Integer timeLeft;

    private String run;

    /**
     * @return
     */
    public String getRelay() {
        return relay;
    }

    /**
     * @param relay
     */
    public void setRelay(String relay) {
        this.relay = relay;
    }

    /**
     * @return
     */
    public Integer getRelayId() {
        return new Integer(relayId);
    }

    /**
     * @param relayId
     */
    public void setRelayId(String relayId) {
        this.relayId = relayId;
    }

    /**
     * @return
     */
    public Integer getTimeLeft() {
        return timeLeft;
    }

    /**
     * @param timeLeft
     */
    public void setTimeLeft(Integer timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * @return
     */
    public String getRun() {
        return run;
    }

    /**
     * @param run
     */
    public void setRun(String run) {
        this.run = run;
    }

}
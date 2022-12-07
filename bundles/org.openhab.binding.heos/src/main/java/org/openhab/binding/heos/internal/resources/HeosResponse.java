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
package org.openhab.binding.heos.internal.resources;

/**
 * The {@link HeosResponse} returns the information
 * got from the HEOS JSON message
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosResponse {

    private HeosResponseEvent event;
    private HeosResponsePayload payload;
    private String pid;
    private String rawResponseMessage;

    public HeosResponse() {
        this.event = new HeosResponseEvent();
        this.payload = new HeosResponsePayload();
    }

    /**
     * Returns the HEOS Event which contains the information of
     * the received kind of event.
     *
     * @return the Heos Event Type
     */
    public HeosResponseEvent getEvent() {
        return event;
    }

    /**
     * Returns the Heos payload which is decode from the Heos response
     *
     * @return the Heos Payload
     */
    public HeosResponsePayload getPayload() {
        return payload;
    }

    public void setEvent(HeosResponseEvent event) {
        this.event = event;
    }

    public void setPayload(HeosResponsePayload payload) {
        this.payload = payload;
    }

    /**
     *
     * @return the Player ID from which the response was received
     */
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     *
     * @return the undecoded message received from HEOS
     */
    public String getRawResponseMessage() {
        return rawResponseMessage;
    }

    public void setRawResponseMessage(String rawResponseMessage) {
        this.rawResponseMessage = rawResponseMessage;
    }

}

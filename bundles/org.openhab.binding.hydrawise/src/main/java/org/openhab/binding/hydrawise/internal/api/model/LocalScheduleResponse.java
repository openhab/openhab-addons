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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LocalScheduleResponse extends Response {

    private List<Running> running = new LinkedList<Running>();

    private List<Relay> relays = new LinkedList<Relay>();

    private String name;

    private Integer time;

    /**
     * @return
     */
    public List<Running> getRunning() {
        return running;
    }

    /**
     * @param running
     */
    public void setRunning(List<Running> running) {
        this.running = running;
    }

    /**
     * @return
     */
    public List<Relay> getRelays() {
        return relays;
    }

    /**
     * @param relays
     */
    public void setRelays(List<Relay> relays) {
        this.relays = relays;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public Integer getTime() {
        return time;
    }

    /**
     * @param time
     */
    public void setTime(Integer time) {
        this.time = time;
    }
}

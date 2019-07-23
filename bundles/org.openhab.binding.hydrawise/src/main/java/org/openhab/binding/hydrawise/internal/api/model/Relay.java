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

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Relay {

    private Integer relayId;

    private Integer relay;

    private String name;

    private String icon;

    private String lastwater;

    private Integer time;

    private Integer type;

    @SerializedName("run")
    private String runTime;

    @SerializedName("run_seconds")
    private Integer runTimeSeconds;

    private String nicetime;

    private String id;

    /**
     * @return
     */
    public Integer getRelayId() {
        return relayId;
    }

    /**
     * @param relayId
     */
    public void setRelayId(Integer relayId) {
        this.relayId = relayId;
    }

    /**
     * @return
     */
    public Integer getRelay() {
        return relay;
    }

    /**
     * @param relay
     */
    public void setRelay(Integer relay) {
        this.relay = relay;
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
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return
     */
    public String getLastwater() {
        return lastwater;
    }

    /**
     * @param lastwater
     */
    public void setLastwater(String lastwater) {
        this.lastwater = lastwater;
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

    /**
     * @return
     */
    public Integer getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return
     */
    public String getRunTime() {
        return runTime;
    }

    /**
     * @param runTime
     */
    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    /**
     * @return
     */
    public Integer getRunTimeSeconds() {
        return runTimeSeconds;
    }

    /**
     * @param runTimeSeconds
     */
    public void setRunTimeSeconds(Integer runTimeSeconds) {
        this.runTimeSeconds = runTimeSeconds;
    }

    /**
     * @return
     */
    public String getNicetime() {
        return nicetime;
    }

    /**
     * @param nicetime
     */
    public void setNicetime(String nicetime) {
        this.nicetime = nicetime;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns back the actual relay number when multiple controllers are chained.
     *
     * @return
     */
    public int getRelayNumber() {
        int quotient = getRelay() / 100;
        return (getRelay() - (quotient * 100)) + (quotient * 12);
    }

}
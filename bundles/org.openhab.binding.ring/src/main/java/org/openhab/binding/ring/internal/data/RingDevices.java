/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openhab.binding.ring.internal.ApiConstants;
import org.openhab.binding.ring.internal.RingAccount;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

public class RingDevices {
    private List<Doorbell> doorbells;
    private List<Stickupcam> stickupcams;
    private List<Chime> chimes;
    private List<Other> others;

    public RingDevices(JSONObject jsonRingDevices, RingAccount ringAccount) {
        addDoorbells((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_DOORBOTS), ringAccount);
        addStickupCams((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_STICKUP_CAMS), ringAccount);
        addChimes((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_CHIMES), ringAccount);
        addOthers((JSONArray) jsonRingDevices.get(ApiConstants.DEVICES_OTHER), ringAccount);
    }

    /**
     * Helper method to create the doorbell list.
     *
     * @param jsonDoorbells
     */
    private final void addDoorbells(JSONArray jsonDoorbells, RingAccount ringAccount) {
        doorbells = new ArrayList<>();
        for (Object obj : jsonDoorbells) {
            Doorbell doorbell = new Doorbell((JSONObject) obj);
            doorbell.setRingAccount(ringAccount);
            doorbells.add(doorbell);
        }
    }

    /**
     * Retrieve the Doorbells Collection.
     *
     * @return
     */
    public Collection<Doorbell> getDoorbells() {
        return doorbells;
    }

    /**
     * Helper method to create the stickupcam list.
     *
     * @param jsonStickupcams
     */
    private final void addStickupCams(JSONArray jsonStickupcams, RingAccount ringAccount) {
        stickupcams = new ArrayList<>();
        for (Object obj : jsonStickupcams) {
            Stickupcam stickupcam = new Stickupcam((JSONObject) obj);
            stickupcam.setRingAccount(ringAccount);
            stickupcams.add(stickupcam);
        }
    }

    /**
     * Retrieve the Stickupcams Collection.
     *
     * @return
     */
    public Collection<Stickupcam> getStickupcams() {
        return stickupcams;
    }

    /**
     * Helper method to create the chime list.
     *
     * @param jsonChimes
     */
    private final void addChimes(JSONArray jsonChimes, RingAccount ringAccount) {
        chimes = new ArrayList<>();
        for (Object obj : jsonChimes) {
            Chime chime = new Chime((JSONObject) obj);
            chime.setRingAccount(ringAccount);
            chimes.add(chime);
        }
    }

    /**
     * Retrieve the Chimes Collection.
     *
     * @return
     */
    public Collection<Chime> getChimes() {
        return chimes;
    }

    /**
     * Helper method to create the other list.
     *
     * @param jsonOther
     */
    private final void addOthers(JSONArray jsonOthers, RingAccount ringAccount) {
        others = new ArrayList<>();
        for (Object obj : jsonOthers) {
            Other other = new Other((JSONObject) obj);
            other.setRingAccount(ringAccount);
            others.add(other);
        }
    }

    /**
     * Retrieve the Others Collection.
     *
     * @return
     */
    public Collection<Other> getOthers() {
        return others;
    }

    /**
     * Retrieve a collection of all devices.
     *
     * @return
     */
    public Collection<RingDevice> getRingDevices() {
        List<RingDevice> result = new ArrayList<>();
        result.addAll(doorbells);
        result.addAll(stickupcams);
        result.addAll(chimes);
        result.addAll(others);
        return result;
    }
}

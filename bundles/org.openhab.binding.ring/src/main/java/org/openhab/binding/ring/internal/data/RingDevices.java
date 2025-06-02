/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.ApiConstants;
import org.openhab.binding.ring.internal.RingAccount;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class RingDevices {
    private List<Doorbell> doorbells = new ArrayList<>();
    private List<Stickupcam> stickupcams = new ArrayList<>();
    private List<Chime> chimes = new ArrayList<>();
    private List<OtherDevice> otherdevices = new ArrayList<>();

    public RingDevices(JsonObject jsonRingDevices, RingAccount ringAccount) {
        addDoorbells((JsonArray) jsonRingDevices.get(ApiConstants.DEVICES_DOORBOTS), ringAccount);
        addStickupCams((JsonArray) jsonRingDevices.get(ApiConstants.DEVICES_STICKUP_CAMS), ringAccount);
        addChimes((JsonArray) jsonRingDevices.get(ApiConstants.DEVICES_CHIMES), ringAccount);
        addOtherDevices((JsonArray) jsonRingDevices.get(ApiConstants.DEVICES_OTHERDEVICE), ringAccount);
    }

    /**
     * Helper method to create the doorbell list.
     *
     * @param jsonDoorbells
     */
    private void addDoorbells(JsonArray jsonDoorbells, RingAccount ringAccount) {
        for (Object obj : jsonDoorbells) {
            Doorbell doorbell = new Doorbell((JsonObject) obj);
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
    private void addStickupCams(JsonArray jsonStickupcams, RingAccount ringAccount) {
        for (Object obj : jsonStickupcams) {
            Stickupcam stickupcam = new Stickupcam((JsonObject) obj);
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
    private void addChimes(JsonArray jsonChimes, RingAccount ringAccount) {
        for (Object obj : jsonChimes) {
            Chime chime = new Chime((JsonObject) obj);
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
    private void addOtherDevices(JsonArray jsonOtherDevices, RingAccount ringAccount) {
        for (Object obj : jsonOtherDevices) {
            OtherDevice otherdevice = new OtherDevice((JsonObject) obj);
            otherdevice.setRingAccount(ringAccount);
            otherdevices.add(otherdevice);
        }
    }

    /**
     * Retrieve the Others Collection.
     *
     * @return
     */
    public Collection<OtherDevice> getOtherDevices() {
        return otherdevices;
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
        result.addAll(otherdevices);
        return result;
    }
}

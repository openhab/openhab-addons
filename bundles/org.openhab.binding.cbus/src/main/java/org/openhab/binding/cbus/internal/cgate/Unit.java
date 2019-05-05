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
package org.openhab.binding.cbus.internal.cgate;

import java.util.ArrayList;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class Unit extends CGateObject implements Comparable<Unit> {
    private Network network;

    private int unitIndex;

    private int unitId;

    private boolean on_network;

    Unit(CGateSession cgate_session, Network network, int unitIndex, int unitId) {
        super(cgate_session);
        this.network = network;
        this.unitIndex = unitIndex;
        this.unitId = unitId;
        this.on_network = unitIndex != -1;
    }

    @Override
    protected String getKey() {
        return String.valueOf(unitId);
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        throw new IllegalArgumentException("There are no CGateObjects owned by a Unit");
    }

    @Override
    String getProjectAddress() {
        return "//" + getNetwork().getProjectName();
    }

    @Override
    String getResponseAddress(boolean id) {
        return getNetwork().getNetworkID() + "/" + (id ? ("p/" + getUnitID()) : ("Unit[" + unitIndex + "]"));
    }

    @Override
    public int compareTo(Unit o) {
        int cmp = network.compareTo(o.network);
        if (cmp != 0)
            return cmp;
        return (getUnitID() < o.getUnitID() ? -1 : (getUnitID() == o.getUnitID() ? 0 : 1));
    }

    static void createDBUnit(CGateSession cgate_session, Network network, String response) throws CGateException {
        String application_type = Network.getApplicationType(network, response);
        int unitId = getUnitID(network, response);

        if (application_type.equals("p")) {
            Unit unit = (Unit) network.getCachedObject("unit", String.valueOf(unitId));
            if (unit == null) {
                unit = new Unit(cgate_session, network, -1, unitId);
                network.cacheObject("unit", unit);
            }
        }
    }

    static Unit getOrCreateUnit(CGateSession cgate_session, Network network, int unitIndex, String response)
            throws CGateException {
        int index = response.indexOf("=");
        String unitId = response.substring(index + 1);

        Unit unit = (Unit) network.getCachedObject("unit", unitId);
        if (unit == null) {
            unit = new Unit(cgate_session, network, unitIndex, Integer.parseInt(unitId));
            network.cacheObject("unit", unit);
        }
        return unit;
    }

    static int getUnitID(Network network, String response) {
        String application_type = Network.getApplicationType(network, response);
        String application_address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/"
                + application_type + "/";
        int index = response.indexOf(application_address);
        int unit_index = response.indexOf(" ", index + 1);
        return Integer.parseInt(response.substring(index + application_address.length(), unit_index).trim());
    }

    /**
     *
     * @return
     */
    public int getUnitID() {
        return unitId;
    }

    /**
     *
     * @return
     */
    public Network getNetwork() {
        return network;
    }

    public String getExistsOnNetwork() {
        return onNetwork() ? "Yes" : "No";
    }

    public boolean onNetwork() {
        return on_network;
    }

    public String getName() throws CGateException {
        String address = getResponseAddress(true) + "/TagName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getUnitType() throws CGateException {
        String address = getResponseAddress(true) + "/UnitType";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getSerialNumber() throws CGateException {
        String address = getResponseAddress(true) + "/SerialNumber";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getUnitName() throws CGateException {
        String address = getResponseAddress(true) + "/UnitName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getFirmware() throws CGateException {
        String address = getResponseAddress(true) + "/FirmwareVersion";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }
}

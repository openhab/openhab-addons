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
public class Group extends CGateObject implements Comparable<Group> {
    private Application application;

    private int groupIndex;

    private int groupId;

    private boolean on_network;

    Group(CGateSession cgate_session, Application application, int groupIndex, int groupId) {
        super(cgate_session);
        this.application = application;
        this.groupIndex = groupIndex;
        this.groupId = groupId;
        this.on_network = groupIndex != -1;
    }

    @Override
    protected String getKey() {
        return String.valueOf(groupId);
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        throw new IllegalArgumentException("There are no CGateObjects owned by a Group");
    }

    @Override
    String getProjectAddress() {
        return "//" + getNetwork().getProjectName();
    }

    @Override
    String getResponseAddress(boolean id) {
        return getNetwork().getNetworkID() + "/" + application.getApplicationID() + "/"
                + (id ? getGroupID() : ("Group[" + groupIndex + "]"));
    }

    @Override
    public int compareTo(Group o) {
        int cmp = application.compareTo(o.application);
        if (cmp != 0)
            return cmp;
        return (getGroupID() < o.getGroupID() ? -1 : (getGroupID() == o.getGroupID() ? 0 : 1));
    }

    static void createDBGroup(CGateSession cgate_session, Network network, String response) throws CGateException {
        String application_type = Network.getApplicationType(network, response);
        int groupId = getGroupID(network, response);

        if (!application_type.equals("p")) {
            Application application = network.getApplication(Integer.parseInt(application_type));
            if (application != null) {
                Group group = (Group) application.getCachedObject("group", String.valueOf(groupId));
                if (group == null) {
                    group = new Group(cgate_session, application, -1, groupId);
                    application.cacheObject("group", group);
                }
            }
        }
    }

    static Group getOrCreateGroup(CGateSession cgate_session, Application application, int groupIndex, String response)
            throws CGateException {
        int index = response.indexOf("=");
        String groupId = response.substring(index + 1);

        if (groupId.equals("255"))
            return null;

        Group group = (Group) application.getCachedObject("group", groupId);
        if (group == null) {
            group = new Group(cgate_session, application, groupIndex, Integer.parseInt(groupId));
            application.cacheObject("group", group);
        }
        return group;
    }

    static int getGroupID(Network network, String response) {
        String application_type = Network.getApplicationType(network, response);
        String application_address = network.getResponseAddress(true) + "/" + application_type + "/";
        int index = response.indexOf(application_address);
        int unit_index = response.indexOf(" ", index + 1);
        return Integer.parseInt(response.substring(index + application_address.length(), unit_index).trim());
    }

    /**
     *
     * @return
     */
    public int getGroupID() {
        return groupId;
    }

    /**
     *
     * @return
     */
    public Application getApplication() {
        return application;
    }

    /**
     *
     * @return
     */
    public Network getNetwork() {
        return application.getNetwork();
    }

    public String getName() throws CGateException {
        if (!on_network)
            return "";
        String address = getResponseAddress(false) + "/TagName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    /**
     * Issue a <code>on //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.79</i></a>
     * @throws CGateException
     */
    public Response on() throws CGateException {
        return getCGateSession().sendCommand("on " + getProjectAddress() + "/" + getResponseAddress(true));
    }

    /**
     * Issue a <code>off //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.77</i></a>
     * @throws CGateException
     */
    public Response off() throws CGateException {
        return getCGateSession().sendCommand("off " + getProjectAddress() + "/" + getResponseAddress(true));
    }

    /**
     * Issue a <code>ramp //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.100</i></a>
     * @param level
     * @param seconds
     * @throws CGateException
     */
    public Response ramp(int level, int seconds) throws CGateException {
        return getCGateSession().sendCommand(
                "ramp " + getProjectAddress() + "/" + getResponseAddress(true) + " " + level + " " + seconds + "s");
    }

    /**
     * Issue a <code>terminate_ramp //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.123</i></a>
     * @param force
     * @throws CGateException
     */
    public Response terminateRamp(boolean force) throws CGateException {
        return getCGateSession().sendCommand(
                "terminate_ramp " + getProjectAddress() + "/" + getResponseAddress(true) + (force ? " force" : ""));
    }

    /**
     * Issue a <code>get //PROJECT/NET_ID/GROUP_ID Level</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.44</i></a>
     * @throws CGateException
     */
    public int getLevel() throws CGateException {
        ArrayList<String> resp_array = getCGateSession()
                .sendCommand("get " + getProjectAddress() + "/" + getResponseAddress(true) + " Level").toArray();
        String level_str = responseToMap(resp_array.get(0)).get("Level");
        return level_str == null ? 0 : Integer.valueOf(level_str);
    }
}

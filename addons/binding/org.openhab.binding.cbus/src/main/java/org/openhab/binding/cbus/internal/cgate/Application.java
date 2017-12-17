/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openhab.binding.cbus.internal.cgate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class Application extends CGateObject implements Comparable<Application> {
    private Network network;

    private int applicationIndex;

    private int applicationId;

    private Application(CGateSession cgate_session, Network network, int applicationIndex, int applicationId) {
        super(cgate_session);
        this.network = network;
        this.applicationIndex = applicationIndex;
        this.applicationId = applicationId;
        setupSubtreeCache("group");
    }

    @Override
    protected String getKey() {
        return String.valueOf(applicationId);
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        if (address.startsWith("//"))
            throw new IllegalArgumentException("Address must be a relative address. i.e. Not starting with //");

        boolean return_next = false;
        int next_part_index = address.indexOf("/");
        if (next_part_index == -1) {
            next_part_index = address.length();
            return_next = true;
        }

        int group_id = Integer.parseInt(address.substring(0, next_part_index));
        Group group = getGroup(group_id);
        if (group == null)
            throw new IllegalArgumentException("No group found: " + address);

        if (return_next)
            return group;

        return group.getCGateObject(address.substring(next_part_index + 1));
    }

    @Override
    String getProjectAddress() {
        return "//" + getNetwork().getProjectName();
    }

    @Override
    String getResponseAddress(boolean id) {
        return getNetwork().getNetworkID() + "/"
                + (id ? getApplicationID() : ("Application[" + applicationIndex + "]"));
    }

    @Override
    public int compareTo(Application o) {
        int cmp = network.compareTo(o.network);
        if (cmp != 0)
            return cmp;
        return (getApplicationID() < o.getApplicationID() ? -1 : (getApplicationID() == o.getApplicationID() ? 0 : 1));
    }

    static Application getOrCreateApplication(CGateSession cgate_session, Network network, int applicationIndex,
            String response) {
        int index = response.indexOf("=");
        String applicationId = response.substring(index + 1);

        if (applicationId.equals("255"))
            return null;

        Application application = (Application) network.getCachedObject("application", applicationId);
        if (application == null) {
            application = new Application(cgate_session, network, applicationIndex, Integer.parseInt(applicationId));
            network.cacheObject("application", application);
        }
        return application;
    }

    public Network getNetwork() {
        return network;
    }

    public int getApplicationID() {
        return applicationId;
    }

    public String getHexID() {
        return Integer.toHexString(applicationId);
    }

    public String getName() throws CGateException {
        String address = getResponseAddress(true) + "/TagName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getDescription() throws CGateException {
        String address = getResponseAddress(true) + "/Description";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    Response dbget(String param_name) throws CGateException {
        return getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + getResponseAddress(true)
                + (param_name == null ? "" : ("/" + param_name)));
    }

    public ArrayList<Group> getGroups(boolean cached_objects) throws CGateException {
        if (!cached_objects)
            clearCache("group");

        ArrayList<Group> groups = new ArrayList<Group>();

        Collection<CGateObject> cachedGroups = getAllCachedObjects("group");
        if (cachedGroups != null && !cachedGroups.isEmpty()) {
            for (CGateObject group : cachedGroups)
                groups.add((Group) group);
            Collections.sort(groups, new Comparator<Group>() {

                @Override
                public int compare(Group o1, Group o2) {
                    return (o1.getGroupID() < o2.getGroupID() ? -1 : (o1.getGroupID() == o2.getGroupID() ? 0 : 1));
                }
            });
            return groups;
        }

        Response resp = dbget(null);

        int number_of_groups = -1;
        for (String response : resp) {
            String address = getResponseAddress(true) + "/Group[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_groups = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        for (int i = 1; i <= number_of_groups; i++) {
            ArrayList<String> resp_array = dbget("Group[" + i + "]/Address").toArray();
            Group group = Group.getOrCreateGroup(getCGateSession(), this, i, resp_array.get(0));
            if (group != null)
                groups.add(group);
        }

        network.tree();

        return groups;
    }

    /**
     * Retrieve the Group Object for the specified group id.
     *
     * @param group_id The group to retrieve
     * @return The Group
     * @throws CGateException
     */
    public Group getGroup(int group_id) throws CGateException {
        Group group = (Group) getCachedObject("group", String.valueOf(group_id));
        if (group != null)
            return group;

        getGroups(false);

        return (Group) getCachedObject("group", String.valueOf(group_id));
    }
}

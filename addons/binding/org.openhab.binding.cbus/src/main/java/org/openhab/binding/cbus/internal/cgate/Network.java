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
import java.util.HashMap;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public final class Network extends CGateObject implements Comparable<Network> {
    private Project project;

    private int net_id;

    private Network(CGateSession cgate_session, Project project, String cgate_response) throws CGateException {
        super(cgate_session);
        this.project = project;
        this.net_id = getNetworkID(project, cgate_response);
        setupSubtreeCache("application");
        setupSubtreeCache("unit");
    }

    @Override
    protected String getKey() {
        return String.valueOf(net_id);
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        if (address.startsWith("//")) {
            throw new IllegalArgumentException("Address must be a relative address. i.e. Not starting with //");
        }

        boolean return_next = false;
        int next_part_index = address.indexOf("/");
        if (next_part_index == -1) {
            next_part_index = address.length();
            return_next = true;
        }

        String next_part = address.substring(0, next_part_index);
        if (next_part.equals("p")) {
            if (return_next) {
                throw new IllegalArgumentException("The address must not end with p");
            }

            int unit_part_index = address.substring(next_part_index + 1).indexOf("/");
            if (unit_part_index == -1) {
                unit_part_index = address.length();
                return_next = true;
            }

            next_part = address.substring(next_part_index + 1, unit_part_index);
            int unit_id = Integer.parseInt(next_part);
            Unit unit = getUnit(unit_id);
            if (unit == null) {
                throw new IllegalArgumentException("No unit found: " + address);
            }

            if (return_next) {
                return unit;
            }

            return unit.getCGateObject(address.substring(next_part_index + 1));
        } else {
            int application_id = Integer.parseInt(next_part);
            Application application = getApplication(application_id);
            if (application == null) {
                throw new IllegalArgumentException("No application found: " + address);
            }

            if (return_next) {
                return application;
            }

            return application.getCGateObject(address.substring(next_part_index + 1));
        }
    }

    @Override
    String getProjectAddress() {
        return "//" + getProjectName();
    }

    @Override
    String getResponseAddress(boolean id) {
        return String.valueOf(getNetworkID());
    }

    @Override
    public int compareTo(Network o) {
        int cmp = project.compareTo(o.project);
        if (cmp != 0) {
            return cmp;
        }
        return (getNetworkID() < o.getNetworkID() ? -1 : (getNetworkID() == o.getNetworkID() ? 0 : 1));
    }

    /**
     * Issue a <code>net list_all</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.63</i></a>
     * @param cgate_session The C-Gate session
     * @param cached_objects Return cached Project objects or rebuild list from C-Gate
     * @return ArrayList of Networks
     * @throws CGateException
     */
    public static ArrayList<Network> listAll(CGateSession cgate_session, boolean cached_objects) throws CGateException {
        Response resp = cgate_session.sendCommand("net list_all");

        if (!cached_objects) {
            for (Project project : Project.dir(cgate_session, true)) {
                project.clearCache("network");
            }
        }

        ArrayList<Network> networks = new ArrayList<Network>();
        for (String response : resp) {
            networks.add(getOrCreateNetwork(cgate_session, response));
        }

        return networks;
    }

    /**
     * Retrieve the Unit Object for the specified unit id.
     *
     * @param unit_id The unit to retrieve
     * @return The Unit
     * @throws CGateException
     */
    public Application getApplication(int application_id) throws CGateException {
        Application application = (Application) getCachedObject("application", String.valueOf(application_id));
        if (application != null) {
            return application;
        }

        getApplications(false);

        return (Application) getCachedObject("application", String.valueOf(application_id));
    }

    /**
     * Retrieve the Unit Object for the specified unit id.
     *
     * @param unit_id The unit to retrieve
     * @return The Unit
     * @throws CGateException
     */
    public Unit getUnit(int unit_id) throws CGateException {
        Unit unit = (Unit) getCachedObject("unit", String.valueOf(unit_id));
        if (unit != null) {
            return unit;
        }

        getUnits(false);

        return (Unit) getCachedObject("unit", String.valueOf(unit_id));
    }

    private static Network getOrCreateNetwork(CGateSession cgate_session, String cgate_response) throws CGateException {
        HashMap<String, String> resp_map = responseToMap(cgate_response);

        Project.dir(cgate_session, true);
        Project project = Project.getProject(cgate_session, resp_map.get("project"));

        int net_id = getNetworkID(project, cgate_response);

        Network network = (Network) project.getCachedObject("network", String.valueOf(net_id));
        if (network == null) {
            network = new Network(cgate_session, project, cgate_response);
            project.cacheObject("network", network);
        }
        return network;
    }

    static int getNetworkID(Project project, String cgate_response) throws CGateException {
        HashMap<String, String> resp_map = responseToMap(cgate_response);
        int net_id = -1;
        String value = resp_map.get("network");
        if (value != null) {
            net_id = Integer.parseInt(value.trim());
        } else {
            value = resp_map.get("address");
            if (value != null) {
                String net_str = value.substring(project.getName().length() + 3);
                net_id = Integer.parseInt(net_str.trim());
            }
        }

        if (net_id < 0) {
            throw new CGateException();
        }

        return net_id;
    }

    /**
     *
     * @return
     */
    public int getNetworkID() {
        return net_id;
    }

    public Project getProject() {
        return project;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getName() throws CGateException {
        String address = getResponseAddress(true) + "/TagName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + address)
                .toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getType() throws CGateException {
        ArrayList<String> resp_array = getCGateSession()
                .sendCommand("show " + getProjectAddress() + "/" + getResponseAddress(true) + " Type").toArray();
        return responseToMap(resp_array.get(0)).get("Type");
    }

    public String getInterfaceAddress() throws CGateException {
        ArrayList<String> resp_array = getCGateSession()
                .sendCommand("show " + getProjectAddress() + "/" + getResponseAddress(true) + " InterfaceAddress")
                .toArray();
        return responseToMap(resp_array.get(0)).get("InterfaceAddress");
    }

    public String getState() throws CGateException {
        ArrayList<String> resp_array = getCGateSession()
                .sendCommand("show " + getProjectAddress() + "/" + getResponseAddress(true) + " State").toArray();
        return responseToMap(resp_array.get(0)).get("State");
    }

    static String getApplicationType(Network network, String response) {
        String network_address = network.getResponseAddress(true) + "/";
        int index = response.indexOf(network_address);
        int application_index = response.indexOf("/", index + network_address.length());
        return response.substring(index + network_address.length(), application_index);
    }

    public ArrayList<Unit> getUnits(boolean cached_objects) throws CGateException {
        if (!cached_objects) {
            clearCache("unit");
        }

        Response resp = dbget(null);

        int number_of_units = -1;
        for (String response : resp) {
            String address = "" + getResponseAddress(true) + "/Unit[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_units = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Unit> units = new ArrayList<Unit>();
        for (int i = 1; i <= number_of_units; i++) {
            ArrayList<String> resp_array = dbget("Unit[" + i + "]/Address").toArray();
            Unit unit = Unit.getOrCreateUnit(getCGateSession(), this, i, resp_array.get(0));
            if (unit != null) {
                units.add(unit);
            }
        }

        tree();

        return units;
    }

    /**
     * Issue a <code>tree //PROJECT/NET_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.124</i></a>
     * @throws CGateException
     */
    void tree() throws CGateException {
        CGateSession cgate_session = getCGateSession();
        getApplications(true);
        Response resp = cgate_session.sendCommand("tree " + getAddress());

        for (String response : resp) {
            if (response.indexOf("" + getAddress() + "/") > -1) {
                if (getApplicationType(this, response).equals("p")) {
                    Unit.createDBUnit(cgate_session, this, response);
                } else {
                    Group.createDBGroup(cgate_session, this, response);
                }
            }
        }
    }

    /**
     * Issue a <code>net open //PROJECT/NET_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.65</i></a>
     * @throws CGateException
     */
    public void open() throws CGateException {
        getCGateSession().sendCommand("net open " + getProjectAddress() + "/" + getResponseAddress(true)).handle200();
    }

    Response dbget(String param_name) throws CGateException {
        return getCGateSession().sendCommand("dbget " + getProjectAddress() + "/" + getResponseAddress(true)
                + (param_name == null ? "" : ("/" + param_name)));
    }

    /**
     * Get all Application objects for this Network.
     *
     * @return ArrayList of Applications
     * @param cached_objects Return cached Project objects or rebuild list from C-Gate
     * @throws CGateException
     */
    public ArrayList<Application> getApplications(boolean cached_objects) throws CGateException {
        CGateSession cgate_session = getCGateSession();
        Response resp = dbget(null);

        if (!cached_objects) {
            clearCache("application");
        }

        int number_of_applications = -1;
        for (String response : resp) {
            String address = getResponseAddress(true) + "/Application[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_applications = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Application> applications = new ArrayList<Application>();
        for (int i = 1; i <= number_of_applications; i++) {
            ArrayList<String> resp_array = dbget("Application[" + i + "]/Address").toArray();
            Application application = Application.getOrCreateApplication(cgate_session, this, i, resp_array.get(0));
            if (application != null) {
                applications.add(application);
            }
        }

        return applications;
    }

    public void startSync() throws CGateException {
        getCGateSession().sendCommand("getstate " + getAddress());
    }

    public boolean isOnline() throws CGateException {
        return "ok".equals(getState());
    }
}

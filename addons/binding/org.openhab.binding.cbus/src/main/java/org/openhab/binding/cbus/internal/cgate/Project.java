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
public final class Project extends CGateObject implements Comparable<Project> {
    private String project_name;

    private Project(CGateSession cgate_session) {
        super(cgate_session);
        setupSubtreeCache("network");
    }

    private Project(CGateSession cgate_session, String cgate_response) {
        this(cgate_session);
        HashMap<String, String> resp_map = responseToMap(cgate_response);
        this.project_name = resp_map.get("project");
    }

    @Override
    protected String getKey() {
        return project_name;
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

        int network_id = Integer.parseInt(address.substring(0, next_part_index));
        Network network = getNetwork(network_id);
        if (network == null)
            throw new IllegalArgumentException("No network found: " + address);

        if (return_next)
            return network;

        return network.getCGateObject(address.substring(next_part_index + 1));
    }

    @Override
    String getProjectAddress() {
        return "//" + getName();
    }

    @Override
    String getResponseAddress(boolean id) {
        return "";
    }

    @Override
    public int compareTo(Project o) {
        return project_name.compareTo(project_name);
    }

    /**
     * Issue a <code>project dir</code> and <code>project list</code> to the
     * C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.89</i></a>
     * @param cgate_session The C-Gate session
     * @param cached_objects Return cached Project objects or rebuild list from C-Gate
     * @return ArrayList of Projects
     * @throws CGateException
     */
    public static ArrayList<Project> dir(CGateSession cgate_session, boolean cached_objects) throws CGateException {
        Response resp = cgate_session.sendCommand("project dir");

        if (!cached_objects)
            cgate_session.clearCache("project");

        ArrayList<Project> projects = new ArrayList<Project>();
        for (String response : resp)
            projects.add(getOrCreateProject(cgate_session, response));

        return projects;
    }

    /**
     * Issue a <code>project dir</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.90</i></a>
     * @param cgate_session The C-Gate session
     * @param cached_objects Return cached Project objects or rebuild list from C-Gate
     * @return ArrayList of Projects
     * @throws CGateException
     */
    public static ArrayList<Project> list(CGateSession cgate_session, boolean cached_objects) throws CGateException {
        Response resp = cgate_session.sendCommand("project list");

        if (!cached_objects)
            cgate_session.clearCache("project");

        ArrayList<Project> projects = new ArrayList<Project>();
        for (String response : resp)
            projects.add(getOrCreateProject(cgate_session, response));

        return projects;
    }

    /**
     * Issue a <code>project new <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.92</i></a>
     * @param cgate_session The C-Gate session
     * @param project_name The project name of the new copy
     * @return Project The new Project
     * @throws CGateException
     */
    public static Project newProject(CGateSession cgate_session, String project_name) throws CGateException {
        cgate_session.sendCommand("project new " + project_name).handle200();

        Project new_project = new Project(cgate_session);
        new_project.project_name = project_name;
        cgate_session.cacheObject("project", new_project);
        return new_project;
    }

    /**
     * Retrieve the Project Object for the specified project name.
     *
     * @param cgate_session The CGateSession
     * @param project_name The project name to retrieve
     * @return The Project
     * @throws CGateException
     */
    public static Project getProject(CGateSession cgate_session, String project_name) throws CGateException {
        Project project = (Project) cgate_session.getCachedObject("project", project_name);
        if (project != null)
            return project;

        dir(cgate_session, false);

        return (Project) cgate_session.getCachedObject("project", project_name);
    }

    /**
     * Retrieve the Network Object for the specified network id.
     *
     * @param network_id The network to retrieve
     * @return The Network
     * @throws CGateException
     */
    public Network getNetwork(int network_id) throws CGateException {
        Network network = (Network) getCachedObject("network", String.valueOf(network_id));
        if (network != null)
            return network;

        load();
        Network.listAll(getCGateSession(), true);

        return (Network) getCachedObject("network", String.valueOf(network_id));
    }

    /**
     * Get all Network objects for this Project.
     *
     * @param cached_objects Return cached Project objects or rebuild list from C-Gate
     * @return ArrayList of Networks
     * @throws CGateException
     */
    public ArrayList<Network> getNetworks(boolean cached_objects) throws CGateException {
        Network.listAll(getCGateSession(), cached_objects);

        ArrayList<Network> networks = new ArrayList<Network>();
        for (CGateObject network : getAllCachedObjects("network"))
            networks.add((Network) network);

        return networks;
    }

    static Project getOrCreateProject(CGateSession cgate_session, String cgate_response) throws CGateException {
        String project_name = null;

        HashMap<String, String> resp_map = responseToMap(cgate_response);
        project_name = resp_map.get("project");

        if (project_name == null)
            throw new CGateException();

        Project project = (Project) cgate_session.getCachedObject("project", project_name);
        if (project == null) {
            project = new Project(cgate_session, cgate_response);
            cgate_session.cacheObject("project", project);
        }
        return project;
    }

    /**
     * Get the project name.
     *
     * @return The project name
     */
    public String getName() {
        return project_name;
    }

    /**
     * Issue a <code>project close <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.86</i></a>
     * @throws CGateException
     */
    public void close() throws CGateException {
        getCGateSession().sendCommand("project close " + project_name).handle200();
    }

    /**
     * Issue a <code>project copy <i>source project_name</i> <i>copy project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.87</i></a>
     * @param project_name The project name of the new copy
     * @return Project The new copy of the current Project
     * @throws CGateException
     */
    public Project copy(String target_project_name) throws CGateException {
        CGateSession cgate_session = getCGateSession();
        cgate_session.sendCommand("project copy " + project_name + " " + target_project_name).handle200();

        Project new_project = new Project(cgate_session);
        new_project.project_name = target_project_name;
        cgate_session.cacheObject("project", new_project);
        return new_project;
    }

    /**
     * Issue a <code>project delete <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.88</i></a>
     * @throws CGateException
     */
    public void delete() throws CGateException {
        CGateSession cgate_session = getCGateSession();
        cgate_session.sendCommand("project delete " + project_name).handle200();

        cgate_session.uncacheObject("project", this);
    }

    /**
     * Issue a <code>project load <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.91</i></a>
     * @throws CGateException
     */
    public void load() throws CGateException {
        getCGateSession().sendCommand("project load " + project_name).handle200();
    }

    /**
     * Issue a <code>project start <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.96</i></a>
     * @throws CGateException
     */
    public void start() throws CGateException {
        getCGateSession().sendCommand("project start " + project_name).handle200();
    }

    /**
     * Issue a <code>project save <i>project_name</i></code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.95</i></a>
     * @throws CGateException
     */
    public void save() throws CGateException {
        getCGateSession().sendCommand("project save " + project_name).handle200();
    }
}

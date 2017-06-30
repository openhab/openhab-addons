/*
 * Copyright 2017 Steffen Folman Sørensen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.snapcast.internal.types;

import java.util.ArrayList;

/**
 * Generic POJO for Unmashalling Json Response from Snapcast
 */
public class ServerStatus {
    class InternalServer {
        private Server server;
        private ArrayList<Group> groups;
        private ArrayList<Stream> streams;

        public Server getServer() {
            return server;
        }

        public ArrayList<Group> getGroups() {
            return groups;
        }

        public ArrayList<Stream> getStreams() {
            return streams;
        }

    }

    private InternalServer server;

    public Server getServer() {
        return server.getServer();
    }

    public ArrayList<Group> getGroups() {
        return server.getGroups();
    }

    public ArrayList<Stream> getStreams() {
        return server.getStreams();
    }

    public void setServer(InternalServer server) {
        this.server = server;
    }

    /*
     * private int id;
     * private ArrayList<Group> groups;
     * private Server server;
     * private ArrayList<Stream> streams;
     *
     * public void setStreams(ArrayList<Stream> streams) {
     * this.streams = streams;
     * }
     *
     * public void setServer(Server server) {
     * this.server = server;
     * }
     *
     * public ArrayList<Group> getGroups() {
     * return groups;
     * }
     *
     * public void setGroups(ArrayList<Group> groups) {
     * this.groups = groups;
     * }
     *
     * public String getHost() {
     * return host;
     * }
     *
     * public void setHost(String host) {
     * this.host = host;
     * }
     *
     * public int getId() {
     * return id;
     * }
     *
     * public void setId(int id) {
     * this.id = id;
     * }
     *
     * public ArrayList<Stream> getStreams() {
     * return streams;
     * }
     *
     * @Override
     * public String toString() {
     * return "ServerStatus{}";
     * }
     *
     * public Server getServer() {
     * return server;
     * }
     */
}

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

/**
 * Generic POJO for Unmashalling Json Response from Snapcast
 */
public class Client {

    private boolean connected;
    private Host host;
    private ClientConfig config;
    private ClientLastSeen lastSeen;
    private SnapClient snapclient;

    public Client() {
    }

    public Host getHost() {
        return host;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public void setConfig(ClientConfig config) {
        this.config = config;
    }

    public void setLastSeen(ClientLastSeen lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setSnapclient(SnapClient snapclient) {
        this.snapclient = snapclient;
    }

    public Boolean isConnected() {
        return connected;
    }

    public ClientConfig getConfig() {
        return config;
    }

    class ClientLastSeen {
        private Integer usec;
        private Long sec;

        public ClientLastSeen() {

        }

        public void setUsec(Integer usec) {
            this.usec = usec;
        }

        public void setSec(Long sec) {
            this.sec = sec;
        }
    }
}

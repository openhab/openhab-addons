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
public class Server {
    private Host host;
    private SnapServer snapserver;

    public void setSnapserver(SnapServer snapServer) {
        this.snapserver = snapServer;
    }

    public Server() {
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public SnapServer getSnapserver() {
        return snapserver;
    }
}

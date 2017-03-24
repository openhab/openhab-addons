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
public class Host {
    private String ip;
    private String mac;
    private String name;
    private String arch;
    private String os;

    public Host() {
    }

    public String getMac() {
        return mac;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }
}

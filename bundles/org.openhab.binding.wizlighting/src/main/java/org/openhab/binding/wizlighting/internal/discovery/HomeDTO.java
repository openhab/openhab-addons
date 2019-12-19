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
package org.openhab.binding.wizlighting.internal.discovery;

/**
 * This is {@link HomeDTO} Object to parse Discovery response of Home Items.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class HomeDTO {

    static class Light {
        public int id;
        public int home_id;
        public String name;
        public String mac_address;
        public String fw_version;
        public String ip;
        public boolean connection_status;
    }

    static class Data {
        public int id;
        public String username;
        public Light[] lights;
    }

    public boolean success;
    public Data data;

    public Light[] getLights() {
        if (this.data != null && this.data.lights != null && this.data.lights.length > 0) {
            return this.data.lights;
        } else {
            return null;
        }
    }
}

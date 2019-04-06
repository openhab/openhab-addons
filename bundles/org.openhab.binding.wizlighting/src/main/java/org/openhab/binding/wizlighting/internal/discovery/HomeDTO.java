/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

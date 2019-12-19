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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This POJO represents User Details for discovery
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class UserDTO {

    @NonNullByDefault
    static class Home {
        public int id;
        public String name;
    }

    @NonNullByDefault
    static class Data {
        public int id;
        public String username;
        public Home[] homes;
    }

    public boolean success;
    public @Nullable Data data;

    public int getHomeId() {
        if (this.data != null && this.data.homes != null && this.data.homes.length > 0) {
            return this.data.homes[0].id;
        } else {
            return -1;
        }
    }
}

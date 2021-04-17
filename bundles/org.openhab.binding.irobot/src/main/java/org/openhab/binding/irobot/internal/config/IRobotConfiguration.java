/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.irobot.internal.config;

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.UNKNOWN;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.irobot.internal.IRobotBindingConstants.Models;

/**
 * The {@link IRobotConfiguration} is a class for IRobot thing configuration
 *
 * @author Pavel Fedin - Initial contribution
 * @author Alexander Falkenstern - Add supported robot type
 */
@NonNullByDefault
public class IRobotConfiguration {
    private String address = UNKNOWN;
    private String password = UNKNOWN;
    private String family = UNKNOWN;
    private String blid = UNKNOWN;

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address.trim();
    }

    public String getPassword() {
        return password.isBlank() ? UNKNOWN : password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Models getFamily() {
        return Models.fromString(family);
    }

    public void setFamily(final Models family) {
        this.family = family.toString();
    }

    public String getBlid() {
        return blid.isBlank() ? UNKNOWN : blid;
    }

    public void setBlid(final String blid) {
        this.blid = blid;
    }
}

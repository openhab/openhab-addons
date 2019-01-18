/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Wouter Born - Add Thing properties
 */
@NonNullByDefault
public class Version {

    private long major;
    private long minor;

    public Version(long major, long minor) {
        this.major = major;
        this.minor = minor;
    }

    public long getMajor() {
        return major;
    }

    public long getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

}

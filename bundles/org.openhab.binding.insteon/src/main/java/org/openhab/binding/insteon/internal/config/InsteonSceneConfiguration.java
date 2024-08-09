/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonSceneConfiguration} is the configuration for an insteon scene thing.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonSceneConfiguration {

    private int group = -1;

    public int getGroup() {
        return group;
    }

    @Override
    public String toString() {
        String s = "";
        s += " group=" + group;
        return s;
    }
}

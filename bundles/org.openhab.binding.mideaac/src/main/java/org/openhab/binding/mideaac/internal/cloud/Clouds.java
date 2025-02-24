/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.cloud;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Clouds} class securely stores email and password
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - JavaDoc
 */
@NonNullByDefault
public class Clouds {

    private final HashMap<Integer, Cloud> clouds;

    /**
     * Cloud Provider data
     */
    public Clouds() {
        clouds = new HashMap<Integer, Cloud>();
    }

    private Cloud add(String email, String password, CloudProvider cloudProvider) {
        int hash = (email + password + cloudProvider.name()).hashCode();
        Cloud cloud = new Cloud(email, password, cloudProvider);
        clouds.put(hash, cloud);
        return cloud;
    }

    /**
     * Gets user provided cloud provider data
     * 
     * @param email your email
     * @param password your password
     * @param cloudProvider your Cloud Provider
     * @return parameters for cloud provider
     */
    public @Nullable Cloud get(String email, String password, CloudProvider cloudProvider) {
        int hash = (email + password + cloudProvider.name()).hashCode();
        if (clouds.containsKey(hash)) {
            return clouds.get(hash);
        }
        return add(email, password, cloudProvider);
    }
}

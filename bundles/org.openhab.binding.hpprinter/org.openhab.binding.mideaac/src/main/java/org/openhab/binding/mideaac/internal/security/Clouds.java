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
package org.openhab.binding.mideaac.internal.security;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Clouds} class securely stores email and password
 *
 * @author Jacek Dobrowolski - Initial Contribution
 */
@NonNullByDefault
public class Clouds {

    private final HashMap<Integer, CloudDTO> clouds;

    public Clouds() {
        clouds = new HashMap<Integer, CloudDTO>();
    }

    private CloudDTO add(String email, String password, CloudProvider cloudProvider) {
        int hash = (email + password + cloudProvider.getName()).hashCode();
        CloudDTO cloud = new CloudDTO(email, password, cloudProvider);
        clouds.put(hash, cloud);
        return cloud;
    }

    public @Nullable CloudDTO get(String email, String password, CloudProvider cloudProvider) {
        int hash = (email + password + cloudProvider.getName()).hashCode();
        if (clouds.containsKey(hash)) {
            return clouds.get(hash);
        }
        return add(email, password, cloudProvider);
    }
}

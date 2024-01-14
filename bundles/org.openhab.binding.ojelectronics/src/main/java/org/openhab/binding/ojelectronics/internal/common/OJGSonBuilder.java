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
package org.openhab.binding.ojelectronics.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Builder for Gson
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public final class OJGSonBuilder {

    /**
     * Gets a correct initialized {@link Gson}
     *
     * @return {@link com.google.gson.GSon}
     */
    public static Gson getGSon() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }
}

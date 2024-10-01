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
package org.openhab.binding.salus.internal.aws.http;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Copied from org.openhab.binding.windcentrale.internal.dto.CognitoGson
 * 
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class CognitoGson {

    public static final Gson GSON = new GsonBuilder()//
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)//
            .registerTypeAdapter(Instant.class, new InstantDeserializer())//
            .create();
}

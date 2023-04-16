/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.windcentrale.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link CognitoGson} class provides a {@link Gson} instance configured for (de)serializing all Cognito data
 * from/to JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class CognitoGson {

    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
}

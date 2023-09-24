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
package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * The {@link Header} class is used for serializing and deserializing of JSONs.
 * It contains the data lika namespace, name, messageId, payloadVersion and authorization.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Header {
    @Expose
    public String namespace = "";
    @Expose
    public String name = "";
    @Expose
    public String messageId = "";
    @Expose
    public String payloadVersion = "";

    public String authorization = "";
}

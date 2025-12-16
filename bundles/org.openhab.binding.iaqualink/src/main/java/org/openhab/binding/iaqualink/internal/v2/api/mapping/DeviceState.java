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
package org.openhab.binding.iaqualink.internal.v2.api.mapping;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * This class wraps JSONPath document context, insulating clients from needing to understand the underlying
 * JSONPath library and API, and ensuring that parsed JSON is cached where possible.
 *
 * @author Jonathan Gilbert
 */
@NonNullByDefault
public class DeviceState {
    DocumentContext documentContext;

    DeviceState(DocumentContext documentContext) {
        this.documentContext = documentContext;
    }

    public String jsonString() {
        return documentContext.jsonString();
    }

    public static DeviceState parse(String jsonString) {
        return new DeviceState(JsonPath.parse(jsonString));
    }
}

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
package org.openhab.binding.myuplink.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 * this is an interface which can be implemented by different transformer classes
 *
 * @author Anders Alfredsson - initial contribution
 */
@NonNullByDefault
public interface ResponseTransformer {

    /**
     * Transform the received data into a Map of channels and the State they should be updated to
     *
     * @param jsonData The input json data
     * @param group The channel group
     * @return
     */
    Map<Channel, State> transform(JsonObject jsonData, String group);
}

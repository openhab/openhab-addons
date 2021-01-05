/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.client.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

/**
 * The Web Thing Description. Refer https://iot.mozilla.org/wot/#web-thing-description
 *
 * @author Gregor Roth - Initial contribution
 */
public class WebThingDescription {

    public String id = null;

    public String title = "";

    @SerializedName("@context")
    public String contextKeyword = "";

    public Map<String, Property> properties = Map.of();

    public List<Link> links = List.of();

    public Optional<Property> getProperty(String propertyName) {
        return Optional.ofNullable(properties.get(propertyName));
    }
}

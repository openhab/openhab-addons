/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.net.URI;
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

    /**
     * convenience method to read properties
     * 
     * @param propertyName the property name to read
     * @return the property value
     */
    public Optional<Property> getProperty(String propertyName) {
        return Optional.ofNullable(properties.get(propertyName));
    }

    /**
     * convenience method to read the event stream uri
     * 
     * @return the optional event stream uri
     */
    public Optional<URI> getEventStreamUri() {
        for (var link : this.links) {
            var href = link.href;
            if ((href != null) && href.startsWith("ws")) {
                var rel = Optional.ofNullable(link.rel).orElse("<undefined>");
                if (rel.equals("alternate")) {
                    return Optional.of(URI.create(href));
                }
            }
        }
        return Optional.empty();
    }
}

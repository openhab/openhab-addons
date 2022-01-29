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
package org.openhab.binding.heos.internal.json.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract parent class for the HEOS event/response objects
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public abstract class HeosObject {
    private final Logger logger = LoggerFactory.getLogger(HeosObject.class);

    public final String rawCommand;
    private final Map<String, String> attributes;

    HeosObject(String rawCommand, Map<String, String> attributes) {
        this.rawCommand = rawCommand;
        this.attributes = attributes;
    }

    public boolean getBooleanAttribute(HeosCommunicationAttribute attributeName) {
        return "on".equals(attributes.get(attributeName.getLabel()));
    }

    public @Nullable Long getNumericAttribute(HeosCommunicationAttribute attributeName) {
        @Nullable
        String attribute = attributes.get(attributeName.getLabel());

        if (attribute == null) {
            return null;
        }

        try {
            return Long.valueOf(attribute);
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse number: {}, message: {}", attribute, e.getMessage());
            return null;
        }
    }

    public @Nullable String getAttribute(HeosCommunicationAttribute attributeName) {
        return attributes.get(attributeName.getLabel());
    }

    public boolean hasAttribute(HeosCommunicationAttribute attribute) {
        return attributes.containsKey(attribute.getLabel());
    }

    @Override
    public String toString() {
        return "HeosObject{" + "rawCommand='" + rawCommand + '\'' + ", attributes=" + attributes + '}';
    }
}

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
package org.openhab.binding.webthing.internal.client;

import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;

/**
 * A WebThing represents the client-side proxy of a remote devices implementing the Web Thing API according to
 * https://iot.mozilla.org/wot/
 * The API design is oriented on https://www.w3.org/TR/wot-scripting-api/#the-consumedthing-interface
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface ConsumedThing {

    /**
     * @return the description (meta data) of the WebThing
     */
    WebThingDescription getThingDescription();

    /**
     * Makes a request for Property value change notifications
     *
     * @param propertyName the property to be observed
     * @param listener the listener to call on changes
     */
    void observeProperty(String propertyName, BiConsumer<String, Object> listener);

    /**
     * Writes a single Property.
     *
     * @param propertyName the propertyName
     * @return the current propertyValue
     * @throws PropertyAccessException if the property can not be read
     */
    Object readProperty(String propertyName) throws PropertyAccessException;

    /**
     * Writes a single Property.
     *
     * @param propertyName the propertyName
     * @param newValue the new propertyValue
     * @throws PropertyAccessException if the property can not be written
     */
    void writeProperty(String propertyName, Object newValue) throws PropertyAccessException;

    /**
     * @return true, if connection is alive
     */
    boolean isAlive();

    /**
     * closes the connection
     */
    void close();
}

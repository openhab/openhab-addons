/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.IOException;

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
     * @throws IOException if the Webthing resource could not be connected
     */
    void observeProperty(String propertyName, PropertyChangedListener listener) throws IOException;

    /**
     * Writes a single Property.
     *
     * @param propertyName the propertyName
     * @return the current propertyValue
     * @throws IOException if the WebThing resource could not be connected
     */
    Object readProperty(String propertyName) throws IOException;

    /**
     * Writes a single Property.
     *
     * @param propertyName the propertyName
     * @param newValue the new propertyValue
     * @throws IOException if the WebThing resource could not be connected
     */
    void writeProperty(String propertyName, Object newValue) throws IOException;

    /**
     * closes the connection
     */
    void destroy();
}

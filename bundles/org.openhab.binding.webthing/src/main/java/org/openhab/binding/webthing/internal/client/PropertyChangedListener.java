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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener that will be notified, if a specific property of the WebThing is changed
 * 
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface PropertyChangedListener {

    /**
     * callback that will be called, if a WebThing property value is changed
     * 
     * @param webThing the associated WebThing
     * @param propertyName the WebThing property name
     * @param value the new WebThing property value
     */
    default void onPropertyValueChanged(ConsumedThing webThing, String propertyName, Object value) {
    }
}

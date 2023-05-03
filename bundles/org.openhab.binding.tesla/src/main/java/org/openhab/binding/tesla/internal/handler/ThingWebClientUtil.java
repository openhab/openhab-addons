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
package org.openhab.binding.tesla.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingUID;

/**
 * {@link ThingWebClientUtil} provides an utility method to create a valid consumer name for web clients.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class ThingWebClientUtil {

    private static final int MAX_CONSUMER_NAME_LENGTH = 20;

    /**
     * Build a valid consumer name for HTTP or WebSocket client.
     *
     * @param uid thing UID for which to associate HTTP or WebSocket client
     * @param prefix a prefix to consider for the name; can be null
     * @return a valid consumer name
     */
    public static String buildWebClientConsumerName(ThingUID uid, @Nullable String prefix) {
        String pref = prefix == null ? "" : prefix;
        String name = pref + uid.getAsString().replace(':', '-');
        if (name.length() > MAX_CONSUMER_NAME_LENGTH) {
            // Try to use only prefix + binding ID + thing ID
            name = pref + uid.getBindingId();
            if (name.length() > (MAX_CONSUMER_NAME_LENGTH / 2)) {
                // Truncate the binding ID to keep enough place for thing ID
                name = name.substring(0, MAX_CONSUMER_NAME_LENGTH / 2);
            }
            // Add the thing ID
            String id = uid.getId();
            int maxIdLength = MAX_CONSUMER_NAME_LENGTH - 1 - name.length();
            if (id.length() > maxIdLength) {
                // If thing ID is too big, use a hash code of the thing UID instead of thing id
                // and truncate it if necessary
                id = buildHashCode(uid, maxIdLength);
            }
            name += "-" + id;
        }
        return name;
    }

    // Make the method public just to be able to call it inside the tests
    static String buildHashCode(ThingUID uid, int maxLength) {
        String result = Integer.toHexString(uid.hashCode());
        if (result.length() > maxLength) {
            result = result.substring(result.length() - maxLength);
        }
        return result;
    }
}

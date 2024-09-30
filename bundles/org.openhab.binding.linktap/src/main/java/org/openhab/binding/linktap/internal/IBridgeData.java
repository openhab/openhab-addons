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
package org.openhab.binding.linktap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines a interface that Things under the Bridge can implement to receive
 * callbacks, when the bridges configuration data has been updated.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface IBridgeData {

    /**
     * Any things under a Bridge that implement this interface, will have this
     * invoked after new configuration data has been retrieved from the GW.
     */
    void handleBridgeDataUpdated();
}

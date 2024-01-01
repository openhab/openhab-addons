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
package org.openhab.binding.insteon.internal.driver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for classes that want to listen to notifications from
 * the driver.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public interface DriverListener {
    /**
     * Notification that querying of the modems on all ports has successfully completed.
     */
    void driverCompletelyInitialized();

    /**
     * Notification that the driver was disconnected
     */
    void disconnected();
}

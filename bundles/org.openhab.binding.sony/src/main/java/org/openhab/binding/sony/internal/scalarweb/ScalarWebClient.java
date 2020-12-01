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
package org.openhab.binding.sony.internal.scalarweb;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;

/**
 * This interface represents a scalar web client
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface ScalarWebClient extends AutoCloseable {

    /**
     * Gets the device manager
     *
     * @return the non-null device manager
     */
    ScalarWebDeviceManager getDevice();

    /**
     * Gets the service for the specified name
     *
     * @param serviceName the service name
     * @return the service or null if not found
     */
    @Nullable
    ScalarWebService getService(final String serviceName);
}

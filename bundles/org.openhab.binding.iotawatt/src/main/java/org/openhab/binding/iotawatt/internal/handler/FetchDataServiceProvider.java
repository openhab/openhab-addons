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
package org.openhab.binding.iotawatt.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.iotawatt.internal.service.DeviceHandlerCallback;
import org.openhab.binding.iotawatt.internal.service.FetchDataService;

/**
 * Provides a FetchDataService.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public interface FetchDataServiceProvider {
    /**
     * Get the service to handle data fetching.
     *
     * @param deviceHandlerCallback The DeviceHandlerCallback to assign to the FetchDataService
     * @return The provided FetchDataService
     */
    FetchDataService getFetchDataService(DeviceHandlerCallback deviceHandlerCallback);
}

/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api;

import java.io.IOException;

/**
 * Interface for the client to get data from the API of the evnotify online service.
 *
 * @author Michael Schmidt - Initial contribution
 */
public interface EVNotifyClient {

    /**
     * returns a {@link ChargingData}
     *
     * @return state of a car
     */
    ChargingData getCarChargingData() throws IOException, InterruptedException, ApiException;
}

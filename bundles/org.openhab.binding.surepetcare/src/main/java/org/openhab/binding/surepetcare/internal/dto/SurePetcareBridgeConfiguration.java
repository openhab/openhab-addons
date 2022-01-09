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
package org.openhab.binding.surepetcare.internal.dto;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

/**
 * The {@link SurePetcareBridgeConfiguration} is a container for all the bridge configuration.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareBridgeConfiguration {

    public String username;
    public String password;
    public long refreshIntervalTopology = DEFAULT_REFRESH_INTERVAL_TOPOLOGY;
    public long refreshIntervalStatus = DEFAULT_REFRESH_INTERVAL_STATUS;
}

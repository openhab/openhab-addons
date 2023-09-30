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
package org.openhab.binding.nzwateralerts.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WaterWebService} class contains the common interfaces for the different
 * services.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public interface WaterWebService {
    String service();

    String endpoint(String region);

    int findWaterLevel(String data, String area);
}

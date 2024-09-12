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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WundergroundUpdateReceiverConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
public class WundergroundUpdateReceiverConfiguration {

    /**
     * The station id as registered by wunderground.com or any string if not forwarding data to wunderground.com.
     * This value will be used as the representation property.
     */
    public String stationId = "";
}

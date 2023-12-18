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
package org.openhab.binding.thekeys.internal.gateway;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TheKeysGatewayConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysGatewayConfiguration {

    public String host = "";
    public String code = "";
    public int refreshInterval = 5;
    public int apiTimeout = 30;
}

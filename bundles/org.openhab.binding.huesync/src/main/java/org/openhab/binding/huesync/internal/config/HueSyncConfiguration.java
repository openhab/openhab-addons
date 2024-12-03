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
package org.openhab.binding.huesync.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Binding configuration parameters,
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncConfiguration {
    public String registrationId = "";
    public String apiAccessToken = "";
    public String host = "";
    public Integer port = 443;
    public Integer statusUpdateInterval = 10;
}

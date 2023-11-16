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
package org.openhab.binding.tellstick.internal.conf;

/**
 * Configuration class for Tellstick bridge used to connect to the
 * Telldus Live service.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TelldusLiveConfiguration {
    public String publicKey;
    public String privateKey;
    public String token;
    public String tokenSecret;
    public long refreshInterval;
}

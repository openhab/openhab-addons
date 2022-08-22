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
package org.openhab.binding.smsmodem.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SMSModemRemoteBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class SMSModemRemoteBridgeConfiguration {

    public String ip = "";
    public Integer networkPort = 2000;
    public String simPin = "";
    public Integer pollingInterval = 15;
    public Integer delayBetweenSend = 0;
}

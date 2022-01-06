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
package org.openhab.binding.souliss.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayConfig} is responsible for holding souliss gateway config
 *
 * @author Luca Calcaterra - Initial Contribution
 */
@NonNullByDefault
public final class GatewayConfig {
    public int pingInterval;
    public int subscriptionInterval;
    public int healthyInterval;
    public int sendInterval;
    public int timeoutToRequeue;
    public int timeoutToRemovePacket;
    public int preferredLocalPortNumber;
    public int gatewayPortNumber;
    public int userIndex;
    public int nodeIndex;
    public String gatewayLanAddress = "";
    public String gatewayWanAddress = "";
}

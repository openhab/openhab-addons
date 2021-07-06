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
package org.openhab.binding.souliss.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayConfig} is responsible for holding souliss gateway config
 *
 * @author Luca Calcaterra - Initial Contribution
 */
@NonNullByDefault
public final class GatewayConfig {
    public static int pingInterval;
    public static int subscriptionInterval;
    public static int healthyInterval;
    public static int sendInterval;
    public static int timeoutToRequeue;
    public static int timeoutToRemovePacket;
    public static int preferredLocalPortNumber;
    public static int gatewayPortNumber;
    public static int userIndex;
    public static int nodeIndex;
    public static String gatewayLanAddress = "";
    public static String gatewayWanAddress = "";
}

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
package org.openhab.binding.souliss.internal.discovery;

import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result callback interface.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public interface DiscoverResult {
    static boolean IS_GATEWAY_DETECTED = false;

    void gatewayDetected(InetAddress addr, String id);

    void thingDetectedTypicals(byte lastByteGatewayIP, byte typical, byte node, byte slot);

    void thingDetectedActionMessages(String sTopicNumber, String sTopicVariant);
}

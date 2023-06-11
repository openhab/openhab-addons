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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenThermGatewayCallback} is used as a callback interface by a connector to signal status
 * and relay incoming messages to be processed by the binding.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public interface OpenThermGatewayCallback {

    void connectionStateChanged(ConnectionState state);

    void receiveMessage(Message message);

    void receiveAcknowledgement(String message);
}

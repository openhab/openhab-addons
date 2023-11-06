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

import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenThermGatewayConnector} interface is used to allow multiple types of connectors
 * to be implemented and used to connect to the OpenTherm Gateway.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public interface OpenThermGatewayConnector extends Callable<Boolean> {
    void sendCommand(GatewayCommand command);

    boolean isConnected();

    void stop();

    void start();
}

/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;

/** 
 * @author Arjen Korevaar - Initial contribution
 */
public interface OpenThermGatewayCallback {
    public void connecting();

    public void connected();

    public void disconnected();

    public void receiveMessage(@NonNull Message message);

    public void log(@NonNull LogLevel loglevel, @NonNull String message);

    public void log(@NonNull LogLevel loglevel, @NonNull String format, @NonNull String arg);

    public void log(@NonNull LogLevel loglevel, @NonNull String format, @NonNull Throwable t);
}

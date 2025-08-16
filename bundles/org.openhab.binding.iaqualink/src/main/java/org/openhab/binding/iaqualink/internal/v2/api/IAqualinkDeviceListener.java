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
package org.openhab.binding.iaqualink.internal.v2.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener for IAqualink device messages
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public interface IAqualinkDeviceListener {
    void onGetAccepted(String deviceId, String msg);

    void onUpdateAccepted(String deviceId, String msg);

    void onUpdateRejected(String deviceId, String msg);

    void onDisconnected(String deviceId);
}

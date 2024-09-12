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
package org.openhab.binding.miio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Interface for a listener on the {@link org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication}.
 * Informs when a message is received.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public interface MiIoMessageListener {
    /**
     * Callback method for the {@link MiIoMessageListener}
     *
     * @param cmd The received message in JSON format
     */
    void onMessageReceived(MiIoSendCommand cmd);

    /**
     * Callback method for the {@link MiIoMessageListener}
     *
     * @param status Status online/offline
     * @param statusDetail Status details text
     */
    void onStatusUpdated(ThingStatus status, ThingStatusDetail statusDetail);
}

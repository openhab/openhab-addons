/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;

/**
 * Interface for a listener on the {@link MiIoAsyncCommunication}.
 * Informs when a message is received.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
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

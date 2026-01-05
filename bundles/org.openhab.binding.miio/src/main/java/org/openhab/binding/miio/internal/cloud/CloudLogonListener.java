/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginState;

/**
 * Interface for a listener on the {@link org.openhab.binding.miio.cloud.MiCloudConnector}.
 * Informs when login information is updated.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public interface CloudLogonListener {
    /**
     * Callback method for the {@link MiIoMessageListener}
     *
     * @param image the captcha image as jpg byte array
     */
    void onLogonImage(byte[] image);

    /**
     * Callback method for the {@link MiIoMessageListener}
     *
     * @param status Status text
     */
    void onStatusUpdated(CloudLoginState loginState, String status);
}

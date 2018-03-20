/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.internal.azureiothub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

/**
 * This triggered in case of cloud to device communication
 *
 * @author Niko Tanghe - Initial contribution
 * @author Kai Kreuzer - code cleanup
 */

public class AzureEventCallback implements IotHubEventCallback {
    private final Logger logger = LoggerFactory.getLogger(AzureEventCallback.class);

    @Override
    public void execute(IotHubStatusCode status, Object context) {
        logger.debug("IoT Hub responded to message with status {}", status.name());

        if (context != null) {
            synchronized (context) {
                context.notify();
            }
        }
    }
}

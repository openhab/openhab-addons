/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetChannel;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public interface ScanListener extends EventListener {

    /**
     * Called when the scan is stopped on error.
     */
    void onScanError();

    /**
     * Called when the scan is completed successfully
     */
    void onScanCompleted();

    /**
     * Called when device found.
     */
    void onDeviceFound(int macAddress, String firmware, String hardware, Map<Integer, OpenWebNetChannel> channels);
}

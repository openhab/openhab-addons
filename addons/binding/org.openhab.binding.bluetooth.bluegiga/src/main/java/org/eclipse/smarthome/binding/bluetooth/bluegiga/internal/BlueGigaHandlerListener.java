/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A listener to track {@link BlueGigaSerialHandler} life cycle events.
 *
 * @author Chris Jackson - Initial contribution and API
 */
@NonNullByDefault
public interface BlueGigaHandlerListener {

    /**
     * Notifies when the handler gets closed because of the reason specified as an argument.
     *
     * @param reason a reason caused to be closed
     */
    void bluegigaClosed(Exception reason);

}

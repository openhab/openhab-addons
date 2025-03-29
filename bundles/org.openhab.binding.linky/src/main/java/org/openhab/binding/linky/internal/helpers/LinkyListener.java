/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.handler.BridgeLocalBaseHandler;

/**
 * The {@link LinkyListener} interface defines all events pushed by a
 * {@link BridgeLocalBaseHandler}.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Laurent Arnal - Refactor to integrate into Linky Binding
 */
@NonNullByDefault
public interface LinkyListener {
    /**
     * Procedure to receive a frame from ther meter
     *
     * @param frame
     *            the frame received
     */
    void onFrameReceived(final LinkyFrame frame);
}

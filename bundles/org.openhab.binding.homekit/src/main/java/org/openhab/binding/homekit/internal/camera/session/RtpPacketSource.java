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
package org.openhab.binding.homekit.internal.camera.session;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Pushes RTP packets to a downstream callback, potentially from its own worker thread.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface RtpPacketSource {

    /**
     * Registers the downstream callback before any input packets are accepted or relayed.
     */
    void connectTo(RtpPacketSink downstream);
}

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teleinfo.internal.data.Frame;

/**
 * The {@link TeleinfoReaderListener} interface defines all events pushed by a {@link TeleinfoReader}.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public interface TeleinfoReaderListener {

    default void onFrameReceived(final TeleinfoReader reader, final Frame frame) {
        // NOP
    }

    default void onOpening(final TeleinfoReader reader) {
        // NOP
    }

    default void onOpened(final TeleinfoReader reader) {
        // NOP
    }

    default void onClosing(final TeleinfoReader reader) {
        // NOP
    }

    default void onClosed(final TeleinfoReader reader) {
        // NOP
    }
}

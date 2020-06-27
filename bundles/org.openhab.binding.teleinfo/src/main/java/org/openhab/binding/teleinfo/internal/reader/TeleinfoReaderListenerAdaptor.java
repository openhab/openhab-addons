/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.teleinfo.internal.dto.Frame;

/**
 * The {@link TeleinfoReaderListenerAdaptor} class defines an adaptor at {@link TeleinfoReaderListener} interface.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoReaderListenerAdaptor implements TeleinfoReaderListener {

    @Override
    public void onFrameReceived(TeleinfoReader reader, Frame frame) {
        // NOP
    }

    @Override
    public void onOpening(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onOpened(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onClosing(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onClosed(TeleinfoReader reader) {
        // NOP
    }
}

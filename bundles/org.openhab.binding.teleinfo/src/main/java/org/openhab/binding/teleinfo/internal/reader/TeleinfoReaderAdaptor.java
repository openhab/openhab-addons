/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teleinfo.internal.data.Frame;

/**
 * The {@link TeleinfoReaderAdaptor} class defines an adaptor at {@link TeleinfoReader} interface.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoReaderAdaptor implements TeleinfoReader {

    private List<TeleinfoReaderListener> listeners = new ArrayList<>();

    @Override
    public void addListener(final TeleinfoReaderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final TeleinfoReaderListener listener) {
        listeners.remove(listener);
    }

    protected void fireOnFrameReceivedEvent(final Frame frame) {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onFrameReceived(this, frame);
        }
    }

    protected void fireOnOpeningEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onOpening(this);
        }
    }

    protected void fireOnOpenedEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onOpened(this);
        }
    }

    protected void fireOnClosingEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onClosing(this);
        }
    }

    protected void fireOnClosedEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onClosed(this);
        }
    }

    public List<TeleinfoReaderListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }
}

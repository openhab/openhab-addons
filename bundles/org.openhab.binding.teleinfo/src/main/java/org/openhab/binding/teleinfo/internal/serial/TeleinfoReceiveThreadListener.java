/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.serial;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;

/**
 * The {@link TeleinfoReceiveThreadListener} interface defines all events pushed by a {@link TeleinfoReceiveThread}.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public interface TeleinfoReceiveThreadListener {

    void onFrameReceived(@NonNull final TeleinfoReceiveThread receiveThread, @NonNull final Frame frame);

    void onInvalidFrameReceived(@NonNull final TeleinfoReceiveThread receiveThread,
            @NonNull final InvalidFrameException error);

    void onSerialPortInputStreamIOException(@NonNull final TeleinfoReceiveThread receiveThread,
            @NonNull final IOException e);

    boolean continueOnReadNextFrameTimeoutException(@NonNull final TeleinfoReceiveThread receiveThread,
            @NonNull final TimeoutException e);
}

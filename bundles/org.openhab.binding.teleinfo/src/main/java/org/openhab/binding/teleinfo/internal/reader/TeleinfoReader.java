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

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TeleinfoReader} interface defines a mechanism to read Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public interface TeleinfoReader extends Closeable {

    void open() throws IOException;

    void addListener(final TeleinfoReaderListener listener);

    void removeListener(final TeleinfoReaderListener listener);
}

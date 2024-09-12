/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linuxinput.internal.evdev4j;

import static org.openhab.binding.linuxinput.internal.evdev4j.Utils.constantFromInt;

import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import jnr.constants.platform.linux.Errno;
import jnr.posix.POSIX;

/**
 * Exception wrapping an operating system errno.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public class LastErrorException extends RuntimeException {
    private static final long serialVersionUID = 3112920209797990207L;
    private final int errno;

    LastErrorException(POSIX posix, int errno) {
        super("Error " + errno + ": " + posix.strerror(errno));
        this.errno = errno;
    }

    LastErrorException(POSIX posix, int errno, String detail) {
        super(MessageFormat.format("Error ({0}) for {1}: {2}", errno, detail, posix.strerror(errno)));
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }

    public Optional<Errno> getError() {
        return constantFromInt(Errno.values(), errno);
    }
}

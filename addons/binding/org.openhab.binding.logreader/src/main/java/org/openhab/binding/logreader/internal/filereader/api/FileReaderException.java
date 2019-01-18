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
package org.openhab.binding.logreader.internal.filereader.api;

/**
 * Exception for file reader errors.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class FileReaderException extends Exception {
    private static final long serialVersionUID = 1272957002073978608L;

    public FileReaderException(String message) {
        super(message);
    }

    public FileReaderException(Throwable cause) {
        super(cause);
    }
}

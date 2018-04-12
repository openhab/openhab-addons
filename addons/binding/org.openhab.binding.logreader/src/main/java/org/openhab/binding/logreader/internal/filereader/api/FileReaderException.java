/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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
package org.openhab.binding.logreader.internal.filereader.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for file reader listeners.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public interface FileReaderListener {

    /**
     * This method is called if the file is not found.
     */
    void fileNotFound();

    /**
     * This method is called if a file rotation is detected.
     *
     */
    void fileRotated();

    /**
     * This method is called when new line is detected.
     *
     * @param line the line.
     */
    void handle(@Nullable String line);

    /**
     * This method is called when exception has occurred.
     *
     * @param ex the exception.
     */
    void handle(@Nullable Exception ex);
}

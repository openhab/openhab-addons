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
 * Interface for file reader listeners.
 *
 * @author Pauli Anttila - Initial contribution
 */
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
    void handle(String line);

    /**
     * This method is called when exception has occurred.
     *
     * @param ex the exception.
     */
    void handle(Exception ex);
}

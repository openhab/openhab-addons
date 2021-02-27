/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.benqprojector.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.benqprojector.internal.BenqProjectorException;

/**
 * Base class for BenQ projector communication.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public interface BenqProjectorConnector {

    public static final String START = "\r*";
    public static final String END = "#\r";
    public static final String RESP_START = ">*";

    /**
     * Procedure for connecting to projector.
     *
     * @throws BenqProjectorException
     */
    void connect() throws BenqProjectorException;

    /**
     * Procedure for disconnecting to projector controller.
     *
     * @throws BenqProjectorException
     */
    void disconnect() throws BenqProjectorException;

    /**
     * Procedure for send raw data to projector.
     *
     * @param data
     *            Message to send.
     *
     * @param timeout
     *            timeout to wait response in milliseconds.
     *
     * @throws BenqProjectorException
     */
    String sendMessage(String data, int timeout) throws BenqProjectorException;
}

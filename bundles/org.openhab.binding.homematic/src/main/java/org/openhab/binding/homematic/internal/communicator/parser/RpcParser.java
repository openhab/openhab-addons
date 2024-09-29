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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

/**
 * Interface for all message parsers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcParser<M, R> {

    /**
     * Parses the message returns the result.
     */
    R parse(M message) throws IOException;
}

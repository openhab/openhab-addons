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
package org.openhab.binding.dali.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dali.internal.handler.DaliException;

/**
 * The {@link DaliBackwardFrame} represents a response message on the DALI bus.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliBackwardFrame extends DaliFrame {

    public DaliBackwardFrame(byte data) throws DaliException {
        super(8, new byte[] { data });
    }
}

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * The {@link DaliCommandBase} is an abstract command for DALI devices.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliCommandBase {
    public DaliForwardFrame frame;
    public boolean sendTwice;

    public DaliCommandBase(DaliForwardFrame frame, boolean sendTwice) {
        this.frame = frame;
        this.sendTwice = sendTwice;
    }
}

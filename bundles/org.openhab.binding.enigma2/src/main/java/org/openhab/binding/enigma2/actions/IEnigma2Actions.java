/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IEnigma2Actions} defines the interface for all thing actions supported by the binding.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public interface IEnigma2Actions {
    void sendRcCommand(String rcButton);

    void sendInfo(String text);

    void sendInfo(String text, int timeout);

    void sendWarning(String text);

    void sendWarning(String text, int timeout);

    void sendError(String text);

    void sendError(String text, int timeout);

    void sendQuestion(String text);

    void sendQuestion(String text, int timeout);
}

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
package org.openhab.binding.paradoxalarm.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IResponseReceiver} Used to pass parsed responses from Paradox to original senders of the requests for
 * further processing.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface IResponseReceiver {
    void receiveResponse(IResponse response, IParadoxInitialLoginCommunicator communicator);
}

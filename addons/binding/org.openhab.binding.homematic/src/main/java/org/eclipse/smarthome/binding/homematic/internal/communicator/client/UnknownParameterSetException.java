/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.communicator.client;

import java.io.IOException;

/**
 * Exception if the RPC call returns a unknown -3 Unknown paramset.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class UnknownParameterSetException extends IOException {
    private static final long serialVersionUID = -246970996431236583L;

    public UnknownParameterSetException(String message) {
        super(message);
    }

}

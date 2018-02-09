/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.client;

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

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

public class ZWaveSerialMessageException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2106654578826723533L;

    ZWaveSerialMessageException(String reason) {
        super(reason);
    }
}

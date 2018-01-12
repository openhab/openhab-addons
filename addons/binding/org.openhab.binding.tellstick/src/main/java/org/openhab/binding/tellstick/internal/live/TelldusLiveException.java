/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal.live;

import org.tellstick.device.TellstickException;

/**
 * {@link TelldusLiveException} is used when there is exception communicating with Telldus Live.
 * This exception extends the Telldus Core exception.
 *
 * @author Jarle Hjortland
 */
public class TelldusLiveException extends TellstickException {

    public TelldusLiveException(Exception source) {
        super(null, 0);
        this.initCause(source);
    }

    private static final long serialVersionUID = 3067179547449451158L;

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

}

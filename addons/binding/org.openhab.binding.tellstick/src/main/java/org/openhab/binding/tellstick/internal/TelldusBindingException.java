/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal;

import org.tellstick.device.TellstickException;

/**
 * {@link TelldusBindingException} is used when there is exception communicating with Telldus Live.
 * This exception extends the Telldus Core exception.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TelldusBindingException extends TellstickException {

    private static final long serialVersionUID = 30671795474333158L;

    private String msg;

    public TelldusBindingException(String message) {
        super(null, 0);
        this.msg = message;
    }

    @Override
    public String getMessage() {
        return msg;
    }

}

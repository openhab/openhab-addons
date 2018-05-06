/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.exception;

/**
 * Exception if a device is not found, this happens if a device is requested and the data from Gardena Smart Home has
 * not been loaded.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaDeviceNotFoundException extends GardenaException {

    private static final long serialVersionUID = 2704767320916725490L;

    public GardenaDeviceNotFoundException(String message) {
        super(message);
    }

}

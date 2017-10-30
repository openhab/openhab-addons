/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.seneye.service;

/**
 * There was an error in the seneye configuration
 *
 * @author Niko Tanghe
 */

public class InvalidConfigurationException extends Exception {
    private static final long serialVersionUID = -2894268584378662737L;

    public InvalidConfigurationException(String message) {
        super(message);
    }
}
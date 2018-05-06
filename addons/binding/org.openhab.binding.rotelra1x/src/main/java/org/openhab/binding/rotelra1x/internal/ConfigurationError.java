/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x.internal;

/**
 * Exception to indicate a failure due to incorrect or missing Thing configuration.
 *
 * @author Marius Bjørnstad - Initial contribution
 */

public class ConfigurationError extends Exception {

    private static final long serialVersionUID = 1L;

    public ConfigurationError(String message) {
        super(message);
    }

}

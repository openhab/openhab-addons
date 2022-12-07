/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sensibo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SensiboConfigurationException} class wraps exceptions raised when due to configuration errors
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboConfigurationException extends SensiboException {
    private static final long serialVersionUID = 1L;

    public SensiboConfigurationException(final String message) {
        super(message);
    }
}

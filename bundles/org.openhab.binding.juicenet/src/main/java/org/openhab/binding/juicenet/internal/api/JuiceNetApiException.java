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
package org.openhab.binding.juicenet.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link JuiceNetApiException} implements an API Exception
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiException extends Exception {
    private static final long serialVersionUID = 5421236828224242152L;

    public JuiceNetApiException(String message) {
        super(message);
    }
}

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

/**
 * The {@link SensiboCommunicationException} class wraps exceptions raised when communicating with the API
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboCommunicationException extends SensiboException {
    private static final long serialVersionUID = 1L;

    public SensiboCommunicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SensiboCommunicationException(final String message) {
        super(message);
    }

    public SensiboCommunicationException(final AbstractRequest req, final String overallStatus) {
        super("Server responded with error to request " + req.getClass().getSimpleName() + "/" + req.getRequestUrl()
                + ": " + overallStatus);
    }
}

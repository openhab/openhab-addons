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
package org.openhab.binding.lametrictime.api.local;

import org.openhab.binding.lametrictime.api.local.model.Failure;

public class NotificationNotFoundException extends LaMetricTimeException
{
    private static final long serialVersionUID = 1L;

    public NotificationNotFoundException()
    {
        super();
    }

    public NotificationNotFoundException(String message,
                                         Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotificationNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotificationNotFoundException(String message)
    {
        super(message);
    }

    public NotificationNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public NotificationNotFoundException(Failure failure)
    {
        super(failure);
    }
}

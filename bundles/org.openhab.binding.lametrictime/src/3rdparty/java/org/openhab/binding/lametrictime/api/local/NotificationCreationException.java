/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.local;

import org.openhab.binding.lametrictime.api.local.model.Failure;

public class NotificationCreationException extends LaMetricTimeException
{
    private static final long serialVersionUID = 1L;

    public NotificationCreationException()
    {
        super();
    }

    public NotificationCreationException(String message)
    {
        super(message);
    }

    public NotificationCreationException(Throwable cause)
    {
        super(cause);
    }

    public NotificationCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotificationCreationException(String message,
                                         Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotificationCreationException(Failure failure)
    {
        super(failure);
    }
}

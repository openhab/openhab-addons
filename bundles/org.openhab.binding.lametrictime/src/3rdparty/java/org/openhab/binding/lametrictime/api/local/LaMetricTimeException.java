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

import java.util.List;

import org.openhab.binding.lametrictime.api.local.model.Error;
import org.openhab.binding.lametrictime.api.local.model.Failure;

public class LaMetricTimeException extends Exception
{
    private static final long serialVersionUID = 1L;

    public LaMetricTimeException()
    {
        super();
    }

    public LaMetricTimeException(String message)
    {
        super(message);
    }

    public LaMetricTimeException(Throwable cause)
    {
        super(cause);
    }

    public LaMetricTimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LaMetricTimeException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public LaMetricTimeException(Failure failure)
    {
        super(buildMessage(failure));
    }

    private static String buildMessage(Failure failure)
    {
        StringBuilder builder = new StringBuilder();

        List<Error> errors = failure.getErrors();
        if (!errors.isEmpty())
        {
            builder.append(errors.get(0).getMessage());
        }

        for (int i = 1; i < errors.size(); i++)
        {
            builder.append("; ").append(errors.get(i).getMessage());
        }

        return builder.toString();
    }
}

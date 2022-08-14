/*
 * Copyright 2017 Gregory Moyer
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
package org.openhab.binding.sleepiq.api.model;

import com.google.gson.annotations.SerializedName;

public class Error
{
    @SerializedName("Code")
    private Long code;
    @SerializedName("Message")
    private String message;

    public Long getCode()
    {
        return code;
    }

    public void setCode(Long code)
    {
        this.code = code;
    }

    public Error withCode(Long code)
    {
        setCode(code);
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Error withMessage(String message)
    {
        setMessage(message);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof Error))
        {
            return false;
        }
        Error other = (Error)obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        }
        else if (!code.equals(other.code))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Error [code=");
        builder.append(code);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }
}

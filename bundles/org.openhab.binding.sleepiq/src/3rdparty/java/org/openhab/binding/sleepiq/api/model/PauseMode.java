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
package org.openhab.binding.sleepiq.api.model;

public class PauseMode
{
    private String accountId;
    private String bedId;
    private String pauseMode;

    public String getAccountId()
    {
        return accountId;
    }

    public void setAccountId(String accountId)
    {
        this.accountId = accountId;
    }

    public PauseMode withAccountId(String accountId)
    {
        setAccountId(accountId);
        return this;
    }

    public String getBedId()
    {
        return bedId;
    }

    public void setBedId(String bedId)
    {
        this.bedId = bedId;
    }

    public PauseMode withBedId(String bedId)
    {
        setBedId(bedId);
        return this;
    }

    public String getPauseMode()
    {
        return pauseMode;
    }

    public void setPauseMode(String pauseMode)
    {
        this.pauseMode = pauseMode;
    }

    public PauseMode withPauseMode(String pauseMode)
    {
        setPauseMode(pauseMode);
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
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
        if (!(obj instanceof PauseMode))
        {
            return false;
        }
        PauseMode other = (PauseMode)obj;
        if (accountId == null)
        {
            if (other.accountId != null)
            {
                return false;
            }
        }
        else if (!accountId.equals(other.accountId))
        {
            return false;
        }
        if (bedId == null)
        {
            if (other.bedId != null)
            {
                return false;
            }
        }
        else if (!bedId.equals(other.bedId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PauseMode [accountId=");
        builder.append(accountId);
        builder.append(", bedId=");
        builder.append(bedId);
        builder.append(", pauseMode=");
        builder.append(pauseMode);
        builder.append("]");
        return builder.toString();
    }
}

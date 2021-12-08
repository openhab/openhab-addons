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

public class PauseModeResponse {
    private String accountId;
    private String bedId;
    private String pauseMode;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public PauseModeResponse withAccountId(String accountId) {
        setAccountId(accountId);
        return this;
    }

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public PauseModeResponse withBedId(String bedId) {
        setBedId(bedId);
        return this;
    }

    public String getPauseMode() {
        return pauseMode;
    }

    public void setPauseMode(String pauseMode) {
        this.pauseMode = pauseMode;
    }

    public PauseModeResponse withPauseMode(String pauseMode) {
        setPauseMode(pauseMode);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PauseModeResponse)) {
            return false;
        }
        PauseModeResponse other = (PauseModeResponse) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (bedId == null) {
            if (other.bedId != null) {
                return false;
            }
        } else if (!bedId.equals(other.bedId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
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

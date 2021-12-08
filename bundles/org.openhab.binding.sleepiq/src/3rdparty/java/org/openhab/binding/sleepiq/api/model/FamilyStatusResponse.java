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

import java.util.List;

public class FamilyStatusResponse {
    private List<BedStatus> beds;

    public List<BedStatus> getBeds() {
        return beds;
    }

    public void setBeds(List<BedStatus> beds) {
        this.beds = beds;
    }

    public FamilyStatusResponse withBeds(List<BedStatus> beds) {
        setBeds(beds);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beds == null) ? 0 : beds.hashCode());
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
        if (!(obj instanceof FamilyStatusResponse)) {
            return false;
        }
        FamilyStatusResponse other = (FamilyStatusResponse) obj;
        if (beds == null) {
            if (other.beds != null) {
                return false;
            }
        } else if (!beds.equals(other.beds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FamilyStatus [beds=");
        builder.append(beds);
        builder.append("]");
        return builder.toString();
    }
}

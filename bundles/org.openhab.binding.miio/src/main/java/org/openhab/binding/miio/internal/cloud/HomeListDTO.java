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
package org.openhab.binding.miio.internal.cloud;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the home json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */

public class HomeListDTO {

    @SerializedName("homelist")
    @Expose
    private List<HomeDTO> homelist;
    @SerializedName("has_more")
    @Expose
    private Boolean hasMore;
    @SerializedName("max_id")
    @Expose
    private String maxId;

    public List<HomeDTO> getHomelist() {
        return homelist;
    }

    public void setHomelist(List<HomeDTO> homelist) {
        this.homelist = homelist;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getMaxId() {
        return maxId;
    }

    public void setMaxId(String maxId) {
        this.maxId = maxId;
    }
}

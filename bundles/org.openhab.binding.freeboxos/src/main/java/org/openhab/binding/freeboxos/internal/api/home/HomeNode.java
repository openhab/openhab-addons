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
package org.openhab.binding.freeboxos.internal.api.home;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HomeNode} is a Java class used to map the
 * structure used by the home API
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class HomeNode {
    public class HomeNodesResponse extends Response<List<HomeNode>> {
    }

    public static enum HomeNodeStatus {
        @SerializedName("unreachable")
        UNREACHABLE,
        @SerializedName("disabled")
        DISABLED,
        @SerializedName("active")
        ACTIVE,
        @SerializedName("unpaired")
        UNPAIRED;
    }

    private int id;
    private @Nullable String label;
    private @Nullable String name;
    private String category = "";
    private HomeNodeStatus status = HomeNodeStatus.UNREACHABLE;
    private @Nullable List<HomeNodeEndpoint> showEndpoints;

    public int getId() {
        return id;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public HomeNodeStatus getStatus() {
        return status;
    }

    public List<HomeNodeEndpoint> getShowEndpoints() {
        return showEndpoints != null ? showEndpoints : List.of();
    }
}

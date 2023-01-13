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
package org.openhab.binding.freeboxos.internal.api.home;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.HomeNodeStatus;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link HomeNode} is a Java class used to map the structure used by the home API
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class HomeNode extends Node {
    public class HomeNodesResponse extends Response<List<HomeNode>> {
    }

    private String category = "";
    private HomeNodeStatus status = HomeNodeStatus.UNKNOWN;
    private List<HomeNodeEndpoint> showEndpoints = List.of();

    public String getCategory() {
        return category;
    }

    public HomeNodeStatus getStatus() {
        return status;
    }

    public List<HomeNodeEndpoint> getShowEndpoints() {
        return showEndpoints;
    }
}

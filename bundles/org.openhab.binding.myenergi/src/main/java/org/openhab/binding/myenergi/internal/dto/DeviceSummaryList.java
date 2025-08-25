/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.dto;

import java.util.ArrayList;

/**
 * The {@link DeviceSummaryList} is a DTO class used to represent the highlevel status of all myenergy devices. It's
 * used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class DeviceSummaryList extends ArrayList<DeviceSummary> {

    private static final long serialVersionUID = -1997120707296007505L;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (DeviceSummary sum : this) {
            if (!sum.harvis.isEmpty()) {
                builder.append("[");
                for (HarviSummary c : sum.harvis) {
                    builder.append("(").append(c).append(")");
                }
                builder.append("]");
            }
            if (!sum.zappis.isEmpty()) {
                builder.append("[");
                for (ZappiSummary c : sum.zappis) {
                    builder.append("(").append(c).append(")");
                }
                builder.append("]");
            }
            if (sum.activeServer != null) {
                builder.append(",");
                builder.append(sum.activeServer);
            }
        }
        builder.append("]");
        return builder.toString();
    }
}

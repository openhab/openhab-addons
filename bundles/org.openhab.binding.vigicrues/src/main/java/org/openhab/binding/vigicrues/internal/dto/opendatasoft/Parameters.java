/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal.dto.opendatasoft;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Parameters} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Parameters {
    private String[] dataset = {};
    private String timezone = "";
    private int rows;
    private String format = "";
    private @Nullable Refine refine;
    private String[] facet = {};

    public Optional<String> getDataset() {
        return Arrays.stream(dataset).findFirst();
    }

    public ZoneId getTimezone() {
        return ZoneId.of(timezone);
    }

    public int getRows() {
        return rows;
    }

    public String getFormat() {
        return format;
    }

    public Optional<Refine> getRefine() {
        Refine refine = this.refine;
        if (refine != null) {
            return Optional.of(refine);
        }
        return Optional.empty();
    }

    public String[] getFacets() {
        return facet;
    }
}

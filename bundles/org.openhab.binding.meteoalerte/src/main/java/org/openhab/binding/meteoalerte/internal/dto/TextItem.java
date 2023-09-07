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
package org.openhab.binding.meteoalerte.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TextItem {
    private @Nullable Hazard hazardCode;
    public @Nullable String typeCode;
    public List<TermItem> termItems = List.of();

    public Hazard getHazard() {
        Hazard local = hazardCode;
        return local == null ? Hazard.ALL : local;
    }
}

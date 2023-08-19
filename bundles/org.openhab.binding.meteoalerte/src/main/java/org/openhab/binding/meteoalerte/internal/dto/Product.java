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

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Product {
    public String warningType = "vigilance";
    public String typeCdp = "";
    public String versionVigilance = "";
    public String versionCdp = "";
    @SerializedName("domain_id")
    public Domain domain = Domain.UNKNOWN;
    public @Nullable ZonedDateTime updateTime;
    public List<TextBlocItem> textBlocItems = List.of();
    public List<Period> periods = List.of();
}

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HomeNodeEndpoint} is a Java class used to map the structure used by the home API
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class HomeNodeEndpoint {

    public static enum EpType {
        @SerializedName("signal")
        SIGNAL,
        @SerializedName("slot")
        SLOT;
    }

    public static enum Visibility {
        @SerializedName("internal")
        INTERNAL,
        @SerializedName("normal")
        NORMAL,
        @SerializedName("dashboard")
        DASHBOARD;
    }

    private int id;

    private @Nullable String label;

    private @Nullable String name;

    private @NonNullByDefault({}) EpType epType;
    private @NonNullByDefault({}) Visibility visibility;

    private @Nullable String valueType;

    private int refresh;

    private @Nullable HomeNodeEndpointUi ui;

    public int getId() {
        return id;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable String getName() {
        return name;
    }

    public EpType getEpType() {
        return epType;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public @Nullable String getValueType() {
        return valueType;
    }

    public long getRefresh() {
        return refresh;
    }

    public @Nullable HomeNodeEndpointUi getUi() {
        return ui;
    }
}

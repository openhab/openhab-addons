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
package org.openhab.binding.freeboxos.internal.api.player;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PlayerStatusForegroundApp} is the Java class used to map informations of the player status
 *
 * http://mafreebox.freebox.fr/PlayerStatusInformations
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerStatusForegroundApp {
    private int packageId;
    private @Nullable String curlUrl;
    private @Nullable Object context;

    @SerializedName(value = "package")
    private @Nullable String _package;

    public PlayerStatusForegroundApp(int packageId, String curlUrl, @Nullable Object context, String _package) {
        this.packageId = packageId;
        this.curlUrl = curlUrl;
        this.context = context;
        this._package = _package;
    }

    public int getPackageId() {
        return packageId;
    }

    public @Nullable String getCurlUrl() {
        return curlUrl;
    }

    public @Nullable Object getContext() {
        return context;
    }

    public String getPackage() {
        return Objects.requireNonNull(_package);
    }
}

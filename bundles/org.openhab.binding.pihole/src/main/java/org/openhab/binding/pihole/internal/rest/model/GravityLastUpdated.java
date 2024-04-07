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
package org.openhab.binding.pihole.internal.rest.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class GravityLastUpdated {
    @SerializedName("file_exists")
    @Nullable
    private Boolean fileExists;
    @Nullable
    private Long absolute;
    @Nullable
    private Relative relative;

    public GravityLastUpdated() {
    }

    public GravityLastUpdated(Boolean fileExists, Long absolute, Relative relative) {
        this.fileExists = fileExists;
        this.absolute = absolute;
        this.relative = relative;
    }

    @Nullable
    public Boolean getFileExists() {
        return fileExists;
    }

    public void setFileExists(Boolean fileExists) {
        this.fileExists = fileExists;
    }

    @Nullable
    public Long getAbsolute() {
        return absolute;
    }

    public void setAbsolute(Long absolute) {
        this.absolute = absolute;
    }

    @Nullable
    public Relative getRelative() {
        return relative;
    }

    public void setRelative(Relative relative) {
        this.relative = relative;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GravityLastUpdated that = (GravityLastUpdated) o;

        if (!Objects.equals(fileExists, that.fileExists))
            return false;
        if (!Objects.equals(absolute, that.absolute))
            return false;
        return Objects.equals(relative, that.relative);
    }

    @Override
    public int hashCode() {
        int result = fileExists != null ? fileExists.hashCode() : 0;
        result = 31 * result + (absolute != null ? absolute.hashCode() : 0);
        result = 31 * result + (relative != null ? relative.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GravityLastUpdated{" + //
                "fileExists=" + fileExists + //
                ", absolute=" + absolute + //
                ", relative=" + relative + //
                '}';
    }
}

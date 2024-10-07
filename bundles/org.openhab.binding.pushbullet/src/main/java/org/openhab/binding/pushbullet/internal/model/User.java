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
package org.openhab.binding.pushbullet.internal.model;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the user object received from the API.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class User {

    @SerializedName("iden")
    private @Nullable String iden;

    @SerializedName("active")
    private @Nullable Boolean active;

    @SerializedName("created")
    private @Nullable Instant created;

    @SerializedName("modified")
    private @Nullable Instant modified;

    @SerializedName("email")
    private @Nullable String email;

    @SerializedName("email_normalized")
    private @Nullable String emailNormalized;

    @SerializedName("name")
    private @Nullable String name;

    @SerializedName("image_url")
    private @Nullable String imageUrl;

    @SerializedName("max_upload_size")
    private @Nullable Integer maxUploadSize;

    @SerializedName("referred_count")
    private @Nullable Integer referredCount;

    @SerializedName("referrer_iden")
    private @Nullable String referrerIden;

    public @Nullable String getIden() {
        return iden;
    }

    public @Nullable Boolean getActive() {
        return active;
    }

    public @Nullable Instant getCreated() {
        return created;
    }

    public @Nullable Instant getModified() {
        return modified;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public @Nullable String getEmailNormalized() {
        return emailNormalized;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getImageUrl() {
        return imageUrl;
    }

    public @Nullable Integer getMaxUploadSize() {
        return maxUploadSize;
    }

    public @Nullable Integer getReferredCount() {
        return referredCount;
    }

    public @Nullable String getReferrerIden() {
        return referrerIden;
    }

    @Override
    public String toString() {
        return "User {iden='" + iden + ", active='" + active + "', created='" + created + "', modified='" + modified
                + "', email='" + email + "', emailNormalized='" + emailNormalized + "', name='" + name + "', imageUrl='"
                + imageUrl + "', maxUploadSize='" + maxUploadSize + "', referredCount='" + referredCount
                + "', referrerIden='" + referrerIden + "'}";
    }
}

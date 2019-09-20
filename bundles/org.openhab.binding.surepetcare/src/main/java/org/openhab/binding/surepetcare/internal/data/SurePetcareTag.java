/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcareTag} is the Java class used to represent the micro chip or collar tag of a pet. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareTag extends SurePetcareBaseObject {

    // {
    // "id":34552,
    // "tag":"981.000007623719",
    // "version":"MA==",
    // "created_at":"2019-09-02T09:27:17+00:00",
    // "updated_at":"2019-09-02T09:27:17+00:00",
    // "supported_product_ids":[
    // 3,
    // 4,
    // 6
    // ]
    // }

    private String tag;
    private List<Integer> supportedProductIds = new ArrayList<Integer>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Integer> getSupportedProductIds() {
        return supportedProductIds;
    }

    public void setSupportedProductIds(List<Integer> supportedProductIds) {
        this.supportedProductIds = supportedProductIds;
    }

    public @NonNull ZonedDateTime getCreatedAtAsZonedDateTime() {
        return createdAt.toInstant().atZone(ZoneId.systemDefault());
    }

    public @NonNull ZonedDateTime getUpdatedAtAsZonedDateTime() {
        return updatedAt.toInstant().atZone(ZoneId.systemDefault());
    }
}

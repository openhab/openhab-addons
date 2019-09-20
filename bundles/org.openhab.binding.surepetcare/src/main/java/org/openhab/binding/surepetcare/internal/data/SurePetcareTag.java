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
    private List<Integer> supported_product_ids = new ArrayList<Integer>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Integer> getSupported_product_ids() {
        return supported_product_ids;
    }

    public void setSupported_product_ids(List<Integer> supported_product_ids) {
        this.supported_product_ids = supported_product_ids;
    }

    public @NonNull ZonedDateTime getCreatedAt() {
        return created_at.toInstant().atZone(ZoneId.systemDefault());
    }

    public @NonNull ZonedDateTime getUpdatedAt() {
        return updated_at.toInstant().atZone(ZoneId.systemDefault());
    }
}

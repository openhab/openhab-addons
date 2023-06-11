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
package org.openhab.binding.surepetcare.internal.dto;

import java.util.ArrayList;
import java.util.List;

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

    public String tag;
    public List<Integer> supportedProductIds = new ArrayList<>();
}

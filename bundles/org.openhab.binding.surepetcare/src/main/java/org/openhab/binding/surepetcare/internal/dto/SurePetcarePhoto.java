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
package org.openhab.binding.surepetcare.internal.dto;

/**
 * The {@link SurePetcarePhoto} is the Java class used to represent the photo of a pet or user. It's used to deserialize
 * JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcarePhoto extends SurePetcareBaseObject {

    // {
    // "id":78634,
    // "location":"https:\/\/surehub.s3.amazonaws.com\/user-photos\/thm\/23421\/z70LUtqaHVlAIkgYRDIooi5666GvQwdAZptCgeZU.jpg",
    // "uploading_user_id":34542,
    // "version":"MA==",
    // "created_at":"2019-09-02T09:31:07+00:00",
    // "updated_at":"2019-09-02T09:31:07+00:00"
    // }

    public String location;
    public Long uploadingUserId;
}

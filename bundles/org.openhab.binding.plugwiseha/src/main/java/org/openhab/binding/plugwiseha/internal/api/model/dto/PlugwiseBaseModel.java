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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import java.time.ZonedDateTime;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The {@link PlugwiseBaseModel} abstract class contains
 * methods and properties that similar for all object model classes.
 * 
 * @author B. van Wetten - Initial contribution
 */
public abstract class PlugwiseBaseModel {

    private String id;

    @XStreamAlias("created_date")
    private ZonedDateTime createdDate;

    @XStreamAlias("modified_date")
    private ZonedDateTime modifiedDate;

    @XStreamAlias("updated_date")
    private ZonedDateTime updateDate;

    @XStreamAlias("deleted_date")
    private ZonedDateTime deletedDate;

    public String getId() {
        return id;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public ZonedDateTime getModifiedDate() {
        return modifiedDate;
    }

    public ZonedDateTime getUpdatedDate() {
        return updateDate;
    }

    public ZonedDateTime getDeletedDate() {
        return deletedDate;
    }
}

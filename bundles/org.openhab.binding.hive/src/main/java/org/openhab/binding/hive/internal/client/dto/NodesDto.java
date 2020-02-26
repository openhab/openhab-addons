/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A model of a "NodeRequestEntity"/"NodeResponseEntity"
 *
 * Based on the Hive API Swagger model.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class NodesDto {
    /*
     * linked (object, optional): Linked entities grouped by entity type. Used when side-loading entities
     */

    /*
     * links (inline_model_34, optional): URL Templates for links to other entities e.g.: "users.nodes": "https://api.example.com/nodes/{users.nodes}" ,
     */

    /*
     * meta (object, optional): Meta information about this entity
     */

    /**
     * sessions (Array[Session], optional): List of sessions
     */
    public @Nullable List<@Nullable NodeDto> nodes;
}

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
package org.openhab.binding.webthing.internal.client.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The Web Thing Description Property object. Refer https://iot.mozilla.org/wot/#property-object
 *
 * @author Gregor Roth - Initial contribution
 */
public class Property {

    public String title = "";

    @SerializedName("@type")
    public String typeKeyword = "";

    public String type = "string";

    public String unit = null;

    public boolean readOnly = false;

    public String description = "";

    public List<Link> links = List.of();
}

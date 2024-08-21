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
package org.openhab.io.hueemulation.internal.dto.response;

/**
 * This object describes the right hand side of "success".
 * The response looks like this:
 *
 * <pre>
 * {
 *   "success":{
 *      "id": "-the-id-"
 *   }
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
public class HueSuccessCreateGroup extends HueSuccessResponse {
    public String id;

    public HueSuccessCreateGroup(String id) {
        this.id = id;
    }
}

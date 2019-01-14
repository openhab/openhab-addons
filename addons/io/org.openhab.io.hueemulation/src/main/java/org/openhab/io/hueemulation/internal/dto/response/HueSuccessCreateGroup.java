/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class HueSuccessCreateGroup implements HueSuccessResponse {
    public int id;

    public HueSuccessCreateGroup(int id) {
        this.id = id;
    }
}

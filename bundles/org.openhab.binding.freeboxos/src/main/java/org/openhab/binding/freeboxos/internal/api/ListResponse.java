/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines an API result that returns a list of objects
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListResponse<T> extends BaseResponse {
    protected List<T> result = new ArrayList<>();

    public List<T> getResult() {
        return result;
    }
}

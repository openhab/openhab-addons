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
package org.openhab.binding.dbquery.action;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines rule actions for interacting with DBQuery addon Things.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface IDBQueryActions {
    ActionQueryResult executeQuery(String query, Map<String, @Nullable Object> parameters, int timeoutInSeconds);

    ActionQueryResult getLastQueryResult();

    void setQueryParameters(Map<String, @Nullable Object> parameters);
}

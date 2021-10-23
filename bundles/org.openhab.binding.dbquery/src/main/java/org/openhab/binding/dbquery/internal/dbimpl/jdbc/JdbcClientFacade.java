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
package org.openhab.binding.dbquery.internal.dbimpl.jdbc;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.dbimpl.jdbc.JdbcQueryFactory.JdbcQuery;

/**
 * Facade to Jdbc connection to facilitate testing
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface JdbcClientFacade {
    boolean connect();

    boolean isConnected();

    boolean disconnect();

    List<Map<String, @Nullable Object>> query(JdbcQuery query);
}

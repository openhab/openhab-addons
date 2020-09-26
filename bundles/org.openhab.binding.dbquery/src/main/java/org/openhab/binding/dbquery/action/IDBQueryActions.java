/**
 * Copyright (c) 2020-2020 Contributors to the openHAB project
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

/**
 * The {@link IDBQueryActions} interface defines rule actions for interacting with DBQuery addon Things.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface IDBQueryActions {
    String executeQuery(String query, Map<String, Object> parameters);

    QueryResult executeQueryNonScalar(String query, Map<String, Object> parameters);

    void setQueryParameters(Map<String, Object> parameters);

    class QueryResult {
        private boolean correct;

        public QueryResult(boolean correct) {
            this.correct = correct;
        }

        public boolean isCorrect() {
            return correct;
        }
    }
}

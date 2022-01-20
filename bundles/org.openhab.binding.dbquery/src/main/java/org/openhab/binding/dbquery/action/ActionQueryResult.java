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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Query result as it's exposed to users in thing actions
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class ActionQueryResult {
    private final boolean correct;
    private List<Map<String, @Nullable Object>> data = Collections.emptyList();

    public ActionQueryResult(boolean correct, @Nullable List<Map<String, @Nullable Object>> data) {
        this.correct = correct;
        if (data != null) {
            this.data = data;
        }
    }

    public boolean isCorrect() {
        return correct;
    }

    public List<Map<String, @Nullable Object>> getData() {
        return data;
    }

    public @Nullable Object getResultAsScalar() {
        var firstResult = data.get(0);
        return isScalarResult() ? firstResult.get(firstResult.keySet().iterator().next()) : null;
    }

    public boolean isScalarResult() {
        return data.size() == 1 && data.get(0).keySet().size() == 1;
    }
}

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dbquery.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A query result value as is extracted by {@link QueryResultExtractor} from a {@link QueryResult}
 * to be set in channels
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class ResultValue {
    private final boolean correct;
    private final @Nullable Object result;

    private ResultValue(boolean correct, @Nullable Object result) {
        this.correct = correct;
        this.result = result;
    }

    public static ResultValue of(@Nullable Object result) {
        return new ResultValue(true, result);
    }

    public static ResultValue incorrect() {
        return new ResultValue(false, null);
    }

    public boolean isCorrect() {
        return correct;
    }

    public @Nullable Object getResult() {
        return result;
    }
}

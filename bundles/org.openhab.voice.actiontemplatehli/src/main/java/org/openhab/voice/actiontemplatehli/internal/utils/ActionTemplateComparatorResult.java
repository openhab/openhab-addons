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
package org.openhab.voice.actiontemplatehli.internal.utils;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import opennlp.tools.util.Span;

/**
 * The {@link ActionTemplateComparatorResult} class represents each configured action
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateComparatorResult {
    public static final ActionTemplateComparatorResult ZERO = new ActionTemplateComparatorResult(0, null);
    public final double score;
    public final @Nullable Span dynamicSpan;

    protected ActionTemplateComparatorResult(double score, @Nullable Span dynamicSpan) {
        this.score = score;
        this.dynamicSpan = dynamicSpan;
    }

    public boolean equals(ActionTemplateComparatorResult other) {
        return score == other.score && Objects.equals(dynamicSpan, other.dynamicSpan);
    }
}

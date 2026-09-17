/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.yamlcomposer.internal.placeholders.DefaultPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ElseIfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ElsePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ForPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.FreezePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.VarPlaceholder;

/**
 * Defines the different sets of processing phases for the YAML composer.
 *
 * @author Jimmy Tanagra - Initial Contribution
 */
@NonNullByDefault
public enum ProcessingPhase {

    SUBSTITUTION(Set.of( //
            SubstitutionPlaceholder.class //
    )),

    INCLUDES(Set.of( //
            IncludePlaceholder.class //
    )),

    DIRECTIVES(Set.of( //
            IfPlaceholder.class, //
            ElseIfPlaceholder.class, //
            ElsePlaceholder.class, //
            ForPlaceholder.class, //
            VarPlaceholder.class //
    )),

    DIRECTIVES_WITH_SUBSTITUTIONS(combine( //
            DIRECTIVES, //
            SubstitutionPlaceholder.class //
    )),

    STANDARD(combine(DIRECTIVES_WITH_SUBSTITUTIONS, //
            IncludePlaceholder.class, //
            InsertPlaceholder.class //
    )),

    MERGE(STANDARD.getPlaceholders()),

    FINALIZATION(Set.of( //
            DefaultPlaceholder.class, //
            RemovePlaceholder.class, //
            FreezePlaceholder.class //
    )),

    NONE(Set.of()),

    ALL(combine(SUBSTITUTION, INCLUDES, DIRECTIVES, STANDARD, FINALIZATION));

    private final Set<Class<? extends Placeholder>> placeholders;

    ProcessingPhase(Set<Class<? extends Placeholder>> placeholders) {
        this.placeholders = placeholders;
    }

    public Set<Class<? extends Placeholder>> getPlaceholders() {
        return placeholders;
    }

    public boolean includes(Class<? extends Placeholder> placeholderType) {
        return placeholders.contains(placeholderType);
    }

    public boolean includes(Placeholder placeholder) {
        return includes(placeholder.getClass());
    }

    @SafeVarargs
    private static Set<Class<? extends Placeholder>> combine(ProcessingPhase basePhase,
            Class<? extends Placeholder>... additions) {
        Set<Class<? extends Placeholder>> result = new HashSet<>(basePhase.getPlaceholders().size() + additions.length);
        result.addAll(basePhase.getPlaceholders());
        Collections.addAll(result, additions);
        return Set.copyOf(result);
    }

    private static Set<Class<? extends Placeholder>> combine(ProcessingPhase... phases) {
        Set<Class<? extends Placeholder>> result = new HashSet<>();
        for (ProcessingPhase phase : phases) {
            result.addAll(phase.getPlaceholders());
        }
        return Set.copyOf(result);
    }
}

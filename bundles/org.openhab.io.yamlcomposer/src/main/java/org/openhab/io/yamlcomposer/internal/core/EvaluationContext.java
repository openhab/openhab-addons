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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Transient state associated with one recursive transformation pass.
 * <p>
 * Holds the live hierarchical variable scope, active processing phase, graph node visitor map
 * for cycle detection, template nesting depth, and static runtime service callbacks for
 * environment variable tracking and dynamic registry lookups.
 *
 * @author Jimmy Tanagra - Initial Contribution
 */
@NonNullByDefault
public record EvaluationContext( //
        Scope scope, //
        ProcessingPhase activePhase, //
        IdentityHashMap<Object, Object> visited, //
        int templateDepth, //
        Consumer<String> envVarCallback, //
        Function<String, @Nullable Map<String, Map<String, @Nullable Object>>> sourceResolver) {

    /** Primary constructor for starting a top-level transformation pass. */
    public EvaluationContext( //
            Consumer<String> envVarCallback, //
            Function<String, @Nullable Map<String, Map<String, @Nullable Object>>> sourceResolver) {
        this(new Scope(), ProcessingPhase.STANDARD, new IdentityHashMap<>(), 0, envVarCallback, sourceResolver);
    }

    /** Creates a child context with an updated scope. */
    public EvaluationContext withScope(Scope newScope) {
        return new EvaluationContext(newScope, activePhase, visited, templateDepth, envVarCallback, sourceResolver);
    }

    /** Creates a child context with an updated processing phase. */
    public EvaluationContext withProcessingPhase(ProcessingPhase newActivePhase) {
        return new EvaluationContext(scope, newActivePhase, visited, templateDepth, envVarCallback, sourceResolver);
    }

    /** Creates an iteration context without reusing container results from a prior iteration. */
    public EvaluationContext forIteration(Scope iterationScope) {
        return new EvaluationContext(iterationScope, activePhase, new IdentityHashMap<>(visited), templateDepth,
                envVarCallback, sourceResolver);
    }

    /** Creates a child transformation context that does not reuse container results from its caller. */
    public EvaluationContext forFragment(Scope fragmentScope) {
        return new EvaluationContext(fragmentScope, activePhase, new IdentityHashMap<>(), templateDepth + 1,
                envVarCallback, sourceResolver);
    }
}

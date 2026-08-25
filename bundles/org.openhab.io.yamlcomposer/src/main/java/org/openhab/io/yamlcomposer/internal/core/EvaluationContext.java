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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transient state associated with one recursive transformation.
 *
 * @author Jimmy Tanagra - Initial Contribution
 */
@NonNullByDefault
public record EvaluationContext(Scope scope, ProcessingPhase activePhase, IdentityHashMap<Object, Object> visited,
        int templateDepth) {

    public EvaluationContext(Scope scope, ProcessingPhase activePhase) {
        this(scope, activePhase, new IdentityHashMap<>(), 0);
    }

    public EvaluationContext withScope(Scope newScope) {
        return new EvaluationContext(newScope, activePhase, visited, templateDepth);
    }

    public EvaluationContext withProcessingPhase(ProcessingPhase newActivePhase) {
        return new EvaluationContext(scope, newActivePhase, visited, templateDepth);
    }

    /** Creates an iteration context without reusing container results from a prior iteration. */
    public EvaluationContext forIteration(Scope iterationScope) {
        return new EvaluationContext(iterationScope, activePhase, new IdentityHashMap<>(visited), templateDepth);
    }

    /** Creates a child transformation context that does not reuse container results from its caller. */
    public EvaluationContext forFragment(Scope fragmentScope) {
        return new EvaluationContext(fragmentScope, activePhase, new IdentityHashMap<>(), templateDepth + 1);
    }
}

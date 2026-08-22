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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.yamlcomposer.internal.placeholders.ElseIfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ElsePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ForPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ReplacePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.VarPlaceholder;

/**
 * Defines the different processing phases for the YAML composer.
 *
 * @author Jimmy Tanagra - Initial Contribution
 */
@NonNullByDefault
public class ProcessingPhase {
    public static final Set<Class<? extends Placeholder>> SUBSTITUTION = //
            Set.of(SubstitutionPlaceholder.class);
    public static final Set<Class<? extends Placeholder>> INCLUDES = //
            Set.of(IncludePlaceholder.class);
    public static final Set<Class<? extends Placeholder>> DIRECTIVES = //
            Set.of(SubstitutionPlaceholder.class, IfPlaceholder.class, ElseIfPlaceholder.class, ElsePlaceholder.class,
                    ForPlaceholder.class, VarPlaceholder.class);
    public static final Set<Class<? extends Placeholder>> STANDARD = //
            Set.of(SubstitutionPlaceholder.class, IfPlaceholder.class, ElseIfPlaceholder.class, ElsePlaceholder.class,
                    ForPlaceholder.class, VarPlaceholder.class, IncludePlaceholder.class, InsertPlaceholder.class);
    public static final Set<Class<? extends Placeholder>> PACKAGE_OVERRIDES = //
            Set.of(RemovePlaceholder.class, ReplacePlaceholder.class);

    private ProcessingPhase() {
    }
}

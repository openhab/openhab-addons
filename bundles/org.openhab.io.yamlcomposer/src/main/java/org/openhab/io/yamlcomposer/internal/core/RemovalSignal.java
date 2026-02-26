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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A signal returned by {@link org.openhab.io.yamlcomposer.internal.processors.PlaceholderProcessor}s to indicate that
 * the entry
 * currently being processed should be <strong>intentionally removed</strong> from its
 * parent container (Map or List).
 * <p>
 * This enum provides an <em>explicit</em> way to signal removal, distinguishing it from
 * error-based removal that occurs when a processor returns {@code null}.
 *
 * <h2>Usage Guidelines</h2>
 * <p>
 * <strong>Use {@code RemovalSignal.REMOVE} when:</strong>
 * <ul>
 * <li>The placeholder's purpose is to explicitly remove an entry (e.g., {@code !remove} directive)</li>
 * <li>The removal is intentional and by design, not due to an error condition</li>
 * <li>You want to make the removal intent clear in the code</li>
 * </ul>
 *
 * <strong>Use {@code null} when:</strong>
 * <ul>
 * <li>Processing failed due to an error (missing parameter, invalid input, resource not found)</li>
 * <li>You've logged a warning describing the error before returning {@code null}</li>
 * <li>The entry should be removed as error recovery, not as intended functionality</li>
 * </ul>
 *
 * <h2>Technical Details</h2>
 * <p>
 * The {@link RecursiveTransformer} treats both {@code RemovalSignal.REMOVE} and {@code null}
 * identically in terms of behavior (both cause entry removal), but the semantic distinction
 * helps developers understand whether removal was intentional or due to error recovery.
 *
 * @author Jimmy Tanagra - Initial contribution
 * @see org.openhab.io.yamlcomposer.internal.processors.PlaceholderProcessor#process
 */
@NonNullByDefault
public enum RemovalSignal {
    /**
     * Signals that the current entry should be intentionally removed from its parent container.
     * <p>
     * This is the recommended way to explicitly indicate removal as part of normal processing,
     * as opposed to returning {@code null} which should be reserved for error cases.
     */
    REMOVE;
}

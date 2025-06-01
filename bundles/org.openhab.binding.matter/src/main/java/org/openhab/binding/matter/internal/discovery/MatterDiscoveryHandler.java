/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.discovery;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;

/**
 * The {@link MatterDiscoveryHandler} is the interface for the discovery handler.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface MatterDiscoveryHandler {
    /**
     * Sets a {@link MatterDiscoveryService} to call when device information is received
     *
     */
    public void setDiscoveryService(MatterDiscoveryService service);

    public CompletableFuture<Void> startScan(@Nullable String code);

    public LocaleProvider getLocaleProvider();

    public TranslationProvider getTranslationProvider();
}

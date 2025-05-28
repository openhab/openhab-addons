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
package org.openhab.binding.sedif.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Contract} holds Contract information
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Contract {
    public String AutoriteOrganisatrice;
    public String DateSortieEPT;
    public boolean eFacture;
    public boolean iclActive;
    public @Nullable String Id;
    public @Nullable String Name;
    public boolean prelevAuto;
    public @Nullable String SITE_Commune;
    public @Nullable String SITE_CP;
    public @Nullable String SITE_Rue;
    public @Nullable String Statut;
}

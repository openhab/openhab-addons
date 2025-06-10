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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Contract} holds Contract information
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Contract {
    public String autoriteOrganisatrice;
    public String dateSortieEPT;
    public boolean eFacture;
    public boolean iclActive;
    public @Nullable String id;
    public @Nullable String name;
    public boolean prelevAuto;
    @SerializedName("SITE_Commune")
    public @Nullable String siteCommune;
    @SerializedName("SITE_CP")
    public @Nullable String siteCp;
    @SerializedName("SITE_Rue")
    public @Nullable String siteRue;
    public @Nullable String statut;
}

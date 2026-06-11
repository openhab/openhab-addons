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
package org.openhab.binding.atmofrance.internal.api.dto;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Taxon {
    ALDER, // aulne
    BIRCH, // bouleau
    OLIVE, // olivier
    GRASSES, // graminees
    WORMWOOD, // armoises
    RAGWEED; // ambroisies

    /*
     * Not available in API as of today
     * CYPRESS, // cypres
     * HAZEL, // noisetier
     * POPLAR, // peuplier
     * WILLOW, // saule
     * ASH, // frene
     * HORNBEAM, // charme
     * PLANE, // platane
     * OAK, // chene
     * LINDEN, // tilleul
     * CHESTNUT, // chataignier
     * RUMEX, // rumex
     * PLANTAIN, // plantain
     * URTICACEAE; // urticacees
     */

    public static final EnumSet<Taxon> AS_SET = EnumSet.allOf(Taxon.class);
}

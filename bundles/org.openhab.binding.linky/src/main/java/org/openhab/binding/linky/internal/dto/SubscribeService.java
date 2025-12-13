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
package org.openhab.binding.linky.internal.dto;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Laurent Arnal - Initial contribution
 */

public class SubscribeService {
    public String id;
    public boolean injection;
    public boolean soutirage;
    public String serviceCode;
    // public LocalDateTime dateDebut;
    // public LocalDateTime dateFin;
    public String etateCode;
    public String etatLibelle;
    public String pointId;
    public String sirenBeneficiaire;
    public String mesuresTypeCode;
    public String mesuresPas;
    public boolean publicationDonnees;
    public Authorisation authorisation;

    public class Authorisation {
        public String autorisationId;
        public String autorisationLibelle;
        public String autorisationType;
        public String autorisationStatut;
    }

}

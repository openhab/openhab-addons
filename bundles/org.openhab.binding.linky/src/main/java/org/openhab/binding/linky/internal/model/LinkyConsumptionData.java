/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link LinkyConsumptionData} is responsible for holding values
 * returned by API calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class LinkyConsumptionData {
    private Etat etat;
    private Graphe graphe;

    public Etat getEtat() {
        return etat;
    }

    public boolean isInactive() {
        return "nonActive".equalsIgnoreCase(etat.valeur);
    }

    public boolean success() {
        return "termine".equalsIgnoreCase(etat.valeur);
    }

    public List<Data> getData() {
        return graphe.data;
    }

    public int getDecalage() {
        return graphe.decalage;
    }

    private static class Etat {
        public String valeur;
    }

    public static class Graphe {
        public int puissanceSouscrite;
        public int decalage;
        public Periode periode;
        public List<Data> data = new ArrayList<>();
    }

    private static class Periode {
        public String dateDebut;
        public String dateFin;
    }

    public static class Data {
        public double valeur;
        public int ordre;

        public boolean isPositive() {
            return valeur > 0;
        }
    }
}

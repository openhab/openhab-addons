/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader;

/**
 * The {@link FrameOptionTempo} class defines a Teleinfo frame with Tempo option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameOptionTempo extends Frame {

    private static final long serialVersionUID = 6423861816467362730L;

    private Integer indexHeuresCreusesJoursBleus; // BBR HC JB : Index heures creuses jours bleus si option = tempo (en
                                                  // Wh)

    private Integer indexHeuresPleinesJoursBleus; // BBR HP JB : Index heures pleines jours bleus si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresCreusesJoursBlancs;// BBR HC JW : Index heures creuses jours blancs si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresPleinesJoursBlancs;// BBR HC JW : Index heures pleines jours blancs si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresCreusesJoursRouges;// BBR HC JR : Index heures creuses jours rouges si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresPleinesJoursRouges;// BBR HP JR : Index heures pleines jours rouges si option = tempo (en
                                                  // Wh)
    private String couleurLendemain; // DEMAIN : Couleur du lendemain si option = tempo
    private String groupeHoraire; // HHPHC : Groupe horaire si option = heures creuses ou tempo

    public FrameOptionTempo() {
        // default constructor
    }

}

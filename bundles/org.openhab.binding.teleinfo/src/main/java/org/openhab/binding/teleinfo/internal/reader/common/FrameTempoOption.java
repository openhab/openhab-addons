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
package org.openhab.binding.teleinfo.internal.reader.common;

/**
 * The {@link FrameTempoOption} interface defines common attributes for Tempo option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public interface FrameTempoOption {

    public static enum CouleurDemain {
        Bleu,
        Blanc,
        Rouge
    }

    public static enum ProgrammeCircuit1 {
        A,
        B,
        C
    }

    public static enum ProgrammeCircuit2 {
        P0,
        P1,
        P2,
        P3,
        P4,
        P5,
        P6,
        P7
    }

    int getBbrhpjr();

    void setBbrhpjr(int bbrhpjr);

    int getBbrhcjr();

    void setBbrhcjr(int bbrhcjr);

    int getBbrhpjw();

    void setBbrhpjw(int bbrhpjw);

    int getBbrhcjw();

    void setBbrhcjw(int bbrhcjw);

    int getBbrhpjb();

    void setBbrhpjb(int bbrhpjb);

    int getBbrhcjb();

    void setBbrhcjb(int bbrhcjb);

    CouleurDemain getDemain();

    void setDemain(CouleurDemain couleurDemain);

    Hhphc getHhphc();

    void setHhphc(Hhphc hhphc);

    ProgrammeCircuit1 getProgrammeCircuit1();

    void setProgrammeCircuit1(ProgrammeCircuit1 programmeCircuit1);

    ProgrammeCircuit2 getProgrammeCircuit2();

    void setProgrammeCircuit2(ProgrammeCircuit2 programmeCircuit2);
}

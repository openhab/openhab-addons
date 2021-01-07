/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import org.openhab.binding.teleinfo.internal.dto.common.Hhphc;
import org.openhab.binding.teleinfo.internal.dto.common.Ptec;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption.CouleurDemain;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption.ProgrammeCircuit1;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption.ProgrammeCircuit2;
import org.openhab.binding.teleinfo.internal.dto.common.FrameType;

/**
 * The {@link Frame} class defines common attributes for any Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class Frame implements Serializable {

    private static final long serialVersionUID = -1934715078822532494L;

    private UUID id;
    private LocalDate timestamp; // UTC timestamp
    private FrameType type;
    
    private String adco;
    
    // cbemm
    private int isousc;
    private int iinst; // ampères
    private Integer adps; // ampères
    private Integer imax; // ampères
    private Ptec ptec;
    private String motdetat;
    
    // base
    private int base;
    
    // ejp
    private int ejphpm;
    private int ejphn;
    private Integer pejp;
    
    // hc
    private int hchc;
    private int hchp;
    private Hhphc hhphc;
    
    // tempo
    private int bbrhpjr;
    private int bbrhcjr;
    private int bbrhpjw;
    private int bbrhcjw;
    private int bbrhpjb;
    private int bbrhcjb;
    private CouleurDemain demain;
    //private Hhphc hhphc;
    private ProgrammeCircuit1 programmeCircuit1;
    private ProgrammeCircuit2 programmeCircuit2;
    
    // icc
    private int papp; // Volt.ampères
    
    // cbetm
    private int iinst1; // ampères
    private int iinst2; // ampères
    private int iinst3; // ampères
    
    // long
    //private int isousc;
    private Integer imax1; // ampères
    private Integer imax2; // ampères
    private Integer imax3; // ampères
    //private Ptec ptec;
    private int pmax; // W
    //private int papp; // Volt.ampères
    //private String motdetat;
    private String ppot;
    
    // short
    private Integer adir1; // ampères
    private Integer adir2; // ampères
    private Integer adir3; // ampères
    
    public Frame() {
        // default constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }
}

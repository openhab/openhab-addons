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
package org.openhab.binding.linky.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Define the evolution option values
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public enum Compteur {

    NotAttrib000(0x00, CompteurType.NOATTRIB, "Non attribué"),

    BleuMonoMultiTarif(0x01, CompteurType.BLEUE, "Compteur bleu monophasé multitarif (BBR) - 1ère gen"),

    CentraleG3(0x02, CompteurType.OTHER, "Centrale de mesure G3 - Poste HTA/BT"),

    Concentrateur01(0x03, CompteurType.OTHER, "Concentrateur multi-compteurs / électrique + 2 fluides"),
    Concentrateur02(0x04, CompteurType.OTHER, "Concentrateur simplifié / élec"),

    BleuMonoSimpleTarif(0x05, CompteurType.BLEUE, "Compteur bleu monophasé simple tarif électronique - 1ère gen"),

    JauneElectro(0x06, CompteurType.JAUNE, "Compteur jaune électronique / tarif modulable"),

    PrismeElectro(0x07, CompteurType.PRISME, "Compteur électronique universel (PRISME ou ICE)"),
    SauterEuridis(0x08, CompteurType.OTHER, "Compteur sauter modifié EURIDIS"),

    BleuTri1Gen(0x09, CompteurType.BLEUE, "Compteur bleu triphasé électronique - 1ère gen"),

    JauneElectro2Gen(0x0A, CompteurType.JAUNE, "Compteur jaune électronique 2ème gen"),

    BleuTriSimpleTarif(0x0B, CompteurType.BLEUE, "Compteur bleu monophasé simple tarif FERRARIS"),

    Prisme(0x0C, CompteurType.PRISME, "Compteur prisme"),

    CentraleMesureF1(0x0D, CompteurType.OTHER, "Centrale de mesure G1 - Poste HTA/BT"),
    AnalyserCourbeCharge(0x0E, CompteurType.OTHER, "Analyseur de courbe de charge (panel BT)"),

    BleuMonoMultiTarifSansBBR(0x0F, CompteurType.BLEUE, "Compteur bleu monophasé multitarif électronique sans BBR"),
    BleuExpICC(0x10, CompteurType.BLEUE, "Compteur bleu expérimentation « 10000 ICC »"),

    ExpICC(0x11, CompteurType.OTHER, "ICC expérimentation « 10000 ICC »"),

    DetectDefHTA(0x12, CompteurType.OTHER, "Détecteur de défauts / HTA , neutre compensé"),
    Concentrateur03(0x13, CompteurType.OTHER, "Concentrateur multi-compteurs / 3 fluides indifférenciés"),

    BleuMonoMultiTarifDemiTaux1Gen(0x14, CompteurType.BLEUE, "Compteur bleu monophasé multitarif 1/2 - 1ère gen"),
    BleuTriMultiTarifDemiTaux1Gen(0x15, CompteurType.BLEUE, "Compteur bleu triphasé 1/2 taux - 1ére gen"),
    BleuMonoMultiTarif2Gen(0x16, CompteurType.BLEUE, "Compteur bleu monophasé multitarif - 2ème gen"),
    BleuMonoMultiTarifDemiTaux2Gen(0x17, CompteurType.BLEUE, "Compteur bleu monophasé multitarif 1/2 - 2ème gen"),

    NotAttrib024(0x18, CompteurType.NOATTRIB, "Non attribué"),

    BleuMonoSimpleTarif2Gen(0x19, CompteurType.BLEUE, "Compteur bleu monophasé simple tarif - 2ème gen"),
    BleuTri20002Gen(0x1A, CompteurType.BLEUE, "Compteur bleu triphasé - palier 2000 - 2ème gen"),
    BleuTri2000DemiTaux2Gen(0x1B, CompteurType.BLEUE, "Compteur bleu triphasé - palier 2000 ½ taux - 2ème gen"),
    BleuMono20073Gen(0x1C, CompteurType.BLEUE, "Compteur bleu monophasé multitarif - palier 2007 - 3ème gen"),
    BleuMono2007DemiTaux3Gen(0x1D, CompteurType.BLEUE, "Compteur bleu monophasé multi ½ - 2007 - 3ème gen"),
    BleuTri20073Gen(0x1E, CompteurType.BLEUE, "Compteur bleu triphasé - palier 2007 - 3ème gen"),
    BleuTri2007DemiTaux3Gen(0x1F, CompteurType.BLEUE, "Compteur bleu triphasé ½ taux - palier 2007 – 3ème gen"),
    BleuTriTele(0x20, CompteurType.BLEUE, "Compteur bleu triphasé télétotalisation"),

    JauneElectroDirect(0x21, CompteurType.JAUNE, "Compteur jaune électronique branchement direct"),

    Ice(0x22, CompteurType.OTHER, "Compteur ICE 4 quadrants"),
    Tromaran(0x23, CompteurType.OTHER, "Compteur trimaran 2P classe 0,2s pour RTE"),
    PmePmiBt(0x24, CompteurType.OTHER, "Compteur PME-PMI BT > 36kva"),
    Prepaie(0x25, CompteurType.OTHER, "Compteur prépaiement"),
    Hxe34(0x26, CompteurType.OTHER, "Compteur triphasé HXE34 de HECL"),

    NotAttrib039(0x27, CompteurType.NOATTRIB, "Non attribué"),

    AffMulti(0x28, CompteurType.OTHER, "Système d'affichage multiusage (SAM)"),

    NotAttrib041(0x29, CompteurType.NOATTRIB, "Non attribué"),

    ActarisMono1(0x2A, CompteurType.OTHER, "Compteur monophasé export (ACTARIS)"),
    ActarisMono2(0x2B, CompteurType.OTHER, "Compteur monophasé export (ACTARIS)"),
    ActarisTri1(0x2C, CompteurType.OTHER, "Compteur triphasé export ACTARIS"),
    ActarisTri2(0x2D, CompteurType.OTHER, "Compteur triphasé export ACTARIS"),
    ModemEuredis(0x2E, CompteurType.OTHER, "Modem EURIDIS pour compteur PME-PMI"),

    NotAttrib047(0x2F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib048(0x30, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib049(0x31, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib050(0x32, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib051(0x33, CompteurType.NOATTRIB, "Non attribué"),

    Concentrateur04(0x34, CompteurType.OTHER, "Concentrateur simplifié / gaz ou Transpondeur Gaz EURIDIS"),
    Concentrateur05(0x35, CompteurType.OTHER, "Concentrateur multi-compteurs / VGR"),
    Concentrateur06(0x36, CompteurType.OTHER, "Concentrateur multi-compteurs / gaz"),

    NotAttrib055(0x37, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib056(0x38, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib057(0x39, CompteurType.NOATTRIB, "Non attribué"),

    PrismeBaie(0x3A, CompteurType.OTHER, "Baie prisme de télétotalisation, exp Lyon"),

    NotAttrib059(0x3B, CompteurType.NOATTRIB, "Non attribué"),

    LinkyMono60AG1(0x3C, CompteurType.LINKY, "Compteur monophasé 60A LINKY - généralisation G1 - arrivée basse"),
    LinkyMono60AG3(0x3D, CompteurType.LINKY, "Compteur monophasé 60A LINKY - généralisation G3 - arrivée haute"),
    LinkyMono90AG1(0x3E, CompteurType.LINKY, "Compteur monophasé 90A LINKY - généralisation G1 - arrivée basse"),
    LinkyTri60AG1(0x3F, CompteurType.LINKY, "Compteur triphasé 60A LINKY - généralisation G1 - arrivée basse"),
    LinkyTri60AG3(0x40, CompteurType.LINKY, "Compteur monophasé 60A LINKY - généralisation G3 - arrivée basse"),
    LinkyMono60AG3Exp(0x41, CompteurType.LINKY, "Compteur monophasé 90A LINKY expérimentation CPL G3 (2000 ex.)"),

    ModulaireGen(0x42, CompteurType.OTHER, "Module du compteur modulaire généralisation"),

    LinkyMono90AG1Pil(0x43, CompteurType.LINKY, "Compteur monophasé 90A LINKY - pilote G1 - arrivée basse"),
    LinkyTri60AG1Pil(0x44, CompteurType.LINKY, "Compteur triphasé 60A LINKY - pilote G1 - arrivée basse"),

    NotAttrib069(0x45, CompteurType.NOATTRIB, "Non attribué"),

    LinkyMono60AG3I(0x46, CompteurType.LINKY, "Compteur monophasé 60A LINKY - interopérabilité G3 - arrivée basse"),
    LinkyTru60AG3I(0x47, CompteurType.LINKY, "Compteur triphasé 60A LINKY - interopérabilité G3 - arrivée basse"),

    HXE12K(0x48, CompteurType.OTHER, "Compteur monophasé HXE12K 10-80A 4 tarifs (Hexing Electrical co,Ltd)"),

    NotAttrib073(0x49, CompteurType.NOATTRIB, "Non attribué"),

    HXE34K(0x4A, CompteurType.OTHER, "Compteur triphasé HXE34K 230/400V 10-80A"),

    LinkyMono90AG3P(0x4B, CompteurType.LINKY, "Compteur monophasé 90A LINKY - palier 1 G3 - arrivée basse"),
    LinkyTru60AG3P(0x4C, CompteurType.LINKY, "Compteur triphasé 60A LINKY - palier 1 G3 - arrivée basse"),

    NotAttrib077(0x4D, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib078(0x4E, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib079(0x4F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib080(0x50, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib081(0x51, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib082(0x52, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib083(0x53, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib084(0x54, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib085(0x55, CompteurType.NOATTRIB, "Non attribué"),

    SeiMono(0x56, CompteurType.OTHER, "Compteur numérique SEI monophasé 60A 230V - G3 - arrivée basse - 60Hz"),
    SeiTri(0x57, CompteurType.OTHER, "Compteur numérique SEI triphasé 60A 230/400V - G3 - 60Hz"),
    PlcMono(0x58, CompteurType.OTHER, "Compteur monophasé PLC DSMR2.2 (Actaris)"),
    PlcTri(0x59, CompteurType.OTHER, "Compteur triphasé PLC DSMR2.2 (Actaris)"),
    CplGen1(0x5A, CompteurType.OTHER, "Compteur monophasé CPL intégré 1ère gen"),
    CplGen2(0x5B, CompteurType.OTHER, "Compteur triphasé CPL intégré 2ème gen"),

    LinkyMono90A(0x5C, CompteurType.LINKY, "Compteur monophasé 90A LINKY ORES – G3 Palier 1"),
    LinkyTri60A3F(0x5D, CompteurType.LINKY, "Compteur triphasé 60A 3 fils LINKY ORES – G3 Palier 1"),
    LinkyTri60A4F(0x5E, CompteurType.LINKY, "Compteur triphasé 60A 4 fils LINKY ORES – G3 Palier 1"),

    NotAttrib095(0x5F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib096(0x60, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib097(0x61, CompteurType.NOATTRIB, "Non attribué"),

    BCPLG0(0x62, CompteurType.OTHER, "BCPL G0 pour compteur CJE et CBE"),

    NotAttrib099(0x63, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib100(0x64, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib101(0x65, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib102(0x66, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib103(0x67, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib104(0x68, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib105(0x69, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib106(0x6A, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib107(0x6B, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib108(0x6C, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib109(0x6D, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib110(0x6E, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib111(0x6F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib112(0x70, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib113(0x71, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib114(0x72, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib115(0x73, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib116(0x74, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib117(0x75, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib118(0x76, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib119(0x77, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib120(0x78, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib121(0x79, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib122(0x7A, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib123(0x7B, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib124(0x7C, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib125(0x7D, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib126(0x7E, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib127(0x7F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib128(0x80, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib129(0x81, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib130(0x82, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib131(0x83, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib132(0x84, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib133(0x85, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib134(0x86, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib135(0x87, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib136(0x88, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib137(0x89, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib138(0x8A, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib139(0x8B, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib140(0x8C, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib141(0x8D, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib142(0x8E, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib143(0x8F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib144(0x90, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib145(0x91, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib146(0x92, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib147(0x93, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib148(0x94, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib149(0x95, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib150(0x96, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib151(0x97, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib152(0x98, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib153(0x99, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib154(0x9A, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib155(0x9B, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib156(0x9C, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib157(0x9D, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib158(0x9E, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib159(0x9F, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib160(0xA0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib161(0xA1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib162(0xA2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib163(0xA3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib164(0xA4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib165(0xA5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib166(0xA6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib167(0xA7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib168(0xA8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib169(0xA9, CompteurType.NOATTRIB, "Non attribué"),

    EuredisBluetooth(0xAA, CompteurType.OTHER, "Coupleur EURIDIS bluetooth (PKE)"),

    NotAttrib171(0xAB, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib172(0xAC, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib173(0xAD, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib174(0xAE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib175(0xAF, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib176(0xB0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib177(0xB1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib178(0xB2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib179(0xB3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib180(0xB4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib181(0xB5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib182(0xB6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib183(0xB7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib184(0xB8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib185(0xB9, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib186(0xBA, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib187(0xBB, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib188(0xBC, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib189(0xBD, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib190(0xBE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib191(0xBF, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib192(0xC0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib193(0xC1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib194(0xC2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib195(0xC3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib196(0xC4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib197(0xC5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib198(0xC6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib199(0xC7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib200(0xC8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib201(0xC9, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib202(0xCA, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib203(0xCB, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib204(0xCC, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib205(0xCD, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib206(0xCE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib207(0xCF, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib208(0xD0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib209(0xD1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib210(0xD2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib211(0xD3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib212(0xD4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib213(0xD5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib214(0xD6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib215(0xD7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib216(0xD8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib217(0xD9, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib218(0xDA, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib219(0xDB, CompteurType.NOATTRIB, "Non attribué"),

    BcplGen1(0xDC, CompteurType.LINKY, "BCPL G1 LINKY pour compteur CJE"),

    NotAttrib221(0xDD, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib222(0xDE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib223(0xDF, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib224(0xE0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib225(0xE1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib226(0xE2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib227(0xE3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib228(0xE4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib229(0xE5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib230(0xE6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib231(0xE7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib232(0xE8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib233(0xE9, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib234(0xEA, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib235(0xEB, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib236(0xEC, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib237(0xED, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib238(0xEE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib239(0xEF, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib240(0xF0, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib241(0xF1, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib242(0xF2, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib243(0xF3, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib244(0xF4, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib245(0xF5, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib246(0xF6, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib247(0xF7, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib248(0xF8, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib249(0xF9, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib250(0xFA, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib251(0xFB, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib252(0xFC, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib253(0xFD, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib254(0xFE, CompteurType.NOATTRIB, "Non attribué"),
    NotAttrib255(0xFF, CompteurType.NOATTRIB, "Non attribué");

    private final CompteurType type;
    private final int id;
    private final String label;

    Compteur(int id, CompteurType type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }
}

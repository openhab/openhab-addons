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
package org.openhab.binding.teleinfo.internal.types;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Define the different available meter on the market
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public enum Meter {

    NotAttrib000(0x00, MeterType.NOATTRIB, "Non attribué"),

    BleuMonoMultiTarif(0x01, MeterType.BLEUE, "Compteur bleu monophasé multitarif (BBR) - 1ère gen"),

    CentraleG3(0x02, MeterType.OTHER, "Centrale de mesure G3 - Poste HTA/BT"),

    Concentrateur01(0x03, MeterType.OTHER, "Concentrateur multi-compteurs / électrique + 2 fluides"),
    Concentrateur02(0x04, MeterType.OTHER, "Concentrateur simplifié / élec"),

    BleuMonoSimpleTarif(0x05, MeterType.BLEUE, "Compteur bleu monophasé simple tarif électronique - 1ère gen"),

    JauneElectro(0x06, MeterType.JAUNE, "Compteur jaune électronique / tarif modulable"),

    PrismeElectro(0x07, MeterType.PRISME, "Compteur électronique universel (PRISME ou ICE)"),
    SauterEuridis(0x08, MeterType.OTHER, "Compteur sauter modifié EURIDIS"),

    BleuTri1Gen(0x09, MeterType.BLEUE, "Compteur bleu triphasé électronique - 1ère gen"),

    JauneElectro2Gen(0x0A, MeterType.JAUNE, "Compteur jaune électronique 2ème gen"),

    BleuTriSimpleTarif(0x0B, MeterType.BLEUE, "Compteur bleu monophasé simple tarif FERRARIS"),

    Prisme(0x0C, MeterType.PRISME, "Compteur prisme"),

    CentraleMesureF1(0x0D, MeterType.OTHER, "Centrale de mesure G1 - Poste HTA/BT"),
    AnalyserCourbeCharge(0x0E, MeterType.OTHER, "Analyseur de courbe de charge (panel BT)"),

    BleuMonoMultiTarifSansBBR(0x0F, MeterType.BLEUE, "Compteur bleu monophasé multitarif électronique sans BBR"),
    BleuExpICC(0x10, MeterType.BLEUE, "Compteur bleu expérimentation « 10000 ICC »"),

    ExpICC(0x11, MeterType.OTHER, "ICC expérimentation « 10000 ICC »"),

    DetectDefHTA(0x12, MeterType.OTHER, "Détecteur de défauts / HTA , neutre compensé"),
    Concentrateur03(0x13, MeterType.OTHER, "Concentrateur multi-compteurs / 3 fluides indifférenciés"),

    BleuMonoMultiTarifDemiTaux1Gen(0x14, MeterType.BLEUE, "Compteur bleu monophasé multitarif 1/2 - 1ère gen"),
    BleuTriMultiTarifDemiTaux1Gen(0x15, MeterType.BLEUE, "Compteur bleu triphasé 1/2 taux - 1ére gen"),
    BleuMonoMultiTarif2Gen(0x16, MeterType.BLEUE, "Compteur bleu monophasé multitarif - 2ème gen"),
    BleuMonoMultiTarifDemiTaux2Gen(0x17, MeterType.BLEUE, "Compteur bleu monophasé multitarif 1/2 - 2ème gen"),

    NotAttrib024(0x18, MeterType.NOATTRIB, "Non attribué"),

    BleuMonoSimpleTarif2Gen(0x19, MeterType.BLEUE, "Compteur bleu monophasé simple tarif - 2ème gen"),
    BleuTri20002Gen(0x1A, MeterType.BLEUE, "Compteur bleu triphasé - palier 2000 - 2ème gen"),
    BleuTri2000DemiTaux2Gen(0x1B, MeterType.BLEUE, "Compteur bleu triphasé - palier 2000 ½ taux - 2ème gen"),
    BleuMono20073Gen(0x1C, MeterType.BLEUE, "Compteur bleu monophasé multitarif - palier 2007 - 3ème gen"),
    BleuMono2007DemiTaux3Gen(0x1D, MeterType.BLEUE, "Compteur bleu monophasé multi ½ - 2007 - 3ème gen"),
    BleuTri20073Gen(0x1E, MeterType.BLEUE, "Compteur bleu triphasé - palier 2007 - 3ème gen"),
    BleuTri2007DemiTaux3Gen(0x1F, MeterType.BLEUE, "Compteur bleu triphasé ½ taux - palier 2007 – 3ème gen"),
    BleuTriTele(0x20, MeterType.BLEUE, "Compteur bleu triphasé télétotalisation"),

    JauneElectroDirect(0x21, MeterType.JAUNE, "Compteur jaune électronique branchement direct"),

    Ice(0x22, MeterType.OTHER, "Compteur ICE 4 quadrants"),
    Tromaran(0x23, MeterType.OTHER, "Compteur trimaran 2P classe 0,2s pour RTE"),
    PmePmiBt(0x24, MeterType.OTHER, "Compteur PME-PMI BT > 36kva"),
    Prepaie(0x25, MeterType.OTHER, "Compteur prépaiement"),
    Hxe34(0x26, MeterType.OTHER, "Compteur triphasé HXE34 de HECL"),

    NotAttrib039(0x27, MeterType.NOATTRIB, "Non attribué"),

    AffMulti(0x28, MeterType.OTHER, "Système d'affichage multiusage (SAM)"),

    NotAttrib041(0x29, MeterType.NOATTRIB, "Non attribué"),

    ActarisMono1(0x2A, MeterType.OTHER, "Compteur monophasé export (ACTARIS)"),
    ActarisMono2(0x2B, MeterType.OTHER, "Compteur monophasé export (ACTARIS)"),
    ActarisTri1(0x2C, MeterType.OTHER, "Compteur triphasé export ACTARIS"),
    ActarisTri2(0x2D, MeterType.OTHER, "Compteur triphasé export ACTARIS"),
    ModemEuredis(0x2E, MeterType.OTHER, "Modem EURIDIS pour compteur PME-PMI"),

    NotAttrib047(0x2F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib048(0x30, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib049(0x31, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib050(0x32, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib051(0x33, MeterType.NOATTRIB, "Non attribué"),

    Concentrateur04(0x34, MeterType.OTHER, "Concentrateur simplifié / gaz ou Transpondeur Gaz EURIDIS"),
    Concentrateur05(0x35, MeterType.OTHER, "Concentrateur multi-compteurs / VGR"),
    Concentrateur06(0x36, MeterType.OTHER, "Concentrateur multi-compteurs / gaz"),

    NotAttrib055(0x37, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib056(0x38, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib057(0x39, MeterType.NOATTRIB, "Non attribué"),

    PrismeBaie(0x3A, MeterType.OTHER, "Baie prisme de télétotalisation, exp Lyon"),

    NotAttrib059(0x3B, MeterType.NOATTRIB, "Non attribué"),

    LinkyMono60AG1(0x3C, MeterType.LINKY, "Compteur monophasé 60A LINKY - généralisation G1 - arrivée basse"),
    LinkyMono60AG3(0x3D, MeterType.LINKY, "Compteur monophasé 60A LINKY - généralisation G3 - arrivée haute"),
    LinkyMono90AG1(0x3E, MeterType.LINKY, "Compteur monophasé 90A LINKY - généralisation G1 - arrivée basse"),
    LinkyTri60AG1(0x3F, MeterType.LINKY, "Compteur triphasé 60A LINKY - généralisation G1 - arrivée basse"),
    LinkyTri60AG3(0x40, MeterType.LINKY, "Compteur monophasé 60A LINKY - généralisation G3 - arrivée basse"),
    LinkyMono60AG3Exp(0x41, MeterType.LINKY, "Compteur monophasé 90A LINKY expérimentation CPL G3 (2000 ex.)"),

    ModulaireGen(0x42, MeterType.OTHER, "Module du compteur modulaire généralisation"),

    LinkyMono90AG1Pil(0x43, MeterType.LINKY, "Compteur monophasé 90A LINKY - pilote G1 - arrivée basse"),
    LinkyTri60AG1Pil(0x44, MeterType.LINKY, "Compteur triphasé 60A LINKY - pilote G1 - arrivée basse"),

    NotAttrib069(0x45, MeterType.NOATTRIB, "Non attribué"),

    LinkyMono60AG3I(0x46, MeterType.LINKY, "Compteur monophasé 60A LINKY - interopérabilité G3 - arrivée basse"),
    LinkyTru60AG3I(0x47, MeterType.LINKY, "Compteur triphasé 60A LINKY - interopérabilité G3 - arrivée basse"),

    HXE12K(0x48, MeterType.OTHER, "Compteur monophasé HXE12K 10-80A 4 tarifs (Hexing Electrical co,Ltd)"),

    NotAttrib073(0x49, MeterType.NOATTRIB, "Non attribué"),

    HXE34K(0x4A, MeterType.OTHER, "Compteur triphasé HXE34K 230/400V 10-80A"),

    LinkyMono90AG3P(0x4B, MeterType.LINKY, "Compteur monophasé 90A LINKY - palier 1 G3 - arrivée basse"),
    LinkyTru60AG3P(0x4C, MeterType.LINKY, "Compteur triphasé 60A LINKY - palier 1 G3 - arrivée basse"),

    NotAttrib077(0x4D, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib078(0x4E, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib079(0x4F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib080(0x50, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib081(0x51, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib082(0x52, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib083(0x53, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib084(0x54, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib085(0x55, MeterType.NOATTRIB, "Non attribué"),

    SeiMono(0x56, MeterType.OTHER, "Compteur numérique SEI monophasé 60A 230V - G3 - arrivée basse - 60Hz"),
    SeiTri(0x57, MeterType.OTHER, "Compteur numérique SEI triphasé 60A 230/400V - G3 - 60Hz"),
    PlcMono(0x58, MeterType.OTHER, "Compteur monophasé PLC DSMR2.2 (Actaris)"),
    PlcTri(0x59, MeterType.OTHER, "Compteur triphasé PLC DSMR2.2 (Actaris)"),
    CplGen1(0x5A, MeterType.OTHER, "Compteur monophasé CPL intégré 1ère gen"),
    CplGen2(0x5B, MeterType.OTHER, "Compteur triphasé CPL intégré 2ème gen"),

    LinkyMono90A(0x5C, MeterType.LINKY, "Compteur monophasé 90A LINKY ORES – G3 Palier 1"),
    LinkyTri60A3F(0x5D, MeterType.LINKY, "Compteur triphasé 60A 3 fils LINKY ORES – G3 Palier 1"),
    LinkyTri60A4F(0x5E, MeterType.LINKY, "Compteur triphasé 60A 4 fils LINKY ORES – G3 Palier 1"),

    NotAttrib095(0x5F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib096(0x60, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib097(0x61, MeterType.NOATTRIB, "Non attribué"),

    BCPLG0(0x62, MeterType.OTHER, "BCPL G0 pour compteur CJE et CBE"),

    NotAttrib099(0x63, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib100(0x64, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib101(0x65, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib102(0x66, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib103(0x67, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib104(0x68, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib105(0x69, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib106(0x6A, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib107(0x6B, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib108(0x6C, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib109(0x6D, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib110(0x6E, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib111(0x6F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib112(0x70, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib113(0x71, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib114(0x72, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib115(0x73, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib116(0x74, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib117(0x75, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib118(0x76, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib119(0x77, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib120(0x78, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib121(0x79, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib122(0x7A, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib123(0x7B, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib124(0x7C, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib125(0x7D, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib126(0x7E, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib127(0x7F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib128(0x80, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib129(0x81, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib130(0x82, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib131(0x83, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib132(0x84, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib133(0x85, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib134(0x86, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib135(0x87, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib136(0x88, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib137(0x89, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib138(0x8A, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib139(0x8B, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib140(0x8C, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib141(0x8D, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib142(0x8E, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib143(0x8F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib144(0x90, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib145(0x91, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib146(0x92, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib147(0x93, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib148(0x94, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib149(0x95, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib150(0x96, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib151(0x97, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib152(0x98, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib153(0x99, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib154(0x9A, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib155(0x9B, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib156(0x9C, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib157(0x9D, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib158(0x9E, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib159(0x9F, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib160(0xA0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib161(0xA1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib162(0xA2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib163(0xA3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib164(0xA4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib165(0xA5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib166(0xA6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib167(0xA7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib168(0xA8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib169(0xA9, MeterType.NOATTRIB, "Non attribué"),

    EuredisBluetooth(0xAA, MeterType.OTHER, "Coupleur EURIDIS bluetooth (PKE)"),

    NotAttrib171(0xAB, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib172(0xAC, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib173(0xAD, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib174(0xAE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib175(0xAF, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib176(0xB0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib177(0xB1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib178(0xB2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib179(0xB3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib180(0xB4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib181(0xB5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib182(0xB6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib183(0xB7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib184(0xB8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib185(0xB9, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib186(0xBA, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib187(0xBB, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib188(0xBC, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib189(0xBD, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib190(0xBE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib191(0xBF, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib192(0xC0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib193(0xC1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib194(0xC2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib195(0xC3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib196(0xC4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib197(0xC5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib198(0xC6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib199(0xC7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib200(0xC8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib201(0xC9, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib202(0xCA, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib203(0xCB, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib204(0xCC, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib205(0xCD, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib206(0xCE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib207(0xCF, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib208(0xD0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib209(0xD1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib210(0xD2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib211(0xD3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib212(0xD4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib213(0xD5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib214(0xD6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib215(0xD7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib216(0xD8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib217(0xD9, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib218(0xDA, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib219(0xDB, MeterType.NOATTRIB, "Non attribué"),

    BcplGen1(0xDC, MeterType.LINKY, "BCPL G1 LINKY pour compteur CJE"),

    NotAttrib221(0xDD, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib222(0xDE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib223(0xDF, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib224(0xE0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib225(0xE1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib226(0xE2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib227(0xE3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib228(0xE4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib229(0xE5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib230(0xE6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib231(0xE7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib232(0xE8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib233(0xE9, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib234(0xEA, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib235(0xEB, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib236(0xEC, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib237(0xED, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib238(0xEE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib239(0xEF, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib240(0xF0, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib241(0xF1, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib242(0xF2, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib243(0xF3, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib244(0xF4, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib245(0xF5, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib246(0xF6, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib247(0xF7, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib248(0xF8, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib249(0xF9, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib250(0xFA, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib251(0xFB, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib252(0xFC, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib253(0xFD, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib254(0xFE, MeterType.NOATTRIB, "Non attribué"),
    NotAttrib255(0xFF, MeterType.NOATTRIB, "Non attribué");

    private static Map<Integer, Meter> idToValue = new Hashtable<Integer, Meter>();

    static {
        for (Meter cpt : EnumSet.allOf(Meter.class)) {
            // Yes, use some appropriate locale in production code :)
            idToValue.put(cpt.id, cpt);
        }
    }

    private final MeterType type;
    private final int id;
    private final String label;

    Meter(int id, MeterType type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }

    public final MeterType getCompteurType() {
        return type;
    }

    public static @Nullable Meter getCompteurForId(int id) {
        if (idToValue.containsKey(id)) {
            return idToValue.get(id);
        }

        return null;
    }

    public String getLabel() {
        return label;
    }
}

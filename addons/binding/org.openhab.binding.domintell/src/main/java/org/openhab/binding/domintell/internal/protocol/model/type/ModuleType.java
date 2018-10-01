/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.type;

import org.openhab.binding.domintell.internal.protocol.model.module.*;

/**
* The {@link ModuleType} is  module type enumeration. Only types with implementation classes are supported by the binding.
*
* @author Gabor Bicskei - Initial contribution
*/
public enum ModuleType {
    AMP,    //Sound Module
    BIR(DBIR01Module.class),    //8 bipolar relays
    DIM(DDIM01Module.class),    //8 dimmer commands
    DIR,    //IR detector
    DMV,    //Mechanical ventilation
    DMX,    //DMX Module
    ET2,    //Ethernet Light Protocol module
    FAN,    //Fan controller
    I10,    //Analog 0-10V input module
    DAL,    //DALI interface
    IS4(DISM04Module.class),    //4 Inputs module
    IS8(DISM08Module.class),    //8 Inputs module
    LCD,    //4*20 char LCD with 2 inputs
    LC3,    //Multifunction LCD
    LED,    //4 leds driver
    DET,    //Infrared detector
    DMR(DMR01Module.class),    //5 Mono-polar relays
    D10(DOUT10V02Module.class),    //0/1-10V dimmer module
    B81(DPBL01Module.class),    //1 Push Button Lythos (and 8 colors)
    B82(DPBL02Module.class),    //2 Push Button Lythos (and 8 colors)
    B84(DPBL04Module.class),    //4 Push Button Lythos (and 8 colors)
    B86(DPBL06Module.class),    //6 Push Button Lythos (and 8 colors)
    BR2(DPBR02Module.class),    //2 Push Button Rainbow (and RGB)
    BR4(DPBR04Module.class),    //4 Push Button Rainbow (and RGB)
    BR6(DPBR06Module.class),    //6 Push Button Rainbow (and RGB)
    BU1(DPB01Module.class),    //1 Push Button
    BU2(DPB02Module.class),    //2 Push Button
    BU4(DPB04Module.class),    //4 Push Button
    BU6(DPB06Module.class),    //6 Push Button
    PRL,    //Rainbow LCD push buttons
    PBL,    //LCD push buttons
    RS2,    //Serial Light Protocol module
    TE1(DTEMModule.class),    //Temperature sensor
    TE2(DTEMModule.class),    //Temperature sensor with 2*16 char LCD
    TRP,    //4 teleruptors
    TPV,    //2 shutter command with teleruptors Bit 0 Relay 1 = UP Bit 1 Relay 1 = DOWN
    TRV,    //4 shutter inverters Bit 0 Relay 1 = UP Bit 1 Relay 1 = DOWN
    V24,    //1 DC shutter command Bit 0 = UP – Bit 1 = DOWN
    TSB,    //Touchscreen
    LT2,    //TFT Touchscreen
    LT4,    //TFT Touchscreen with video
    T35,    //3,5 TFT Touchscreen
    VI1,    //1 button videophone
    VI2,    //2 buttons videophone
    MBD,    //Ex: Daikin RTD-NET
    CAM,    //Cameras informations
    CLK,    //Programmes clock (normal, reset and astronomical)
    STA,    //Radio Station name & frequency
    VAR(VARModule.class),    //Virtual programmed status
    SYS,    //System status
    TPL,    //Specific range of a Temp. profile
    TPR;    //Profile's name which contains next Temp. plage lists received

    private Class<? extends Module> clazz;

    ModuleType() {
    }

    ModuleType(Class<? extends Module> clazz) {
        this.clazz = clazz;
    }

    public boolean isModuleSupported() {
        return this.clazz != null;
    }

    public Class<? extends Module> getClazz() {
        return clazz;
    }
}

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

/**
 * Builds an i18n translation file from the local schemas.
 *
 * @author Mike Jagdis - Initial contribution
 */
const http = require('https');
const fs = require('fs');

const schemas = require('../../../src/main/resources/schema.json');


function codeToLabel(code) {
    let label = code
        .replaceAll(/(?:^|(?<= ))C_F|CF(?= |$)/g, "°C/°F")
        .replaceAll(/(?:^|(?<= ))G_m(?= |$)/g, "g/m")
        .replaceAll(/(?:^|(?<= ))P_K(?= |$)/g, "P/K")

        .replaceAll(/^([A-Z])_(.*)/g, "$2 ($1)") // Move leading capital to the end
        .replaceAll(/_+/g, " ").trim()
        .replaceAll(/\s+/g, " ")
        .replaceAll(/(?<=[^\d ])(?=\d)/g, " ") // Space before digits
        .replaceAll(/(?<=\d)(?=[^\d ])/g, " ") // Space after digits
        .replaceAll(/(?<=[a-z])(?=[A-Z](?:[a-z]|$))/g, " ") // Undo camel casing
        .replaceAll(/(?:^|(?<= ))[aA]rea(?=\w)/g, "Area ") // Space after "area"
        .replaceAll(/(?:^|(?<= ))[aA]uto(?!\W|matic|$)/g, "Auto ") // Space after "Auto"
        .replaceAll(/(?:^|(?<= ))[aA]vg(?=.)/g, "Average ") // Space after "Average" ("Avg")
        .replaceAll(/(?:^|(?<= ))[cC]lean(?!\W|ing|$)/g, "Clean ") // Space after "Clean"
        .replaceAll(/(?<=\w)onoff(?= |$)/g, " On/Off") // Space before "onoff"
        .replaceAll(/(?:^|(?<= ))light(colour|control|loop|mic|mode|pixel|state|status|switch)(?= |$)/g, "Light $1")

        .replaceAll(/(?:^|(?<=\W))(?:[a-z]+|[A-Z]{3,}|ON)(?=\W|$)/g, function (txt) { // Title case
                return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
            })

        .replaceAll(/(?:^|(?<= ))(onoff|ID|RGB|SIM|SMS|SOS|UV)(?=\w)/g, "$1 ") // Space after these abbreviations
        .replaceAll(/(?<=[a-z])(onoff|ID|RGB|SIM|SMS|SOS|UV)(?= )/g, " $1") // and space before these abbreviations

        .replaceAll(/^Cur /g, "") // Remove a leading "Cur"

        .replaceAll(/(?:^|(?<= ))3 D(?= |$)/g, "3D")
        .replaceAll(/(?:^|(?<= ))Ac(?= |$)/g, "AC")
        .replaceAll(/(?:^|(?<= ))Acpower(?= |$)/g, "AC Power")
        .replaceAll(/(?:^|(?<= ))Ack(?= |$)/g, "ACK")
        .replaceAll(/(?:^|(?<= ))Add Ele(?= |$)/g, "Electricity Consumed")
        .replaceAll(/(?:^|(?<= ))Ai(?= |$)/g, "AI")
        .replaceAll(/(?:^|(?<= ))Ala?m(?= |$)/g, "Alarm")
        .replaceAll(/(?:^|(?<= ))Ambi?(?= |$)/g, "Ambient")
        .replaceAll(/(?:^|(?<= ))Apn(?= |$)/g, "APN")
        .replaceAll(/(?:^|(?<= ))Arm Disarm(?= |$)/g, "Arm/Disarm")
        .replaceAll(/(?:^|(?<= ))Audo(?= |$)/g, "Auto")
        .replaceAll(/(?:^|(?<= ))Avg(?= |$)/g, "Average")
        .replaceAll(/(?:^|(?<= ))Back(?:cl)?ight(?= |$)/g, "Backlight")
        .replaceAll(/(?:^|(?<= ))Batt?(?= |$)/g, "Battery")
        .replaceAll(/(?:^|(?<= ))Batte[ry](?= |$)/g, "Battery")
        .replaceAll(/(?:^|(?<= ))Battary(?= |$)/g, "Battery")
        .replaceAll(/(?:^|(?<= ))Batttery(?= |$)/g, "Battery")
        .replaceAll(/(?:^|(?<= ))Battery Char(?= |$)/g, "Battery Charge")
        .replaceAll(/(?:^|(?<= ))Battery Dischar(?= |$)/g, "Battery Discharge")
        .replaceAll(/(?:^|(?<= ))Battery His(?= |$)/g, "Battery History")
        .replaceAll(/(?:^|(?<= ))Batterycharge(?= |$)/g, "Battery Charge")
        .replaceAll(/(?:^|(?<= ))Batterypencent(?= |$)/g, "Battery Pencent")
        .replaceAll(/(?:^|(?<= ))Batteryvoltage(?= |$)/g, "Battery Voltage")
        .replaceAll(/(?:^|(?<= ))Bklight(?= |$)/g, "Backlight")
        .replaceAll(/(?:^|(?<= ))Ble(?= |$)/g, "BLE")
        .replaceAll(/(?:^|(?<= ))Blod(?= |$)/g, "Blood")
        .replaceAll(/(?:^|(?<= ))Bloodtemp(?= |$)/g, "Blood Temperature")
        .replaceAll(/(?:^|(?<= ))Bost(?= |$)/g, "Boost")
        .replaceAll(/(?:^|(?<= ))Breathdistance(?= |$)/g, "Breath Distance")
        .replaceAll(/(?:^|(?<= ))Breathsensitivity(?= |$)/g, "Breath Sensitivity")
        .replaceAll(/(?:^|(?<= ))Calib(?:rat)?(?= |$)/g, "Calibration")
        .replaceAll(/(?:^|(?<= ))Cancle(?= |$)/g, "Cancel")
        .replaceAll(/(?:^|(?<= ))Carpetadaptive(?= |$)/g, "Carpet Adaptive")
        .replaceAll(/(?:^|(?<= ))(?:Ch|Channel)(?= |$)/g, "Channel")
        .replaceAll(/(?:^|(?<= ))Cfg(?= |$)/g, "Configuration")
        .replaceAll(/(?:^|(?<= ))Chg(?= |$)/g, "Charge")
        .replaceAll(/(?:^|(?<= ))Cnt(?= |$)/g, "Count")
        .replaceAll(/(?:^|(?<= ))Claen(?= |$)/g, "Clean")
        .replaceAll(/(?:^|(?<= ))Classicmode(?= |$)/g, "Classic Mode")
        .replaceAll(/(?:^|(?<= ))Closedoor(?= |$)/g, "Close Door")
        .replaceAll(/(?:^|(?<= ))Clr(?= |$)/g, "Clear")
        .replaceAll(/(?:^|(?<= ))Cmd(?= |$)/g, "Command")
        .replaceAll(/(?:^|(?<= ))Co 2(?= |$)/g, "CO₂")
        .replaceAll(/(?:^|(?<= ))Co(?= |$)/g, "CO")
        .replaceAll(/(?:^|(?<= ))Coef?(?= |$)/g, "Coefficient")
        .replaceAll(/(?:^|(?<= ))Color(?=[s ]|$)/g, "Colour") // If default English is GB not US
        .replaceAll(/(?:^|(?<= ))Colorlight(?=[s ]|$)/g, "Colour Light")
        .replaceAll(/(?:^|(?<= ))Comp(?:enst)?(?= |$)/g, "Compensation")
        .replaceAll(/(?:^|(?<= ))Comsumption(?= |$)/g, "Consumption")
        .replaceAll(/(?:^|(?<= ))Continu(?= |$)/g, "Continue")
        .replaceAll(/(?:^|(?<= ))Ctrl|Contorl|Contrl(?= |$)/g, "Control")
        .replaceAll(/(?:^|(?<= ))Cout(down)?(?= |$)/g, "Count$1")
        .replaceAll(/(?:^|(?<= ))Cpu(?= |$)/g, "CPU")
        .replaceAll(/(?:^|(?<= ))Creat(?= |$)/g, "Create")
        .replaceAll(/(?:^|(?<= ))(?:Curr?|Curent|Curren)(?= |$)/g, "Current")
        .replaceAll(/(?:^|(?<= ))([CF])value(?= |$)/g, "$1")
        .replaceAll(/(?:^|(?<= ))Cyclt(?= |$)/g, "Cycle")
        .replaceAll(/(?:^|(?<= ))Dc(?= |$)/g, "DC")
        .replaceAll(/(?:^|(?<= ))Delaytime(?= |$)/g, "Delay Time")
        .replaceAll(/(?:^|(?<= ))Deo(?= |$)/g, "Deodorization")
        .replaceAll(/(?:^|(?<= ))Deordrizer(?= |$)/g, "Deodorizer")
        .replaceAll(/(?:^|(?<= ))Detction(?= |$)/g, "Detection")
        .replaceAll(/(?:^|(?<= ))Dev(?= |$)/g, "Device")
        .replaceAll(/(?:^|(?<= ))Dhw(?= |$)/g, "DHW")
        .replaceAll(/(?:^|(?<= ))Discharg(?= |$)/g, "Discharge")
        .replaceAll(/(?:^|(?<= ))Dif(?= |$)/g, "Diff")
        .replaceAll(/(?:^|(?<= ))Disp(?:aly)?(?= |$)/g, "Display")
        .replaceAll(/(?:^|(?<= ))Displayctr(?= |$)/g, "Display Counter")
        .replaceAll(/(?:^|(?<= ))Eco(?= |$)/g, "ECO")
        .replaceAll(/(?:^|(?<= ))Edgebrush(?= |$)/g, "Edge Brush")
        .replaceAll(/(?:^|(?<= ))Ele(?= |$)/g, "Electricity")
        .replaceAll(/(?:^|(?<= ))En(?= |$)/g, "Enable")
        .replaceAll(/(?:^|(?<= ))Equiment(?= |$)/g, "Equipment")
        .replaceAll(/(?:^|(?<= ))Erro?(?= |$)/g, "Error")
        .replaceAll(/(?:^|(?<= ))Eu(?= |$)/g, "EU")
        .replaceAll(/(?:^|(?<= ))Feellike(?= |$)/g, "Feels Like")
        .replaceAll(/(?:^|(?<= ))Filtrat(?= |$)/g, "Filtrate")
        .replaceAll(/(?:^|(?<= ))Fl Oz(?= |$)/g, "fl oz")
        .replaceAll(/(?:^|(?<= ))Flg(?= |$)/g, "Flag")
        .replaceAll(/(?:^|(?<= ))Floortemp(?= |$)/g, "Floor Temperature")
        .replaceAll(/(?:^|(?<= ))Flowrater?(?= |$)/g, "Flow Rate")
        .replaceAll(/(?:^|(?<= ))Flowswitch(?= |$)/g, "Flow Switch")
        .replaceAll(/(?:^|(?<= ))Foor(?= |$)/g, "Floor")
        .replaceAll(/(?:^|(?<= ))Freq(?= |$)/g, "Frequency")
        .replaceAll(/(?:^|(?<= ))Freshair(?= |$)/g, "Fresh Air")
        .replaceAll(/(?:^|(?<= ))Frosttemp(?= |$)/g, "Frost Temperature")
        .replaceAll(/^Fuc (.*)$/g, "$1 Function")
        .replaceAll(/(?:^|(?<= ))Fw(?= |$)/g, "Firmware")
        .replaceAll(/(?:^|(?<= ))Fun?c(?= |$)/g, "Function")
        .replaceAll(/(?:^|(?<= ))Gradi?c(?= |$)/g, "Gradient")
        .replaceAll(/(?:^|(?<= ))Gsm?c(?= |$)/g, "GSM")
        .replaceAll(/(?:^|(?<= ))Heat Cool(?= |$)/g, "Heat/Cool")
        .replaceAll(/(?:^|(?<= ))Hi(?= |$)/g, "High")
        .replaceAll(/(?:^|(?<= ))Hin(?= |$)/g, "H In")
        .replaceAll(/(?:^|(?<= ))His(?= |$)/g, "History")
        .replaceAll(/(?:^|(?<= ))Hout(?= |$)/g, "H Out")
        .replaceAll(/(?:^|(?<= ))Hdmi(?= |$)/g, "HDMI")
        .replaceAll(/(?:^|(?<= ))Hdr(?= |$)/g, "HDR")
        .replaceAll(/(?:^|(?<= ))Hepa(?= |$)/g, "HEPA")
        .replaceAll(/(?:^|(?<= ))Hightemp(?= |$)/g, "High Temperature")
        .replaceAll(/(?:^|(?<= ))Hr(?= |$)/g, "Hour")
        .replaceAll(/(?:^|(?<= ))Huid(?= |$)/g, "Humidity")
        .replaceAll(/(?:^|(?<= ))Huimidity(?= |$)/g, "Humidity")
        .replaceAll(/(?:^|(?<= ))Hum(?:i|id)?(?= |$)/g, "Humidity")
        .replaceAll(/(?:^|(?<= ))Id(?= |$)/g, "ID")
        .replaceAll(/(?:^|(?<= ))Idx(?= |$)/g, "Index")
        .replaceAll(/(?:^|(?<= ))Iic(?= |$)/g, "IIC")
        .replaceAll(/(?:^|(?<= ))Ill?um(?:in)?(?= |$)/g, "Illumination")
        .replaceAll(/(?:^|(?<= ))Imei(?= |$)/g, "IMEI")
        .replaceAll(/(?:^|(?<= ))Imsi(?= |$)/g, "IMSI")
        .replaceAll(/(?:^|(?<= ))Indicatorlight(?= |$)/g, "Indicator Light")
        .replaceAll(/(?:^|(?<= ))Infared(?= |$)/g, "Infrared")
        .replaceAll(/(?:^|(?<= ))Instan(?= |$)/g, "Instant")
        .replaceAll(/(?:^|(?<= ))Invert(?= |$)/g, "Inverter")
        .replaceAll(/(?:^|(?<= ))Ios(?= |$)/g, "IOS")
        .replaceAll(/(?:^|(?<= ))Ip(?= |$)/g, "IP")
        .replaceAll(/(?:^|(?<= ))Ipm(?= |$)/g, "IPM")
        .replaceAll(/(?:^|(?<= ))Ir(?= |$)/g, "IR")
        .replaceAll(/(?:^|(?<= ))Ithium(?= |$)/g, "Lithium")
        .replaceAll(/(?:^|(?<= ))Lang(?= |$)/g, "Language")
        .replaceAll(/(?:^|(?<= ))Langue(?= |$)/g, "Language")
        .replaceAll(/(?:^|(?<= ))Lanuage(?= |$)/g, "Language")
        .replaceAll(/(?:^|(?<= ))Lanuageswitch(?= |$)/g, "Language Switch")
        .replaceAll(/(?:^|(?<= ))Lbs?(?= |$)/g, "lb")
        .replaceAll(/(?:^|(?<= ))Lcd(?= |$)/g, "LCD")
        .replaceAll(/(?:^|(?<= ))Leakagecurr(?= |$)/g, "Leakage Current")
        .replaceAll(/(?:^|(?<= ))Leavetemp(?= |$)/g, "Leave Temperature")
        .replaceAll(/(?:^|(?<= ))Led(?= |$)/g, "LED")
        .replaceAll(/(?:^|(?<= ))Ledlight(?= |$)/g, "LED Light")
        .replaceAll(/(?:^|(?<= ))Lefttime(?= |$)/g, "Left Time")
        .replaceAll(/(?:^|(?<= ))Leftright(?= |$)/g, "Left/Right")
        .replaceAll(/(?:^|(?<= ))Ligth(?= |$)/g, "Light")
        .replaceAll(/(?:^|(?<= ))Lim(?= |$)/g, "Limit")
        .replaceAll(/(?:^|(?<= ))Lowhu(?= |$)/g, "Low Humidity")
        .replaceAll(/(?:^|(?<= ))Lowpow(?= |$)/g, "Low Power")
        .replaceAll(/(?:^|(?<= ))Lowprotecttemp(?= |$)/g, "Low Protect Temperature")
        .replaceAll(/(?:^|(?<= ))Lowt(?= |$)/g, "Low Temperature")
        .replaceAll(/(?:^|(?<= ))Lowtemp(?= |$)/g, "Low Temperature")
        .replaceAll(/(?:^|(?<= ))Kg(?= |$)/g, "kg")
        .replaceAll(/(?:^|(?<= ))Kw(h?)(?= |$)/g, "kW$1")
        .replaceAll(/(?:^|(?<= ))Maproominfo(?= |$)/g, "Map Room Info")
        .replaceAll(/(?:^|(?<= ))Mach(?= |$)/g, "Machine")
        .replaceAll(/(?:^|(?<= ))Max(?= |$)/g, "Maximum")
        .replaceAll(/(?:^|(?<= ))Maxco 2(?= |$)/g, "Maximum CO₂")
        .replaceAll(/(?:^|(?<= ))Maxheattemp(?= |$)/g, "Maximum Heat Temperature")
        .replaceAll(/(?:^|(?<= ))Maxhum(?= |$)/g, "Maximum Humidity")
        .replaceAll(/(?:^|(?<= ))Maxmin(?= |$)/g, "Max/Min")
        .replaceAll(/(?:^|(?<= ))Maxtemp(?= |$)/g, "Maximum Temperature")
        .replaceAll(/(?:^|(?<= ))Micro?(?= |$)/g, "Mic")
        .replaceAll(/(?:^|(?<= ))Minc(?= |$)/g, "Minimum C")
        .replaceAll(/(?:^|(?<= ))Minf(?= |$)/g, "Minimum F")
        .replaceAll(/(?:^|(?<= ))Mini?(?= |$)/g, "Minimum")
        .replaceAll(/(?:^|(?<= ))Minbright(?= |$)/g, "Minimum Brightness")
        .replaceAll(/(?:^|(?<= ))Minheattemp(?= |$)/g, "Minimum Heat Temperature")
        .replaceAll(/(?:^|(?<= ))Mini?hum(?= |$)/g, "Minimum Humidity")
        .replaceAll(/(?:^|(?<= ))Mini?temp(?= |$)/g, "Minimum Temperature")
        .replaceAll(/(?:^|(?<= ))Mixtemp(?= |$)/g, "Mix Temperature")
        .replaceAll(/(?:^|(?<= ))Mcu(?= |$)/g, "MCU")
        .replaceAll(/(?:^|(?<= ))Mdoe(?= |$)/g, "Mode")
        .replaceAll(/(?:^|(?<= ))Metter(?= |$)/g, "Meter")
        .replaceAll(/(?:^|(?<= ))Modereset(?= |$)/g, "Mode Reset")
        .replaceAll(/(?:^|(?<= ))Modeset(?= |$)/g, "Mode Set")
        .replaceAll(/(?:^|(?<= ))Modetype(?= |$)/g, "Mode Type")
        .replaceAll(/(?:^|(?<= ))Moisure(?= |$)/g, "Moisture")
        .replaceAll(/(?:^|(?<= ))Moniton(?= |$)/g, "Monitor")
        .replaceAll(/(?:^|(?<= ))Moto(?= |$)/g, "Motor")
        .replaceAll(/(?:^|(?<= ))Motorspeed(?= |$)/g, "Motor Speed")
        .replaceAll(/(?:^|(?<= ))Mov(?= |$)/g, "Movement")
        .replaceAll(/(?:^|(?<= ))Movedistance(?= |$)/g, "Movement Distance")
        .replaceAll(/(?:^|(?<= ))Movesensitivity(?= |$)/g, "Movement Sensitivity")
        .replaceAll(/(?:^|(?<= ))Mppt(?= |$)/g, "MPPT")
        .replaceAll(/(?:^|(?<= ))Msg(?= |$)/g, "Message")
        .replaceAll(/(?:^|(?<= ))Neardis(?= |$)/g, "Near Distance")
        .replaceAll(/(?:^|(?<= ))Nfc(?= |$)/g, "NFC")
        .replaceAll(/(?:^|(?<= ))Nightbrightness(?= |$)/g, "Night Brightness")
        .replaceAll(/(?:^|(?<= ))Nightlight(?= |$)/g, "Night Light")
        .replaceAll(/(?:^|(?<= ))Nightvision(?= |$)/g, "Night Vision")
        .replaceAll(/(?:^|(?<= ))NO(?= |$)/g, "No")
        .replaceAll(/(?:^|(?<= ))No(?= \d)/g, "No.")
        .replaceAll(/(?:^|(?<= ))Not ?[dD]isturb(?= \d)/g, "Do Not Disturb")
        .replaceAll(/(?:^|(?<= ))Ntc(?= |$)/g, "NTC")
        .replaceAll(/(?:^|(?<= ))Num(?= |$)/g, "Number")
        .replaceAll(/(?:^|(?<= ))[oO]n ?[oO]ff(?= |$)/g, "On/Off")
        .replaceAll(/(?:^|(?<= ))[oO]n ?[oO]ffby(?= |$)/g, "On/Off By")
        .replaceAll(/(?:^|(?<= ))Openwindow(?= |$)/g, "Over Window")
        .replaceAll(/(?:^|(?<= ))Openwindowfunction(?= |$)/g, "Over Window Function")
        .replaceAll(/(?:^|(?<= ))Oc(?= |$)/g, "Over Current")
        .replaceAll(/(?:^|(?<= ))Occ(?= |$)/g, "Occupancy")
        .replaceAll(/(?:^|(?<= ))Ontimer(?= |$)/g, "On Timer")
        .replaceAll(/(?:^|(?<= ))Osc(?= |$)/g, "OSC")
        .replaceAll(/(?:^|(?<= ))Ota(?= |$)/g, "OTA")
        .replaceAll(/(?:^|(?<= ))Otc(?= |$)/g, "OTC")
        .replaceAll(/(?:^|(?<= ))Outerlimit(?= |$)/g, "Outer Limit")
        .replaceAll(/(?:^|(?<= ))Ov(?= |$)/g, "Over Voltage")
        .replaceAll(/(?:^|(?<= ))Overheattemp(?= |$)/g, "Overheat Temperature")
        .replaceAll(/(?:^|(?<= ))Overtemp(?= |$)/g, "Over Temperature")
        .replaceAll(/(?:^|(?<= ))Overvol(?= |$)/g, "Over VOltage")
        .replaceAll(/(?:^|(?<= ))Paly(?= |$)/g, "Play")
        .replaceAll(/(?:^|(?<= ))Param(?:ater)?(?= |$)/g, "Parameter")
        .replaceAll(/(?:^|(?<= ))Pct(?= |$)/g, "Percent")
        .replaceAll(/(?:^|(?<= ))Pir(?= |$)/g, "PIR")
        .replaceAll(/(?:^|(?<= ))Playtrack(?= |$)/g, "Play Track")
        .replaceAll(/(?:^|(?<= ))Pencent(?= |$)/g, "Percent")
        .replaceAll(/(?:^|(?<= ))Pm (\d+)?(?= |$)/g, "PM$1")
        .replaceAll(/(?:^|(?<= ))Pos(?= |$)/g, "Position")
        .replaceAll(/(?:^|(?<= ))Pow?(?= |$)/g, "Power")
        .replaceAll(/(?:^|(?<= ))Powerofftime(?= |$)/g, "Power Off Time")
        .replaceAll(/(?:^|(?<= ))Pressture(?= |$)/g, "Pressure")
        .replaceAll(/(?:^|(?<= ))[pP]rev ?[nN]ext(?= |$)/g, "Prev/Next")
        .replaceAll(/(?:^|(?<= ))Ptz(?= |$)/g, "PTZ")
        .replaceAll(/(?:^|(?<= ))Pumb?(?= |$)/g, "Pump")
        .replaceAll(/(?:^|(?<= ))Pumpstatus?(?= |$)/g, "Pump Status")
        .replaceAll(/(?:^|(?<= ))Pv(?= |$)/g, "PV")
        .replaceAll(/(?:^|(?<= ))Pvhub(?= |$)/g, "PV Hub")
        .replaceAll(/(?:^|(?<= ))Pvrpm(?= |$)/g, "PV RPM")
        .replaceAll(/(?:^|(?<= ))Pwm(?= |$)/g, "PWM")
        .replaceAll(/(?:^|(?<= ))Pwr?(?= |$)/g, "Power")
        .replaceAll(/(?:^|(?<= ))Qr?(?= |$)/g, "QR")
        .replaceAll(/(?:^|(?<= ))Quickstart(?= |$)/g, "Quick Start")
        .replaceAll(/(?:^|(?<= ))Rateofwork(?= |$)/g, "Rate of Work")
        .replaceAll(/(?:^|(?<= ))Rcordswitch(?= |$)/g, "Record Switch")
        .replaceAll(/(?:^|(?<= ))Rec(?= |$)/g, "Record")
        .replaceAll(/(?:^|(?<= ))Remotelink(?= |$)/g, "Remote Link")
        .replaceAll(/(?:^|(?<= ))Repeate(?= |$)/g, "Repeat")
        .replaceAll(/(?:^|(?<= ))Replacefilter(?= |$)/g, "Replace Filter")
        .replaceAll(/(?:^|(?<= ))resetLIfe Span(?= |$)/g, "Reset Lifespan")
        .replaceAll(/(?:^|(?<= ))Rf(?= |$)/g, "RF")
        .replaceAll(/(?:^|(?<= ))Rfstudy(?= |$)/g, "RF Study")
        .replaceAll(/(?:^|(?<= ))Pxiels?(?= |$)/g, "Pixels")
        .replaceAll(/(?:^|(?<= ))Rad(?= Temp)/g, "Radiator")
        .replaceAll(/(?:^|(?<= ))Remoter(?= |$)/g, "Remote")
        .replaceAll(/(?:^|(?<= ))Resetfilter(?= |$)/g, "Reset Filter")
        .replaceAll(/(?:^|(?<= ))RFIDNotify(?= |$)/g, "RFID Notify")
        .replaceAll(/(?:^|(?<= ))Rfid(?= |$)/g, "RFID")
        .replaceAll(/(?:^|(?<= ))Rgb(?= |$)/g, "RGB")
        .replaceAll(/(?:^|(?<= ))Rgbcw(?= |$)/g, "RGBCW")
        .replaceAll(/(?:^|(?<= ))Rhr(?= |$)/g, "RHR")
        .replaceAll(/(?:^|(?<= ))Rkwh(?= |$)/g, "R kWh")
        .replaceAll(/(?:^|(?<= ))Ritht(?= |$)/g, "Right")
        .replaceAll(/(?:^|(?<= ))Rlr(?= |$)/g, "RLR")
        .replaceAll(/(?:^|(?<= ))RMoisure(?= |$)/g, "R Moisture")
        .replaceAll(/(?:^|(?<= ))Robo(?= |$)/g, "Robot")
        .replaceAll(/(?:^|(?<= ))Robotmodel(?= |$)/g, "Robot Model")
        .replaceAll(/(?:^|(?<= ))Rollbrush(?= |$)/g, "Roll Brush")
        .replaceAll(/(?:^|(?<= ))Roomtemp(?= |$)/g, "Room Temperature")
        .replaceAll(/(?:^|(?<= ))Rre(?= |$)/g, "RRE")
        .replaceAll(/(?:^|(?<= ))Rs 485(?= |$)/g, "RS485")
        .replaceAll(/(?:^|(?<= ))Rssi(?= |$)/g, "RSSI")
        .replaceAll(/(?:^|(?<= ))Rtc(?= |$)/g, "RTC")
        .replaceAll(/(?:^|(?<= ))Runtemp(?= |$)/g, "Run Temperature")
        .replaceAll(/(?:^|(?<= ))Rw(?= |$)/g, "RW")
        .replaceAll(/(?:^|(?<= ))Qty(?= |$)/g, "Quantity")
        .replaceAll(/(?:^|(?<= ))QY(?= |$)/g, "Query")
        .replaceAll(/(?:^|(?<= ))(Rest|Work)day(?= |$)/g, "$1 Day")
        .replaceAll(/(?:^|(?<= ))Rly(?= |$)/g, "Relay")
        .replaceAll(/(?:^|(?<= ))Runing(?= |$)/g, "Running")
        .replaceAll(/(?:^|(?<= ))Savemap(?= |$)/g, "Save Map")
        .replaceAll(/(?:^|(?<= ))Savemoney(?= |$)/g, "Save Money")
        .replaceAll(/(?:^|(?<= ))Sate(?= |$)/g, "State")
        .replaceAll(/(?:^|(?<= ))Scane(?= |$)/g, "Scene")
        .replaceAll(/(?:^|(?<= ))Scen(?= |$)/g, "Scene")
        .replaceAll(/(?:^|(?<= ))Sched(?= |$)/g, "Schedule")
        .replaceAll(/(?:^|(?<= ))Schreset(?= |$)/g, "Schedule Reset")
        .replaceAll(/(?:^|(?<= ))Scrennshot(?= |$)/g, "Screenshot")
        .replaceAll(/(?:^|(?<= ))Sd(?= |$)/g, "SD")
        .replaceAll(/(?:^|(?<= ))Sensibility(?= |$)/g, "Sensitivity")
        .replaceAll(/(?:^|(?<= ))Sensorstatus(?= |$)/g, "Sensor Status")
        .replaceAll(/(?:^|(?<= ))Selfclean(?= |$)/g, "Self Clean")
        .replaceAll(/(?:^|(?<= ))Set(?:temp|TEMP)(?= |$)/g, "Set Temperature")
        .replaceAll(/(?:^|(?<= ))Settemplow(?= |$)/g, "Set Temperature Low")
        .replaceAll(/(?:^|(?<= ))Settempup(?= |$)/g, "Set Temperature Up")
        .replaceAll(/(?:^|(?<= ))Sigle(?= |$)/g, "Single")
        .replaceAll(/(?:^|(?<= ))Shotdown(?= |$)/g, "Shutdown")
        .replaceAll(/(?:^|(?<= ))Sku(?= |$)/g, "SKU")
        .replaceAll(/(?:^|(?<= ))Slowcharge(?= |$)/g, "Slow Charge")
        .replaceAll(/(?:^|(?<= ))Slowchargeset(?= |$)/g, "Slow Charge Set")
        .replaceAll(/(?:^|(?<= ))Sms(?= |$)/g, "SMS")
        .replaceAll(/(?:^|(?<= ))Snesitivity(?= |$)/g, "Sensitivity")
        .replaceAll(/(?:^|(?<= ))Sos(?= |$)/g, "SOS")
        .replaceAll(/(?:^|(?<= ))Soud(?= |$)/g, "Sound")
        .replaceAll(/(?:^|(?<= ))Soundmode(?= |$)/g, "Sound Mode")
        .replaceAll(/(?:^|(?<= ))Specialcmd(?= |$)/g, "Special Command")
        .replaceAll(/(?:^|(?<= ))Speedpercentage(?= |$)/g, "Speed Percentage")
        .replaceAll(/(?:^|(?<= ))Spraymode(?= |$)/g, "Spray Mode")
        .replaceAll(/(?:^|(?<= ))Sprayswitch(?= |$)/g, "Spray Switch")
        .replaceAll(/(?:^|(?<= ))Spraytime(?= |$)/g, "Spray Time")
        .replaceAll(/(?:^|(?<= ))Src(?= |$)/g, "Source")
        .replaceAll(/(?:^|(?<= ))Stopirrigation(?= |$)/g, "Stop Irrigation")
        .replaceAll(/(?:^|(?<= ))Storge(?= |$)/g, "Storage")
        .replaceAll(/(?:^|(?<= ))Sunsetmode(?= |$)/g, "Sunset Mode")
        .replaceAll(/(?:^|(?<= ))Sw(?= |$)/g, "Switch")
        .replaceAll(/(?:^|(?<= ))Swip(?= |$)/g, "Swipe")
        .replaceAll(/(?:^|(?<= ))Swt?ich(?= |$)/g, "Switch")
        .replaceAll(/(?:^|(?<= ))Swithc(?= |$)/g, "Switch")
        .replaceAll(/(?:^|(?<= ))Sysvolum(?= |$)/g, "System Volume")
        .replaceAll(/(?:^|(?<= ))Syn(?= |$)/g, "Sync")
        .replaceAll(/(?:^|(?<= ))Telphoone(?= |$)/g, "Telephone")
        .replaceAll(/(?:^|(?<= ))Tem(?:erature|p|pe|per)?(?= |$)/g, "Temperature")
        .replaceAll(/(?:^|(?<= ))Temperalarm(?= |$)/g, "Temperature Alarm")
        .replaceAll(/(?:^|(?<= ))Temperature In(?= |$)/g, "Temperature Indoor")
        .replaceAll(/(?:^|(?<= ))Temperature Out(?= |$)/g, "Temperature Outdoor")
        .replaceAll(/(?:^|(?<= ))Temperature Outc(?= |$)/g, "Temperature Outdoor C")
        .replaceAll(/(?:^|(?<= ))Temperature Outf(?= |$)/g, "Temperature Outdoor F")
        .replaceAll(/(?:^|(?<= ))Temperatureswitch(?= |$)/g, "Temperature Switch")
        .replaceAll(/(?:^|(?<= ))Tempset(?= |$)/g, "Set Temperature")
        .replaceAll(/(?:^|(?<= ))Thr H(?= |$)/g, "Threshold High")
        .replaceAll(/(?:^|(?<= ))Thr L(?= |$)/g, "Threshold Low")
        .replaceAll(/(?:^|(?<= ))Thur(?= |$)/g, "Thu")
        .replaceAll(/(?:^|(?<= ))Time([AB])(Hour|Minute)(?= |$)/g, "Time $1 $2")
        .replaceAll(/(?:^|(?<= ))Timg(?= |$)/g, "Timing")
        .replaceAll(/(?:^|(?<= ))Tls(?= |$)/g, "TLS")
        .replaceAll(/(?:^|(?<= ))Trig(?= |$)/g, "Trigger")
        .replaceAll(/(?:^|(?<= ))Tv(?= |$)/g, "TV")
        .replaceAll(/(?:^|(?<= ))Uint(?= |$)/g, "Unit")
        .replaceAll(/(?:^|(?<= ))Umount(?= |$)/g, "Unmount")
        .replaceAll(/(?:^|(?<= ))Undertemp(?= |$)/g, "Under Temperature")
        .replaceAll(/(?:^|(?<= ))Undervol(?= |$)/g, "Under Voltage")
        .replaceAll(/(?:^|(?<= ))Updown(?= |$)/g, "Up/Down")
        .replaceAll(/(?:^|(?<= ))Upgradeflag(?= |$)/g, "Upgrade Flag")
        .replaceAll(/(?:^|(?<= ))Url(?= |$)/g, "URL")
        .replaceAll(/(?:^|(?<= ))Us(?= |$)/g, "US")
        .replaceAll(/(?:^|(?<= ))Usb(?= |$)/g, "USB")
        .replaceAll(/(?:^|(?<= ))Uuid(?= |$)/g, "UUID")
        .replaceAll(/(?:^|(?<= ))Uv(?= |$)/g, "UV")
        .replaceAll(/(?:^|(?<= ))Valu(?= |$)/g, "Value")
        .replaceAll(/(?:^|(?<= ))Ver(?= |$)/g, "Version")
        .replaceAll(/(?:^|(?<= ))Voiceswitch(?= |$)/g, "Voice Switch")
        .replaceAll(/(?:^|(?<= ))Voc(?= |$)/g, "VOC")
        .replaceAll(/(?:^|(?<= ))Volatage(?= |$)/g, "Voltage")
        .replaceAll(/(?:^|(?<= ))Vol(?= |$)/g, "Volume")
        .replaceAll(/(?:^|(?<= ))Votage(?= |$)/g, "Voltage")
        .replaceAll(/(?:^|(?<= ))Weightcount(?= |$)/g, "Weight Count")
        .replaceAll(/(?:^|(?<= ))Wi ?[fF]i(?= |$)/g, "WiFi")
        .replaceAll(/(?:^|(?<= ))Windchill(?= |$)/g, "Windows Chill")
        .replaceAll(/(?:^|(?<= ))Windowsopen[fF]i(?= |$)/g, "Windows Open")
        .replaceAll(/(?:^|(?<= ))Woke(?= |$)/g, "Work")
        .replaceAll(/(?:^|(?<= ))Worktime(?= |$)/g, "Work Time")

        .replaceAll(/(?:^|(?<= ))Current (\w* )(Humidity|Temperature)(?= |$)/g, "$1$2")
        .replaceAll(/(?:^|(?<= ))(Humidity|Temperature) (?:Current|Value)(?= |$)/g, "$1")

        .replaceAll(/(?<=Temperature(?: Comp| Diff| Floor| Maximum| Minimum| Sensitivity|(?: Set(?:ting)?(?: Quick)?))? )([CF])(?= |$)/g, "°$1") // FIXME: there are some Temperature_[ABC]

        .replaceAll(/^([AD])(In|Out) /g, "$1 $2 ")
    ;

    switch (label) {
        case "ACFan Speed":
            label = "AC Fan Speed";
            break;
        case "Achz":
            label = "AC Frequency";
            break;
        case "Aci":
            label = "AC Current";
            break;
        case "Acv":
            label = "AC Voltage";
            break;
        case "Addroomdoor":
            label = "Add Room Door";
            break;
        case "Airquality":
            label = "Air Quality";
            break;
        case "AlarmONOFF":
            label = "Alarm On/Off";
            break;
        case "Alarmd B":
            label = "Alarm Volume";
            break;
        case "Alarmswitch":
            label = "Alarm Switch";
            break;
        case "Alarmtype":
            label = "Alarm Type";
            break;
        case "Antileggionella":
            label = "Antilegionella";
            break;
        case "Asynfade":
            label = "Async Fade";
            break;
        case "Asynjump":
            label = "Async Jump";
            break;
        case "Children Lock On":
            label = "Child Lock";
            break;
        case "DCFan Speed":
            label = "DC Fan Speed";
            break;
        case "Dustcollection":
            label = "Dust Collection";
            break;
        case "Filterreplace":
            label = "Filter Replace";
            break;
        case "Finddev":
            label = "Find Device";
            break;
        case "Highalarmtemp":
            label = "High Alarm Temp";
            break;
        case "Limition Floor Temperature":
            label = "Floor Temperature Limit";
            break;
        case "Main Bsh Tm":
            label = "Main Brush Time";
            break;
        case "Material Life Rollbru":
            label = "Material Life Rollbrush";
            break;
        case "Modifyname":
            label = "Modify Name";
            break;
        case "Musicrhythm":
            label = "Music Rhythm";
            break;
        case "Openwindow":
            label = "Open Window";
            break;
        case "Outtds":
            label = "Out TDS";
            break;
        case "pvi":
            label = "PV Current";
            break;
        case "pvv":
            label = "PV Voltage";
            break;
        case "Radiolist":
            label = "Radio List";
            break;
        case "Rain Sen TotalONOFF":
            label = "Rain Sensor Total On/Off";
            break;
        case "Rate Vol":
            label = "Rate Voltage";
            break;
        case "Real Vol":
            label = "Real Voltage";
            break;
        case "Setial Numberr":
            label = "Serial Number";
            break;
        case "Slowflash":
            label = "Slow Flash";
            break;
        case "Soft Version":
            label = "Software Version";
            break;
        case "Targettemset":
            label = "Target Temperature Setting";
            break;
        case "Thelamp":
            label = "Light";
            break;
        case "Tempalarm":
            label = "Temperature Alarm";
            break;
        case "Tempchange":
            label = "Temperature Change";
            break;
        case "Tempstatus":
            label = "Temperature Status";
            break;
        case "Timerreport":
            label = "Timer Report";
            break;
        case "Uptemp":
            label = "UP Temperature";
            break;
        case "Urget Push Infor":
            label = "Urgent Push Information";
            break;
        case "Windleftright":
            label = "Wind Left/Right";
            break;
        case "Windmode":
            label = "Wind Mode";
            break;
    }

    return label;
}


let labels = new Map([
    // These have been seen in remote schemas but do not appear in the local schemas
    // so will not be generated.
    [ "channel-type.tuya.va_temperature.label", "Temperature" ],
    [ "channel-type.tuya.va_humidity.label", "Humidity" ],
]);

for (product in schemas) {
    for (code in schemas[product]) {
        let key = "channel-type.tuya." + code.toLowerCase() + ".label";
        let value = codeToLabel(code);
        if (key in labels) {
            if (value !== labels.get(key)) {
                process.stderr.write("Conflict for " + key + " labels: \"", labels.get(key), "\" and \"" + value + "\"");
            }
        } else {
            labels.set(key, value);
        }
    }
}

process.stdout.write("# ------------------------------------------------------------------------\n");
process.stdout.write("# Everything from here onwards is generated by src/main/tool/mki18n.js.\n");
process.stdout.write("# You should make changes there as any changes here are likely to be lost.\n");
process.stdout.write("\n");
process.stdout.write("\n");
process.stdout.write("# Channel type labels\n");
process.stdout.write("\n");

for (let key of Array.from(labels.keys()).sort()) {
    process.stdout.write(key + " = " + labels.get(key) + "\n");
}

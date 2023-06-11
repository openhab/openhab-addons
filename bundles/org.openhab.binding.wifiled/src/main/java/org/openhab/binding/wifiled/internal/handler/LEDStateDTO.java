/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wifiled.internal.handler;

import static java.lang.Math.*;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link LEDStateDTO} class holds the data and the settings for a LED device (i.e. the selected colors, the running
 * program, etc.).
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis - Initial contribution
 * @author Ries van Twisk - Prevent flashes during classic driver color + white updates
 */
public class LEDStateDTO {
    private HSBType hsbType;
    private OnOffType power;
    private PercentType white;
    private PercentType white2;
    private StringType program;
    private PercentType programSpeed;

    public LEDStateDTO(OnOffType power, DecimalType hue, PercentType saturation, PercentType brightness,
            PercentType white, PercentType white2, StringType program, PercentType programSpeed) {
        this.hsbType = new HSBType(hue, saturation, brightness);
        this.power = power;
        this.white = white;
        this.white2 = white2;
        this.program = program;
        this.programSpeed = programSpeed;
    }

    public LEDStateDTO(OnOffType power, HSBType hsb, PercentType white, PercentType white2, StringType program,
            PercentType programSpeed) {
        this.hsbType = hsb;
        this.power = power;
        this.white = white;
        this.white2 = white2;
        this.program = program;
        this.programSpeed = programSpeed;
    }

    public PercentType getWhite() {
        return white;
    }

    public PercentType getWhite2() {
        return white2;
    }

    public StringType getProgram() {
        return program;
    }

    public PercentType getProgramSpeed() {
        return programSpeed;
    }

    @Override
    public String toString() {
        return power + "," + hsbType.getHue() + "," + hsbType.getSaturation() + "," + hsbType.getBrightness() + ","
                + getWhite() + "," + getWhite2() + " [" + getProgram() + "," + getProgramSpeed() + "]";
    }

    public static LEDStateDTO valueOf(int state, int program, int programSpeed, int red, int green, int blue, int white,
            int white2) {
        OnOffType power = (state & 0x01) != 0 ? OnOffType.ON : OnOffType.OFF;

        float[] hsv = new float[3];
        Color.RGBtoHSB(red, green, blue, hsv);
        DecimalType h = new DecimalType(new BigDecimal(hsv[0]).multiply(new BigDecimal(360.0)));
        PercentType s = new PercentType(new BigDecimal(hsv[1]).multiply(new BigDecimal(100.0)));
        PercentType b = new PercentType(new BigDecimal(hsv[2]).multiply(new BigDecimal(100.0)));
        HSBType hsbType = new HSBType(h, s, b);

        PercentType w = new PercentType(new BigDecimal(white).divide(new BigDecimal(255.0), 3, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100.0)));
        PercentType w2 = new PercentType(new BigDecimal(white2).divide(new BigDecimal(255.0), 3, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100.0)));

        // Range: 0x00 .. 0x1F. Speed is inversed
        BigDecimal ps = new BigDecimal(programSpeed).divide(new BigDecimal(0x1f), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100.0));
        PercentType e = new PercentType(new BigDecimal(100.0).subtract(ps));

        StringType p = new StringType(Integer.toString(program));

        return new LEDStateDTO(power, hsbType, w, w2, p, e);
    }

    public LEDStateDTO withColor(HSBType color) {
        return new LEDStateDTO(power, color, this.getWhite(), this.getWhite2(), this.getProgram(),
                this.getProgramSpeed());
    }

    public LEDStateDTO withBrightness(PercentType brightness) {
        return new LEDStateDTO(power, hsbType.getHue(), hsbType.getSaturation(), brightness, this.getWhite(),
                this.getWhite2(), this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedBrightness(int step) {
        int brightness = hsbType.getBrightness().intValue();
        brightness = max(min(brightness + step, 0), 100);

        return withBrightness(new PercentType(brightness));
    }

    public LEDStateDTO withWhite(PercentType white) {
        return new LEDStateDTO(power, hsbType, white, this.getWhite2(), this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedWhite(int step) {
        int white = this.getWhite().intValue();
        white = max(min(white + step, 0), 100);

        return withWhite(new PercentType(white));
    }

    public LEDStateDTO withWhite2(PercentType white2) {
        return new LEDStateDTO(power, hsbType, this.getWhite(), white2, this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedWhite2(int step) {
        int white = this.getWhite().intValue();
        white = max(min(white + step, 0), 100);

        return withWhite(new PercentType(white));
    }

    public LEDStateDTO withProgram(StringType program) {
        return new LEDStateDTO(power, hsbType, this.getWhite(), this.getWhite2(), program, this.getProgramSpeed());
    }

    public LEDStateDTO withoutProgram() {
        return withProgram(new StringType(String.valueOf(0x61)));
    }

    public LEDStateDTO withProgramSpeed(PercentType programSpeed) {
        return new LEDStateDTO(power, hsbType, this.getWhite(), this.getWhite2(), this.getProgram(), programSpeed);
    }

    public LEDStateDTO withIncrementedProgramSpeed(int step) {
        int programSpeed = this.getProgramSpeed().intValue();
        programSpeed = max(min(programSpeed + step, 0), 100);

        return withProgramSpeed(new PercentType(programSpeed));
    }

    public LEDStateDTO withPower(OnOffType power) {
        return new LEDStateDTO(power, hsbType, this.getWhite(), this.getWhite2(), this.getProgram(), getProgramSpeed());
    }

    public int getRGB() {
        return hsbType.getRGB();
    }

    public HSBType getHSB() {
        return hsbType;
    }

    public OnOffType getPower() {
        return power;
    }
}

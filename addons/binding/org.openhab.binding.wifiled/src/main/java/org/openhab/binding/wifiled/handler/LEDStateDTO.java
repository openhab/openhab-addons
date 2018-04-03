/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import org.eclipse.smarthome.core.library.types.*;

import java.awt.*;
import java.math.BigDecimal;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * The {@link LEDStateDTO} class holds the data and the settings for a LED device (i.e. the selected colors, the running
 * program, etc.).
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis
 */
public class LEDStateDTO extends HSBType {

    private static final long serialVersionUID = 1L;

    protected OnOffType  power;
    protected BigDecimal white;
    protected BigDecimal white2;
    protected StringType program;
    protected BigDecimal programSpeed;

    public LEDStateDTO(OnOffType power, DecimalType hue, PercentType saturation, PercentType brightness, PercentType white,
                       PercentType white2, StringType program, PercentType programSpeed) {
        super(hue, saturation, brightness);
        this.power = power;
        this.white = white.toBigDecimal();
        this.white2 = white2.toBigDecimal();
        this.program = program;
        this.programSpeed = programSpeed.toBigDecimal();
    }

    public PercentType getWhite() {
        return new PercentType(white);
    }

    public PercentType getWhite2() {
        return new PercentType(white2);
    }

    public StringType getProgram() {
        return new StringType(program.toString());
    }

    public PercentType getProgramSpeed() {
        return new PercentType(programSpeed);
    }

    @Override
    public String toString() {
        return power + "," + getHue() + "," + getSaturation() + "," + getBrightness() + "," + getWhite() + "," + getWhite2() + " [" + getProgram() + ","
                + getProgramSpeed() + "]";
    }

    public static LEDStateDTO valueOf(int state, int program, int programSpeed, int red, int green, int blue, int white, int white2) {

        OnOffType power = (state & 0x01) != 0 ? OnOffType.ON : OnOffType.OFF;
        float[] hsv = new float[3];
        Color.RGBtoHSB(red, green, blue, hsv);
        long hue = (long) (hsv[0] * 360);
        int saturation = (int) (hsv[1] * 100);
        int brightness = (int) (hsv[2] * 100);
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        // set Brightness to 0 if state is OFF
        PercentType b = new PercentType(brightness);
        PercentType w = new PercentType(white / 255 * 100);
        PercentType w2 = new PercentType(white2 / 255 * 100);

        StringType p = new StringType(Integer.toString(program));
        PercentType e = new PercentType(100 - (programSpeed / 0x1F * 100)); // Range: 0x00 .. 0x1F. Speed is inversed

        return new LEDStateDTO(power, h, s, b, w, w2, p, e);
    }

    public LEDStateDTO withColor(HSBType color) {
        return new LEDStateDTO(power, color.getHue(), color.getSaturation(), color.getBrightness(), this.getWhite(),
            this.getWhite2(), this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withBrightness(PercentType brightness) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), brightness, this.getWhite(), this.getWhite2(),
            this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedBrightness(int step) {
        int brightness = this.getBrightness().intValue();
        brightness = max(min(brightness + step, 0), 100);

        return withBrightness(new PercentType(brightness));
    }

    public LEDStateDTO withWhite(PercentType white) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), this.getBrightness(), white, this.getWhite2(),
            this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedWhite(int step) {
        int white = this.getWhite().intValue();
        white = max(min(white + step, 0), 100);

        return withWhite(new PercentType(white));
    }

    public LEDStateDTO withWhite2(PercentType white2) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(), white2,
            this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedWhite2(int step) {
        int white = this.getWhite().intValue();
        white = max(min(white + step, 0), 100);

        return withWhite(new PercentType(white));
    }

    public LEDStateDTO withProgram(StringType program) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(), this.getWhite2(),
            program, this.getProgramSpeed());
    }

    public LEDStateDTO withoutProgram() {
        return withProgram(new StringType(String.valueOf(0x61)));
    }

    public LEDStateDTO withProgramSpeed(PercentType programSpeed) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(), this.getWhite2(),
            this.getProgram(), programSpeed);
    }

    public LEDStateDTO withIncrementedProgramSpeed(int step) {
        int programSpeed = this.getProgramSpeed().intValue();
        programSpeed = max(min(programSpeed + step, 0), 100);

        return withProgramSpeed(new PercentType(programSpeed));
    }

    public Color getColor() {
        float hue = (float) this.getHue().intValue() / 360;
        float saturation = (float) this.getSaturation().doubleValue() / 100;
        float brightness = (float) this.getBrightness().intValue() / 100;
        return new Color(Color.HSBtoRGB(hue, saturation, brightness));
    }

    public OnOffType getPower() {
        return power;
    }

    public LEDStateDTO withPower(OnOffType power) {
        return new LEDStateDTO(power, this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(), this.getWhite2(),
            this.getProgram(), getProgramSpeed());
    }

}

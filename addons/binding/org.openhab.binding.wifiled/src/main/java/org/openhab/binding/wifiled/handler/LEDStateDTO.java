/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import java.awt.Color;
import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link LEDStateDTO} class holds the data and the settings for a LED device (i.e. the selected colors, the running
 * program, etc.).
 *
 * @author Osman Basha - Initial contribution
 */
public class LEDStateDTO extends HSBType {

    private static final long serialVersionUID = 1L;

    protected BigDecimal white;
    protected StringType program;
    protected BigDecimal programSpeed;

    public LEDStateDTO(DecimalType hue, PercentType saturation, PercentType brightness, PercentType white,
            StringType program, PercentType programSpeed) {
        super(hue, saturation, brightness);
        this.white = white.toBigDecimal();
        this.program = program;
        this.programSpeed = programSpeed.toBigDecimal();
    }

    public PercentType getWhite() {
        return new PercentType(white);
    }

    public StringType getProgram() {
        return new StringType(program.toString());
    }

    public PercentType getProgramSpeed() {
        return new PercentType(programSpeed);
    }

    @Override
    public String toString() {
        return getHue() + "," + getSaturation() + "," + getBrightness() + "," + getWhite() + " [" + getProgram() + ","
                + getProgramSpeed() + "]";
    }

    public static LEDStateDTO valueOf(int state, int program, int programSpeed, int red, int green, int blue,
            int white) {

        float[] hsv = new float[3];
        Color.RGBtoHSB(red, green, blue, hsv);
        long hue = (long) (hsv[0] * 360);
        int saturation = (int) (hsv[1] * 100);
        int brightness = (int) (hsv[2] * 100);
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        // set Brightness to 0 if state is OFF
        PercentType b = ((state & 0x01) == 0) ? PercentType.ZERO : new PercentType(brightness);
        PercentType w = new PercentType(white / 255 * 100);

        StringType p = new StringType(Integer.toString(program));
        PercentType e = new PercentType(100 - (programSpeed / 0x1F * 100)); // Range: 0x00 .. 0x1F. Speed is inversed

        return new LEDStateDTO(h, s, b, w, p, e);
    }

    public LEDStateDTO withColor(HSBType color) {
        return new LEDStateDTO(color.getHue(), color.getSaturation(), color.getBrightness(), this.getWhite(),
                this.getProgram(), this.getProgramSpeed());
    }

    public LEDStateDTO withBrightness(PercentType brightness) {
        return new LEDStateDTO(this.getHue(), this.getSaturation(), brightness, this.getWhite(), this.getProgram(),
                this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedBrightness(int step) {
        int brightness = this.getBrightness().intValue();
        brightness += step;
        if (brightness > 100) {
            brightness = 100;
        } else if (brightness < 0) {
            brightness = 0;
        }

        return withBrightness(new PercentType(brightness));
    }

    public LEDStateDTO withWhite(PercentType white) {
        return new LEDStateDTO(this.getHue(), this.getSaturation(), this.getBrightness(), white, this.getProgram(),
                this.getProgramSpeed());
    }

    public LEDStateDTO withIncrementedWhite(int step) {
        int white = this.getWhite().intValue();
        white += step;
        if (white > 100) {
            white = 100;
        } else if (white < 0) {
            white = 0;
        }

        return withWhite(new PercentType(white));
    }

    public LEDStateDTO withProgram(StringType program) {
        return new LEDStateDTO(this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(), program,
                this.getProgramSpeed());
    }

    public LEDStateDTO withoutProgram() {
        return withProgram(new StringType(String.valueOf(0x61)));
    }

    public LEDStateDTO withProgramSpeed(PercentType programSpeed) {
        return new LEDStateDTO(this.getHue(), this.getSaturation(), this.getBrightness(), this.getWhite(),
                this.getProgram(), programSpeed);
    }

    public LEDStateDTO withIncrementedProgramSpeed(int step) {
        int programSpeed = this.getProgramSpeed().intValue();
        programSpeed += step;
        if (programSpeed > 100) {
            programSpeed = 100;
        } else if (programSpeed < 0) {
            programSpeed = 0;
        }

        return withProgramSpeed(new PercentType(programSpeed));
    }

    public Color getColor() {
        float hue = (float) this.getHue().intValue() / 360;
        float saturation = (float) this.getSaturation().intValue() / 100;
        float brightness = (float) this.getBrightness().intValue() / 100;
        return new Color(Color.HSBtoRGB(hue, saturation, brightness));
    }

}

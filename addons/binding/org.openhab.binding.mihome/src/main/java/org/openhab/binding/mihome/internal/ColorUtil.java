/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal;

/**
 * Color utilities for conversions
 *
 * @author Patrick Boos - Initial contribution
 */
public class ColorUtil {
    /**
     * Convert color temperature in Kelvins to RGB color for AWT
     *
     * @param temperature the temperature
     * @return ready to use color object
     */
    public static int getRGBFromK(int temperature) {
        // Used this: https://gist.github.com/paulkaplan/5184275 at the beginning
        // based on http://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c
        // this answer: http://stackoverflow.com/a/24856307
        // (so, just interpretation of pseudocode in Java)

        double x = temperature / 1000.0;
        if (x > 40) {
            x = 40;
        }
        double red;
        double green;
        double blue;

        // R
        if (temperature < 6527) {
            red = 1;
        } else {
            double[] redpoly = { 4.93596077e0, -1.29917429e0, 1.64810386e-01, -1.16449912e-02, 4.86540872e-04,
                    -1.19453511e-05, 1.59255189e-07, -8.89357601e-10 };
            red = poly(redpoly, x);
        }
        // G
        if (temperature < 850) {
            green = 0;
        } else if (temperature <= 6600) {
            double[] greenpoly = { -4.95931720e-01, 1.08442658e0, -9.17444217e-01, 4.94501179e-01, -1.48487675e-01,
                    2.49910386e-02, -2.21528530e-03, 8.06118266e-05 };
            green = poly(greenpoly, x);
        } else {
            double[] greenpoly = { 3.06119745e0, -6.76337896e-01, 8.28276286e-02, -5.72828699e-03, 2.35931130e-04,
                    -5.73391101e-06, 7.58711054e-08, -4.21266737e-10 };

            green = poly(greenpoly, x);
        }
        // B
        if (temperature < 1900) {
            blue = 0;
        } else if (temperature < 6600) {
            double[] bluepoly = { 4.93997706e-01, -8.59349314e-01, 5.45514949e-01, -1.81694167e-01, 4.16704799e-02,
                    -6.01602324e-03, 4.80731598e-04, -1.61366693e-05 };
            blue = poly(bluepoly, x);
        } else {
            blue = 1;
        }

        red = clamp(red, 0, 1);
        blue = clamp(blue, 0, 1);
        green = clamp(green, 0, 1);
        return (int) (red * 255 * 256 * 256) + (int) (green * 255 * 256) + (int) (blue * 255);
    }

    public static double poly(double[] coefficients, double x) {
        double result = coefficients[0];
        double xn = x;
        for (int i = 1; i < coefficients.length; i++) {
            result += xn * coefficients[i];
            xn *= x;

        }
        return result;
    }

    public static double clamp(double x, double min, double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}

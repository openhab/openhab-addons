/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Draws the vacuum map file to an image
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class RRMapDraw {

    private static final Color COLOR_MAP_INSIDE = new Color(32, 115, 185);
    private static final Color COLOR_MAP_OUTSIDE = new Color(19, 87, 148);
    private static final Color COLOR_MAP_WALL = new Color(100, 196, 254);
    private static final Color COLOR_GREY_WALL = new Color(93, 109, 126);
    private static final Color COLOR_PATH = new Color(147, 194, 238);
    private static final Color COLOR_ZONES = new Color(0xAD, 0xD8, 0xFF, 0x8F);
    private static final Color COLOR_NO_GO_ZONES = new Color(255, 33, 55, 127);
    private static final Color COLOR_CHARGER_HALO = new Color(0x66, 0xfe, 0xda, 0x7f);
    private static final Color COLOR_ROBO = new Color(75, 235, 149);
    private static final Color COLOR_SCAN = new Color(0xDF, 0xDF, 0xDF);
    private static final Color ROOM1 = new Color(240, 178, 122);
    private static final Color ROOM2 = new Color(133, 193, 233);
    private static final Color ROOM3 = new Color(217, 136, 128);
    private static final Color ROOM4 = new Color(52, 152, 219);
    private static final Color ROOM5 = new Color(205, 97, 85);
    private static final Color ROOM6 = new Color(243, 156, 18);
    private static final Color ROOM7 = new Color(88, 214, 141);
    private static final Color ROOM8 = new Color(245, 176, 65);
    private static final Color ROOM9 = new Color(0xFc, 0xD4, 0x51);
    private static final Color ROOM10 = new Color(72, 201, 176);
    private static final Color ROOM11 = new Color(84, 153, 199);
    private static final Color ROOM12 = new Color(133, 193, 233);
    private static final Color ROOM13 = new Color(245, 176, 65);
    private static final Color ROOM14 = new Color(82, 190, 128);
    private static final Color ROOM15 = new Color(72, 201, 176);
    private static final Color ROOM16 = new Color(165, 105, 189);
    private static final Color[] ROOM_COLORS = { ROOM1, ROOM2, ROOM3, ROOM4, ROOM5, ROOM6, ROOM7, ROOM8, ROOM9, ROOM10,
            ROOM11, ROOM12, ROOM13, ROOM14, ROOM15, ROOM16 };
    private boolean multicolor = false;

    Dimension size = new Dimension();
    private RRMapFileParser rmfp;

    public RRMapDraw(RRMapFileParser rmfp) {
        this.rmfp = rmfp;
    }

    public void setRRFileDecoder(RRMapFileParser rmfp) {
        this.rmfp = rmfp;
    }

    public int getWidth() {
        return rmfp.getImgWidth();
    }

    public int getHeight() {
        return rmfp.getImgHeight();
    }

    /**
     * load Gzipped RR inputstream
     *
     * @throws IOException
     */
    public static RRMapDraw loadImage(InputStream is) throws IOException {
        byte[] inputdata = RRMapFileParser.readRRMapFile(is);
        RRMapFileParser rf = new RRMapFileParser(inputdata);
        return new RRMapDraw(rf);
    }

    /**
     * load Gzipped RR file
     *
     * @throws IOException
     */
    public static RRMapDraw loadImage(File file) throws IOException {
        return loadImage(new FileInputStream(file));
    }

    /**
     * draws the map from the individual pixels
     */
    private void drawMap(Graphics2D g2d, float scale) {
        Stroke stroke = new BasicStroke(1.1f * scale);
        g2d.setStroke(stroke);
        for (int y = 0; y < rmfp.getImgHeight() - 1; y++) {
            for (int x = 0; x < rmfp.getImgWidth() + 1; x++) {
                byte walltype = rmfp.getImage()[x + rmfp.getImgWidth() * y];
                switch (walltype & 0xFF) {
                    case 0x00:
                        g2d.setColor(COLOR_MAP_OUTSIDE);
                        break;
                    case 0x01:
                        g2d.setColor(COLOR_MAP_WALL);
                        break;
                    case 0xFF:
                        g2d.setColor(COLOR_MAP_INSIDE);
                        break;
                    case 0x07:
                        g2d.setColor(COLOR_SCAN);
                        break;
                    default:
                        int obstacle = (walltype & 0x07);
                        int mapId = (walltype & 0xFF) >>> 3;
                        switch (obstacle) {
                            case 0:
                                g2d.setColor(COLOR_GREY_WALL);
                                break;
                            case 1:
                                g2d.setColor(Color.BLACK);
                                break;
                            case 7:
                                g2d.setColor(ROOM_COLORS[Math.round(mapId / 2)]);
                                multicolor = true;
                                break;
                            default:
                                g2d.setColor(Color.WHITE);
                                break;
                        }
                }
                float xPos = scale * (rmfp.getImgWidth() - x);
                float yP = scale * y;
                g2d.draw(new Line2D.Float(xPos, yP, xPos, yP));
            }
        }
    }

    /**
     * draws the vacuum path
     *
     * @param scale
     */
    private void drawPath(Graphics2D g2d, float scale) {
        Stroke stroke = new BasicStroke(0.5f * scale);
        g2d.setStroke(stroke);
        for (Integer pathType : rmfp.getPaths().keySet()) {
            switch (pathType) {
                case RRMapFileParser.PATH:
                    if (!multicolor) {
                        g2d.setColor(COLOR_PATH);
                    } else {
                        g2d.setColor(Color.WHITE);
                    }
                    break;
                case RRMapFileParser.GOTO_PATH:
                    g2d.setColor(Color.GREEN);
                    break;
                case RRMapFileParser.GOTO_PREDICTED_PATH:
                    g2d.setColor(Color.YELLOW);
                    break;
                default:
                    g2d.setColor(Color.CYAN);
            }
            float prvX = 0;
            float prvY = 0;
            for (Float[] point : rmfp.getPaths().get(pathType)) {
                float x = point[0] * scale;
                float y = point[1] * scale;
                if (prvX > 1) {
                    g2d.draw(new Line2D.Float(prvX, prvY, x, y));
                }
                prvX = x;
                prvY = y;
            }
        }
    }

    private void drawZones(Graphics2D g2d, float scale) {
        for (Float[] point : rmfp.getZones()) {
            float x = point[0] * scale;
            float y = point[1] * scale;
            float x1 = point[2] * scale;
            float y1 = point[3] * scale;
            float sx = Math.min(x, x1);
            float w = Math.max(x, x1) - sx;
            float sy = Math.min(y, y1);
            float h = Math.max(y, y1) - sy;
            g2d.setColor(COLOR_ZONES);
            g2d.fill(new Rectangle2D.Float(sx, sy, w, h));
        }
    }

    private void drawNoGo(Graphics2D g2d, float scale) {
        for (Integer area : rmfp.getAreas().keySet()) {
            for (Float[] point : rmfp.getAreas().get(area)) {
                float x = point[0] * scale;
                float y = point[1] * scale;
                float x1 = point[2] * scale;
                float y1 = point[3] * scale;
                float x2 = point[4] * scale;
                float y2 = point[5] * scale;
                float x3 = point[6] * scale;
                float y3 = point[7] * scale;
                Path2D noGo = new Path2D.Float();
                noGo.moveTo(x, y);
                noGo.lineTo(x1, y1);
                noGo.lineTo(x2, y2);
                noGo.lineTo(x3, y3);
                noGo.lineTo(x, y);
                g2d.setColor(COLOR_NO_GO_ZONES);
                g2d.fill(noGo);
                g2d.setColor(area == 9 ? Color.RED : Color.WHITE);
                g2d.draw(noGo);
            }
        }
    }

    private void drawWalls(Graphics2D g2d, float scale) {
        Stroke stroke = new BasicStroke(3 * scale);
        g2d.setStroke(stroke);
        for (Float[] point : rmfp.getWalls()) {
            float x = point[0] * scale;
            float y = point[1] * scale;
            float x1 = point[2] * scale;
            float y1 = point[3] * scale;
            g2d.setColor(Color.RED);
            g2d.draw(new Line2D.Float(x, y, x1, y1));
        }
    }

    private void drawRobo(Graphics2D g2d, float scale) {
        float radius = 3 * scale;
        Stroke stroke = new BasicStroke(2 * scale);
        g2d.setStroke(stroke);
        g2d.setColor(COLOR_CHARGER_HALO);
        drawCircle(g2d, rmfp.getChargerX() * scale, rmfp.getChargerY() * scale, radius);
        drawCenteredImg(g2d, scale / 8,
                "iVBORw0KGgoAAAANSUhEUgAAACoAAAAqCAMAAADyHTlpAAADAFBMVEVHcExF5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o5F5o7////w/fa69tVm6qL1/vlt66br/PNf6p7+//7o/PGE7rR37ay39dOL77nM+OCh8sbB99n2/vlH5o9e6Z2c8sPk++/j++5s66Vj6qBg6p6i88dn66Nq66VY6Zq99td67a6S8L30/vjb+ul57a2C7rNW6Jiv9M5Q55UAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABmUTLUAAAALnRSTlMAJPkvN+wI+4DP1eoxP47t7v4GrpmcB6+Ptj7w+vMJ9Lm11DIlMI0m0DUuM/G06wfbVgAAAfxJREFUeNqNletzmzAMwGVhXg0NTbLkbtl12fvW/f//ya7Xy7h82Nruwa5p8yhhgPGAFrCN6VWfbOmHZEtCNkCSMUX/ZZ6S4SvkrneQbETceE66ELY/aLzXo8bru1M5Blw6IWvt9cIa8LmvkOAPbqcJU7wS5yPoJYi55PUs+tBDwmSNuYBSew69MqVRxWIV3R7BEzKySe3VsjTRrUWSNtneInu41vCNhuTZ0X27jb9VBzD+aUmYCPvvXyqvb4+1pBuJGv618OrddcjTgoSppLryCtRVqwn0Oqtz037tAp2ZHTKvypMqZ5ohnejJ0UEpGsW1ngR2oxjW6OtJuZGrLsOdtJ/XJGQqukPp9M5NTYJpVNKmLKVMJHnSrB+z3RaXYQ9Zpyhv12i062GXJHH7a1GhAuZjCCvWkIVV6JVrVsmotpOxQMIxbjrNUh+D4C9RvcFxB61rgHJqTczUAsKRlsxdDNVrfwp1JPwJEYa6+B0SZkUDr7ayrswYnank9rw0mEtJWUCU/FbI5WXlI7BF5eefBdkpm80ewp0EgjKONWRyUp6qHLlk9b7tbdIlVxFvxhu3nOa/4EwlN7e58F9QctY73S541qSmvPYi6CODd5k84It5e/VCAy7zKFTfArYHMuiQf3c79rzHyF/1vVvF0E3TwcHyYJ+496Ypj5P/uAmtfUpJqE0AAAAASUVORK5CYII=",
                rmfp.getChargerX() * scale, rmfp.getChargerY() * scale);
        radius = 3 * scale;
        g2d.setColor(COLOR_ROBO);
        drawCircle(g2d, rmfp.getRoboX() * scale, rmfp.getRoboY() * scale, radius);
        if (scale > 1.5) {
            drawCenteredImg(g2d, scale / 15,
                    "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",
                    rmfp.getRoboX() * scale, rmfp.getRoboY() * scale);
        }
    }

    private void drawCircle(Graphics2D g2d, float x, float y, float radius) {
        g2d.draw(new Ellipse2D.Double(x - radius, y - radius, 2.0 * radius, 2.0 * radius));
    }

    private void drawCenteredImg(Graphics2D g2d, float scale, String imgData, float x, float y) {
        try {
            BufferedImage addImg = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imgData)));
            int xpos = Math.round(x - (addImg.getWidth() / 2 * scale));
            int ypos = Math.round(y - (addImg.getHeight() / 2 * scale));
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            g2d.drawImage(addImg, scaleOp, xpos, ypos);
        } catch (IOException e) {
            // ignore
        }
    }

    private void drawGoTo(Graphics2D g2d, float scale) {
        float x = rmfp.getGotoX() * scale;
        float y = rmfp.getGotoY() * scale;
        if (!(x == 0 && y == 0)) {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.YELLOW);
            int x3[] = { (int) x, (int) (x - 2 * scale), (int) (x + 2 * scale) };
            int y3[] = { (int) y, (int) (y - 5 * scale), (int) (y - 5 * scale) };
            g2d.fill(new Polygon(x3, y3, 3));

        }
    }

    private void drawOpenHabRocks(Graphics2D g2d, int width, int height, float scale) {
        // easter egg gift :
        int offset = 5;
        int textPos = 55;
        try {
            BufferedImage ohLogo = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(
                    "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")));
            textPos = (int) (ohLogo.getWidth() * scale / 2 + offset);
            AffineTransform at = new AffineTransform();
            at.scale(scale / 2, scale / 2);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            g2d.drawImage(ohLogo, scaleOp, offset, height - (int) (ohLogo.getHeight() * scale / 2) - offset);
        } catch (IOException e) {
            // no joy
        }
        Font font = new Font("TimesRoman", Font.BOLD, 14);
        g2d.setFont(font);
        String message = "Openhab rocks your Xiaomi vacuum!";
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(message);
        if ((stringWidth + textPos) > rmfp.getImgWidth() * scale) {
            font = new Font("TimesRoman", Font.BOLD,
                    (int) Math.floor(14 * (rmfp.getImgWidth() * scale - textPos - offset) / stringWidth));
            g2d.setFont(font);
        }
        int stringHeight = fontMetrics.getAscent();
        g2d.setPaint(Color.white);
        g2d.drawString(message, textPos, height - offset - stringHeight / 2);
    }

    public BufferedImage getImage(float scale) {
        int width = (int) Math.floor(rmfp.getImgWidth() * scale);
        int height = (int) Math.floor(rmfp.getImgHeight() * scale);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = bi.createGraphics();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
        tx.translate(-width, -height);
        g2d.setTransform(tx);
        drawMap(g2d, scale);
        drawZones(g2d, scale);
        drawNoGo(g2d, scale);
        drawWalls(g2d, scale);
        drawPath(g2d, scale);
        drawRobo(g2d, scale);
        drawGoTo(g2d, scale);
        g2d = bi.createGraphics();
        drawOpenHabRocks(g2d, width, height, scale);
        return bi;

    }

    public boolean writePic(String filename, String formatName, float scale) throws IOException {
        return ImageIO.write(getImage(scale), formatName, new File(filename));
    }

    @Override
    public String toString() {
        return rmfp.toString();
    }
}

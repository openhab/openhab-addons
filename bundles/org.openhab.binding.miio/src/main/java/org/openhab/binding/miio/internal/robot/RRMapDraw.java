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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws the vacuum map file to an image
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class RRMapDraw {

    private static final int MAP_OUTSIDE = 0x00;
    private static final int MAP_WALL = 0x01;
    private static final int MAP_INSIDE = 0xFF;
    private static final int MAP_SCAN = 0x07;
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
    private final @Nullable Bundle bundle = FrameworkUtil.getBundle(getClass());
    private boolean multicolor = false;
    private final RRMapFileParser rmfp;

    private final Logger logger = LoggerFactory.getLogger(RRMapDraw.class);

    public RRMapDraw(RRMapFileParser rmfp) {
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
                    case MAP_OUTSIDE:
                        g2d.setColor(COLOR_MAP_OUTSIDE);
                        break;
                    case MAP_WALL:
                        g2d.setColor(COLOR_MAP_WALL);
                        break;
                    case MAP_INSIDE:
                        g2d.setColor(COLOR_MAP_INSIDE);
                        break;
                    case MAP_SCAN:
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
            for (float[] point : rmfp.getPaths().get(pathType)) {
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
        for (float[] point : rmfp.getZones()) {
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
            for (float[] point : rmfp.getAreas().get(area)) {
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
        for (float[] point : rmfp.getWalls()) {
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
        drawCenteredImg(g2d, scale / 8, "charger.png", rmfp.getChargerX() * scale, rmfp.getChargerY() * scale);
        radius = 3 * scale;
        g2d.setColor(COLOR_ROBO);
        drawCircle(g2d, rmfp.getRoboX() * scale, rmfp.getRoboY() * scale, radius);
        if (scale > 1.5) {
            drawCenteredImg(g2d, scale / 15, "robo.png", rmfp.getRoboX() * scale, rmfp.getRoboY() * scale);
        }
    }

    private void drawCircle(Graphics2D g2d, float x, float y, float radius) {
        g2d.draw(new Ellipse2D.Double(x - radius, y - radius, 2.0 * radius, 2.0 * radius));
    }

    private void drawCenteredImg(Graphics2D g2d, float scale, String imgFile, float x, float y) {
        URL image = getImageUrl(imgFile);
        try {
            if (image != null) {
                BufferedImage addImg = ImageIO.read(image);
                int xpos = Math.round(x - (addImg.getWidth() / 2 * scale));
                int ypos = Math.round(y - (addImg.getHeight() / 2 * scale));
                AffineTransform at = new AffineTransform();
                at.scale(scale, scale);
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                g2d.drawImage(addImg, scaleOp, xpos, ypos);
            } else {
                logger.debug("Error loading image {}: File not be found.", imgFile);
            }
        } catch (IOException e) {
            logger.debug("Error loading image {}: {}", image, e.getMessage());
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
        // easter egg gift
        int offset = 5;
        int textPos = 55;
        URL image = getImageUrl("ohlogo.png");
        try {
            if (image != null) {
                BufferedImage ohLogo = ImageIO.read(image);
                textPos = (int) (ohLogo.getWidth() * scale / 2 + offset * scale);
                AffineTransform at = new AffineTransform();
                at.scale(scale / 2, scale / 2);
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                g2d.drawImage(ohLogo, scaleOp, offset,
                        height - (int) (ohLogo.getHeight() * scale / 2) - (int) (offset * scale));
            } else {
                logger.debug("Error loading image ohlogo.png: File not be found.");
            }
        } catch (IOException e) {
            logger.debug("Error loading image ohlogo.png:: {}", e.getMessage());
        }
        Font font = new Font("Helvetica", Font.BOLD, 14);
        g2d.setFont(font);
        String message = "Openhab rocks your Xiaomi vacuum!";
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(message);
        if ((stringWidth + textPos) > rmfp.getImgWidth() * scale) {
            font = new Font("Helvetica ", Font.BOLD,
                    (int) Math.floor(14 * (rmfp.getImgWidth() * scale - textPos - offset * scale) / stringWidth));
            g2d.setFont(font);
        }
        int stringHeight = fontMetrics.getAscent();
        g2d.setPaint(Color.white);
        g2d.drawString(message, textPos, height - offset * scale - stringHeight / 2);
    }

    private @Nullable URL getImageUrl(String image) {
        if (bundle != null) {
            return bundle.getEntry("images/" + image);
        }
        try {
            File fn = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator
                    + "images" + File.separator + image);
            return fn.toURI().toURL();
        } catch (MalformedURLException | SecurityException e) {
            logger.debug("Could create URL for {}: {}", image, e.getMessage());
            return null;
        }
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

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.awt.GraphicsEnvironment;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    private static final float MM = 50.0f;

    private static final int MAP_OUTSIDE = 0x00;
    private static final int MAP_WALL = 0x01;
    private static final int MAP_INSIDE = 0xFF;
    private static final int MAP_SCAN = 0x07;

    private final @Nullable Bundle bundle = FrameworkUtil.getBundle(getClass());
    private final RRMapFileParser rmfp;
    private final Logger logger = LoggerFactory.getLogger(RRMapDraw.class);

    private RRMapDrawOptions drawOptions = new RRMapDrawOptions();
    private boolean multicolor = false;
    private int firstX = 0;
    private int lastX = 0;
    private int firstY = 0;
    private int lastY = 0;

    public RRMapDraw(RRMapFileParser rmfp) {
        this.rmfp = rmfp;
    }

    public int getWidth() {
        return rmfp.getImgWidth();
    }

    public int getHeight() {
        return rmfp.getImgHeight();
    }

    public void setDrawOptions(RRMapDrawOptions options) {
        this.drawOptions = options;
    }

    public RRMapDrawOptions getDrawOptions() {
        return drawOptions;
    }

    public RRMapFileParser getMapParseDetails() {
        return this.rmfp;
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
        Set<Integer> roomIds = new HashSet<Integer>();
        g2d.setStroke(stroke);
        for (int y = 0; y < rmfp.getImgHeight() - 1; y++) {
            for (int x = 0; x < rmfp.getImgWidth() + 1; x++) {
                byte walltype = rmfp.getImage()[x + rmfp.getImgWidth() * y];
                switch (walltype & 0xFF) {
                    case MAP_OUTSIDE:
                        g2d.setColor(drawOptions.getColorMapOutside());
                        break;
                    case MAP_WALL:
                        g2d.setColor(drawOptions.getColorMapWall());
                        break;
                    case MAP_INSIDE:
                        g2d.setColor(drawOptions.getColorMapInside());
                        break;
                    case MAP_SCAN:
                        g2d.setColor(drawOptions.getColorScan());
                        break;
                    default:
                        int obstacle = (walltype & 0x07);
                        int mapId = (walltype & 0xFF) >>> 3;
                        switch (obstacle) {
                            case 0:
                                g2d.setColor(drawOptions.getColorGreyWall());
                                break;
                            case 1:
                                g2d.setColor(Color.BLACK);
                                break;
                            case 7:
                                g2d.setColor(drawOptions.getRoomColors()[mapId % 15]);
                                roomIds.add(mapId);
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
        if (logger.isDebugEnabled() && !roomIds.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Integer r : roomIds) {
                sb.append(" " + r.toString());
            }
            logger.debug("Identified rooms in map:{}", sb.toString());
        }
    }

    /**
     * draws the carpet map
     */
    private void drawCarpetMap(Graphics2D g2d, float scale) {
        if (rmfp.getCarpetMap().length == 0) {
            return;
        }
        Stroke stroke = new BasicStroke(1.1f * scale);
        g2d.setStroke(stroke);
        for (int y = 0; y < rmfp.getImgHeight() - 1; y++) {
            for (int x = 0; x < rmfp.getImgWidth() + 1; x++) {
                int carpetType = rmfp.getCarpetMap()[x + rmfp.getImgWidth() * y];
                switch (carpetType) {
                    case 0:
                        // ignore
                        break;
                    default:
                        g2d.setColor(drawOptions.getColorCarpet());
                        float xPos = scale * (rmfp.getImgWidth() - x);
                        float yP = scale * y;
                        g2d.draw(new Line2D.Float(xPos, yP, xPos, yP));
                        break;
                }
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
        for (Entry<Integer, ArrayList<float[]>> path : rmfp.getPaths().entrySet()) {
            Integer pathType = path.getKey();
            switch (pathType) {
                case RRMapFileParser.PATH:
                    if (!multicolor) {
                        g2d.setColor(drawOptions.getColorPath());
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
            for (float[] point : path.getValue()) {
                float x = toXCoord(point[0]) * scale;
                float y = toYCoord(point[1]) * scale;
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
            float x = toXCoord(point[0]) * scale;
            float y = toYCoord(point[1]) * scale;
            float x1 = toXCoord(point[2]) * scale;
            float y1 = toYCoord(point[3]) * scale;
            float sx = Math.min(x, x1);
            float w = Math.max(x, x1) - sx;
            float sy = Math.min(y, y1);
            float h = Math.max(y, y1) - sy;
            g2d.setColor(drawOptions.getColorZones());
            g2d.fill(new Rectangle2D.Float(sx, sy, w, h));
        }
    }

    private void drawNoGo(Graphics2D g2d, float scale) {
        for (Map.Entry<Integer, ArrayList<float[]>> area : rmfp.getAreas().entrySet()) {
            for (float[] point : area.getValue()) {
                float x = toXCoord(point[0]) * scale;
                float y = toYCoord(point[1]) * scale;
                float x1 = toXCoord(point[2]) * scale;
                float y1 = toYCoord(point[3]) * scale;
                float x2 = toXCoord(point[4]) * scale;
                float y2 = toYCoord(point[5]) * scale;
                float x3 = toXCoord(point[6]) * scale;
                float y3 = toYCoord(point[7]) * scale;
                Path2D noGo = new Path2D.Float();
                noGo.moveTo(x, y);
                noGo.lineTo(x1, y1);
                noGo.lineTo(x2, y2);
                noGo.lineTo(x3, y3);
                noGo.lineTo(x, y);
                g2d.setColor(drawOptions.getColorNoGoZones());
                g2d.fill(noGo);
                g2d.setColor(area.getKey() == 9 ? Color.RED : Color.WHITE);
                g2d.draw(noGo);
            }
        }
    }

    private void drawWalls(Graphics2D g2d, float scale) {
        Stroke stroke = new BasicStroke(3 * scale);
        g2d.setStroke(stroke);
        for (float[] point : rmfp.getWalls()) {
            float x = toXCoord(point[0]) * scale;
            float y = toYCoord(point[1]) * scale;
            float x1 = toXCoord(point[2]) * scale;
            float y1 = toYCoord(point[3]) * scale;
            g2d.setColor(Color.RED);
            g2d.draw(new Line2D.Float(x, y, x1, y1));
        }
    }

    private void drawRobo(Graphics2D g2d, float scale) {
        float radius = 3 * scale;
        Stroke stroke = new BasicStroke(2 * scale);
        g2d.setStroke(stroke);
        g2d.setColor(drawOptions.getColorChargerHalo());
        final float chargerX = toXCoord(rmfp.getChargerX()) * scale;
        final float chargerY = toYCoord(rmfp.getChargerY()) * scale;
        drawCircle(g2d, chargerX, chargerY, radius, false);
        drawCenteredImg(g2d, scale / 8, "charger.png", chargerX, chargerY);
        radius = 3 * scale;
        g2d.setColor(drawOptions.getColorRobo());
        final float roboX = toXCoord(rmfp.getRoboX()) * scale;
        final float roboY = toYCoord(rmfp.getRoboY()) * scale;
        drawCircle(g2d, roboX, roboY, radius, false);
        if (scale > 1.5) {
            drawCenteredImg(g2d, scale / 15, "robo.png", roboX, roboY);
        }
    }

    private void drawObstacles(Graphics2D g2d, float scale) {
        float radius = 2 * scale;
        Stroke stroke = new BasicStroke(3 * scale);
        g2d.setStroke(stroke);
        g2d.setColor(Color.MAGENTA);

        Map<Integer, ArrayList<int[]>> obstacleMap = rmfp.getObstacles();
        for (ArrayList<int[]> obstacles : obstacleMap.values()) {
            obstacles.forEach(obstacle -> {
                final float obstacleX = toXCoord(obstacle[0]) * scale;
                final float obstacleY = toYCoord(obstacle[1]) * scale;
                drawCircle(g2d, obstacleX, obstacleY, radius, true);
                if (scale > 1.0) {
                    drawCenteredImg(g2d, scale / 3, "obstacle-" + obstacle[2] + ".png", obstacleX, obstacleY + 15);
                }
            });
        }
    }

    private void drawCircle(Graphics2D g2d, float x, float y, float radius, boolean fill) {
        Ellipse2D.Double circle = new Ellipse2D.Double(x - radius, y - radius, 2.0 * radius, 2.0 * radius);
        if (fill) {
            g2d.fill(circle);
        } else {
            g2d.draw(circle);
        }
    }

    private void drawCenteredImg(Graphics2D g2d, float scale, String imgFile, float x, float y) {
        URL image = getImageUrl(imgFile);
        try {
            if (image != null) {
                BufferedImage addImg = ImageIO.read(image);
                int xpos = Math.round(x + (addImg.getWidth() / 2 * scale));
                int ypos = Math.round(y + (addImg.getHeight() / 2 * scale));
                AffineTransform at = new AffineTransform();
                at.scale(-scale, -scale);
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
        float x = toXCoord(rmfp.getGotoX()) * scale;
        float y = toYCoord(rmfp.getGotoY()) * scale;
        if (!(x == 0 && y == 0)) {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.YELLOW);
            int[] x3 = { (int) x, (int) (x - 2 * scale), (int) (x + 2 * scale) };
            int[] y3 = { (int) y, (int) (y - 5 * scale), (int) (y - 5 * scale) };
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
        if (drawOptions.getText().isBlank()) {
            return;
        }
        String fontName = getAvailableFont("Helvetica,Arial,Roboto,Verdana,Times,Serif,Dialog".split(","));
        if (fontName == null) {
            return; // no available fonts to draw text
        }
        int fz = (int) (drawOptions.getTextFontSize() * scale);
        Font font = new Font(fontName, Font.BOLD, fz);
        g2d.setFont(font);
        String message = drawOptions.getText();
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(message);
        if ((stringWidth + textPos) > width) {
            int fzn = (int) Math.floor(((float) (width - textPos) / stringWidth) * fz);
            font = new Font(fontName, Font.BOLD, fzn > 0 ? fzn : 1);
            g2d.setFont(font);
        }
        int stringHeight = fontMetrics.getAscent();
        g2d.setPaint(Color.white);
        g2d.drawString(message, textPos, height - offset * scale - stringHeight / 2);
    }

    private @Nullable String getAvailableFont(String[] preferedFonts) {
        final GraphicsEnvironment gEv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (gEv == null) {
            return null;
        }
        String[] fonts = gEv.getAvailableFontFamilyNames();
        if (fonts.length == 0) {
            return null;
        }
        for (int j = 0; j < preferedFonts.length; j++) {
            for (int i = 0; i < fonts.length; i++) {
                if (fonts[i].equalsIgnoreCase(preferedFonts[j])) {
                    return preferedFonts[j];
                }
            }
        }
        // Preferred fonts not available... just go with the first one
        return fonts[0];
    }

    /**
     * Finds the perimeter of the used area in the map
     */
    private void getMapArea(Graphics2D g2d, float scale) {
        int firstX = rmfp.getImgWidth();
        int lastX = 0;
        int firstY = rmfp.getImgHeight();
        int lastY = 0;
        for (int y = 0; y < rmfp.getImgHeight() - 1; y++) {
            for (int x = 0; x < rmfp.getImgWidth() + 1; x++) {
                int walltype = rmfp.getImage()[x + rmfp.getImgWidth() * y] & 0xFF;
                if (walltype > MAP_OUTSIDE) {
                    if (y < firstY) {
                        firstY = y;
                    }
                    if (y > lastY) {
                        lastY = y;
                    }
                    if (x < firstX) {
                        firstX = x;
                    }
                    if (x > lastX) {
                        lastX = x;
                    }
                }
            }
        }
        this.firstX = firstX;
        this.lastX = lastX;
        this.firstY = rmfp.getImgHeight() - lastY;
        this.lastY = rmfp.getImgHeight() - firstY;
    }

    private @Nullable URL getImageUrl(String image) {
        final Bundle bundle = this.bundle;
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

    public BufferedImage getImage() {
        return getImage(drawOptions.getScale());
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
        drawCarpetMap(g2d, scale);
        drawZones(g2d, scale);
        drawNoGo(g2d, scale);
        drawWalls(g2d, scale);
        drawPath(g2d, scale);
        drawRobo(g2d, scale);
        drawGoTo(g2d, scale);
        drawObstacles(g2d, scale);
        if (drawOptions.getCropBorder() < 0) {
            g2d = bi.createGraphics();
            if (drawOptions.isShowLogo()) {
                drawOpenHabRocks(g2d, width, height, scale);
            }
            return bi;
        }
        // crop the image to the used perimeter
        getMapArea(g2d, scale);
        int firstX = (this.firstX - drawOptions.getCropBorder()) > 0 ? this.firstX - drawOptions.getCropBorder() : 0;
        int lastX = (this.lastX + drawOptions.getCropBorder()) < rmfp.getImgWidth()
                ? this.lastX + drawOptions.getCropBorder()
                : rmfp.getImgWidth();
        int firstY = (this.firstY - drawOptions.getCropBorder()) > 0 ? this.firstY - drawOptions.getCropBorder() : 0;
        int lastY = (this.lastY + drawOptions.getCropBorder() + (int) (8 * scale)) < rmfp.getImgHeight()
                ? this.lastY + drawOptions.getCropBorder() + (int) (8 * scale)
                : rmfp.getImgHeight();
        int nwidth = (int) Math.floor((lastX - firstX) * scale);
        int nheight = (int) Math.floor((lastY - firstY) * scale);
        BufferedImage bo = new BufferedImage(nwidth, nheight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D crop = bo.createGraphics();
        crop.transform(AffineTransform.getTranslateInstance(-firstX * scale, -firstY * scale));
        crop.drawImage(bi, 0, 0, null);
        if (drawOptions.isShowLogo()) {
            crop = bo.createGraphics();
            drawOpenHabRocks(crop, nwidth, nheight, scale * .75f);
        }
        return bo;
    }

    public boolean writePic(String filename, String formatName, float scale) throws IOException {
        return ImageIO.write(getImage(scale), formatName, new File(filename));
    }

    private float toXCoord(float x) {
        return rmfp.getImgWidth() + rmfp.getLeft() - (x / MM);
    }

    private float toYCoord(float y) {
        return y / MM - rmfp.getTop();
    }

    @Override
    public String toString() {
        return rmfp.toString();
    }
}

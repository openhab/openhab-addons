/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.InvalidPathException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.robot.RRMapDraw;
import org.openhab.binding.miio.internal.robot.RRMapFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offline map vacuum viewer application
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class RoboMapViewer extends JFrame {

    private static final String TITLE = "Offline Xiaomi Robot Radar Map Viewer";
    private static final float MM = 50.0f;
    private final JFrame parent;
    private final RRDrawPanel rrDrawPanel = new RRDrawPanel();
    private final JTextArea textArea = new JTextArea();
    private final JLabel statusbarL = new JLabel();
    private final JLabel statusbarR = new JLabel("1.0x");

    private float scale = 1.0f;
    private @Nullable File file;
    private @Nullable RRMapDraw rrMap;

    private final Logger logger = LoggerFactory.getLogger(RoboMapViewer.class);
    protected MapPoint fromLocation = new MapPoint();
    private static final long serialVersionUID = 2623447051590306992L;

    @Disabled
    public static void main(String[] args) {
        System.setProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
        RoboMapViewer vc = new RoboMapViewer(args);
        vc.setVisible(true);
    }

    public RoboMapViewer(String[] args) {
        super(TITLE);
        parent = this;
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea.setEditable(false);
        Container c = getContentPane();

        final JButton openButton = new JButton("Open");
        final JButton scalePButton = new JButton("+");
        final JButton scaleMButton = new JButton("-");
        final JButton previousButton = new JButton("<<");
        final JButton nextButton = new JButton(">>");
        previousButton.setToolTipText("Cyles to the previous map file");
        nextButton.setToolTipText("Cyles to the next map file");
        scaleMButton.setToolTipText("Zoom out");
        scalePButton.setToolTipText("Zoom in");

        Box north = Box.createHorizontalBox();
        north.setBackground(Color.GRAY);
        north.setForeground(Color.BLUE);
        north.add(openButton);
        north.add(previousButton);
        north.add(nextButton);
        north.add(Box.createHorizontalGlue());
        north.add(scalePButton);
        north.add(scaleMButton);
        c.add(north, "First");

        JScrollPane mapView = new JScrollPane(rrDrawPanel);
        JSplitPane middle = new JSplitPane(SwingConstants.HORIZONTAL, mapView, new JScrollPane(textArea));
        middle.setResizeWeight(.65d);
        c.add(middle, "Center");

        final JPanel statusbar = new JPanel(new BorderLayout());
        statusbar.add(BorderLayout.WEST, Box.createRigidArea(new Dimension(3, 0)));
        statusbar.add(statusbarL);
        statusbar.add(BorderLayout.EAST, statusbarR);
        c.add(statusbar, "Last");

        rrDrawPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(@Nullable MouseWheelEvent event) {
                if (event != null) {
                    if (event.getWheelRotation() < 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                }
            }
        });

        rrDrawPanel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(@Nullable MouseEvent e) {
                if (e != null) {
                    rrDrawPanel.setEndPoint(e.getX(), e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(@Nullable MouseEvent e) {
                if (e != null) {
                    MapPoint roboMouseLocation = mapCoordstoRoboCoords(localCoordtoMapCoords(e.getPoint()));
                    updateStatusLine(roboMouseLocation);
                }
            }
        });

        rrDrawPanel.addMouseListener((new MouseListener() {

            @Override
            public void mouseReleased(@Nullable MouseEvent e) {
                if (e != null) {
                    rrDrawPanel.setEndPoint(e.getX(), e.getY());
                    repaint();

                    if (rrDrawPanel.hasDrawZone()) {
                        final MapPoint endLocation = mapCoordstoRoboCoords(localCoordtoMapCoords(e.getPoint()));
                        double minX = Math.min(fromLocation.getX(), endLocation.getX());
                        double maxX = Math.max(fromLocation.getX(), endLocation.getX());
                        double minY = Math.min(fromLocation.getY(), endLocation.getY());
                        double maxY = Math.max(fromLocation.getY(), endLocation.getY());
                        textArea.append(String.format(
                                "Zone coordinates:\t%s, %s\t\tZone clean command:  app_zoned_clean[[ %.0f,%.0f,%.0f,%.0f,1 ]]\r\n",
                                endLocation, fromLocation, minX, minY, maxX, maxY));
                    } else {
                        final MapPoint pointLocation = mapCoordstoRoboCoords(localCoordtoMapCoords(e.getPoint()));
                        textArea.append(String.format(
                                "GoTo coordinates:\t[X=%.0f, Y=%.0f]\t\tGoto command:  app_goto_target[ %.0f,%.0f ]\r\n",
                                pointLocation.getX(), pointLocation.getY(), pointLocation.getX(),
                                pointLocation.getY()));
                    }
                }
            }

            @Override
            public void mousePressed(@Nullable MouseEvent e) {
                if (e != null) {
                    rrDrawPanel.setStartPoint(e.getX(), e.getY());
                    fromLocation = mapCoordstoRoboCoords(localCoordtoMapCoords(e.getPoint()));
                }
            }

            @Override
            public void mouseExited(@Nullable MouseEvent e) {
                updateStatusLine(null);
            }

            @Override
            public void mouseEntered(@Nullable MouseEvent e) {
            }

            @Override
            public void mouseClicked(@Nullable MouseEvent e) {
            }
        }));

        scalePButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                zoomIn();
            }
        });
        scaleMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                zoomOut();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                boolean loadNextFile = false;
                final File f = file;
                if (f == null || f.getParentFile() == null) {
                    return;
                }
                try {
                    for (final File fileEntry : f.getParentFile().listFiles()) {
                        if (isRRFile(fileEntry) && loadNextFile) {
                            file = fileEntry;
                            loadfile(fileEntry);
                            break;
                        }
                        if (fileEntry.getName().contentEquals(f.getName())) {
                            loadNextFile = true;
                        }
                    }
                } catch (SecurityException e) {
                    logger.debug("Error finding next file: {}", e);
                }
            }
        });

        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                File previousFile = null;
                final File f = file;
                if (f == null || f.getParentFile() == null) {
                    return;
                }
                try {
                    for (final File fileEntry : f.getParentFile().listFiles()) {
                        if (fileEntry.getName().contentEquals(f.getName())) {
                            if (previousFile != null) {
                                file = previousFile;
                                loadfile(previousFile);
                                break;
                            }
                        }
                        if (isRRFile(fileEntry)) {
                            previousFile = fileEntry;
                        }
                    }
                } catch (SecurityException e) {
                    logger.debug("Error finding next file:{}", e);
                }
            }
        });

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                JFileChooser chooser = new JFileChooser("images");
                chooser.setFileFilter(new FileNameExtensionFilter("Robot Radar map (*.rrmap,*.gz)", "rrmap", "gz"));
                try {
                    int option = chooser.showOpenDialog(parent);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        final File f = chooser.getSelectedFile();
                        file = f;
                        loadfile(f);
                    } else {
                        statusbarL.setText("You cancelled.");
                    }
                } catch (HeadlessException e) {
                    logger.debug("{}", e);
                }
            }
        });

        if (args.length > 0) {
            loadfile(new File(args[0]));
        } else {
            loadFirstFile();
        }
    }

    private MapPoint mapCoordstoRoboCoords(MapPoint imagePoint) {
        final RRMapDraw rrMap = this.rrMap;
        if (rrMap != null) {
            final RRMapFileParser mapDetails = rrMap.getMapParseDetails();
            double xPos = (mapDetails.getLeft() + mapDetails.getImgWidth() - imagePoint.getX()) * MM;
            double yPos = (mapDetails.getTop() + imagePoint.getY()) * MM;
            return new MapPoint(xPos, yPos);
        } else {
            return new MapPoint();
        }
    }

    private MapPoint localCoordtoMapCoords(Point local) {
        final RRMapDraw rrMap = this.rrMap;
        if (rrMap != null) {
            double xLoc = (rrMap.getWidth() * scale - local.getX()) / scale;
            double yLoc = (rrMap.getHeight() * scale - local.getY()) / scale;
            return new MapPoint(xLoc, yLoc);
        }
        return new MapPoint();
    }

    protected boolean isRRFile(File fileEntry) {
        return fileEntry.getName().toLowerCase().endsWith(".rrmap")
                || fileEntry.getName().toLowerCase().endsWith(".gz");
    }

    private void loadFirstFile() {
        try {
            File myDocs = FileSystemView.getFileSystemView().getDefaultDirectory();
            for (final File fileEntry : myDocs.listFiles()) {
                if (isRRFile(fileEntry)) {
                    file = fileEntry;
                    loadfile(fileEntry);
                    break;
                }
            }
        } catch (SecurityException | InvalidPathException e) {
            logger.debug("Error finding first file:{}", e);
        }
    }

    private void updateStatusLine(@Nullable MapPoint p) {
        final File f = this.file;
        if (f != null) {
            statusbarL.setText(f.getName());
        } else {
            statusbarL.setText("");
        }
        if (p != null) {
            statusbarR.setText(String.format("%s  zoom: %.1fx ", p.toString(), scale));
        } else {
            statusbarR.setText(String.format("zoom: %.1fx ", scale));
        }
    }

    private void loadfile(File file) {
        try {
            logger.info("Loading " + file.getPath());
            final RRMapDraw rrMap = RRMapDraw.loadImage(file);
            this.rrMap = rrMap;
            textArea.setText(rrMap.toString());
            parent.setTitle(TITLE + " " + file.getName());
            rrDrawPanel.setImage(rrMap.getImage(scale));
            rrMap.writePic(file.getPath() + ".jpg", "JPEG", scale);
            updateStatusLine(null);
            rrDrawPanel.setSize(rrMap.getWidth(), rrMap.getHeight());
        } catch (Exception e) {
            textArea.append("Error while loading: " + e.getMessage());
            logger.info("Error while loading {}", e);
        }
    }

    private void zoomIn() {
        scale = scale + 0.5f;
        final File f = file;
        if (f != null) {
            loadfile(f);
        }
    }

    private void zoomOut() {
        scale = scale < 1.5 ? 1 : scale - 0.5f;
        final File f = file;
        if (f != null) {
            loadfile(f);
        }
    }
}

/**
 * Point with X & Y coordinate
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
class MapPoint {
    private double x;
    private double y;

    public MapPoint() {
        this(0, 0);
    }

    public MapPoint(MapPoint p) {
        this(p.x, p.y);
    }

    public MapPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("[%.0f,%.0f]", x, y);
    }
}

/**
 * Robot Radar Map map panel
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
class RRDrawPanel extends JPanel {
    private static final long serialVersionUID = 8791558011928073284L;
    private @Nullable BufferedImage image;
    Dimension size = new Dimension();

    int x, y, x2, y2;

    @Override
    protected void paintComponent(@Nullable Graphics g) {
        super.paintComponent(g);
        final BufferedImage image = this.image;
        if (g != null && image != null) {
            g.drawImage(image, 0, 0, this);
            if (hasDrawZone()) {
                g.setColor(Color.YELLOW);
                drawZoneRect(g, x, y, x2, y2);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    public void setImage(BufferedImage bi) {
        image = bi;
        x = y = x2 = y2 = 0;
        setComponentSize();
        repaint();
    }

    private void setComponentSize() {
        final BufferedImage image = this.image;
        if (image != null) {
            size.width = image.getWidth();
            size.height = image.getHeight();
            revalidate(); // signal parent/scrollpane
        }
    }

    public void setStartPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setEndPoint(int x, int y) {
        x2 = (x);
        y2 = (y);
    }

    public boolean hasDrawZone() {
        int pw = Math.abs(x - x2);
        int ph = Math.abs(y - y2);
        return pw != 0 && ph != 0;
    }

    public void drawZoneRect(Graphics g, int x, int y, int x2, int y2) {
        int px = Math.min(x, x2);
        int py = Math.min(y, y2);
        int pw = Math.abs(x - x2);
        int ph = Math.abs(y - y2);
        g.drawRect(px, py, pw, ph);
    }
}

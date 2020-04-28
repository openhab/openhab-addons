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
package org.openhab.binding.miio.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.junit.Ignore;
import org.openhab.binding.miio.internal.robot.RRMapDraw;
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
    private final JFrame parent;
    private final RRDrawPanel rrDrawPanel = new RRDrawPanel();
    private final JTextArea textArea = new JTextArea();
    private final JLabel statusbarL = new JLabel();
    private final JLabel statusbarR = new JLabel("1.0x");

    private float scale = 1.0f;
    private @Nullable File file;

    private final Logger logger = LoggerFactory.getLogger(RoboMapViewer.class);
    private static final long serialVersionUID = 2623447051590306992L;

    @Ignore
    public static void main(String args[]) {
        System.setProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
        RoboMapViewer vc = new RoboMapViewer();
        vc.setVisible(true);
    }

    public RoboMapViewer() {
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

        // TODO: have the map details of the coord with mouse click/moveover
        rrDrawPanel.addMouseListener((new MouseListener() {
            @Override
            public void mouseReleased(@Nullable MouseEvent e) {
            }

            @Override
            public void mousePressed(@Nullable MouseEvent e) {
                if (e != null) {
                    logger.info("Click @ {}", e.getPoint());
                }
            }

            @Override
            public void mouseExited(@Nullable MouseEvent e) {
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
                scale = scale + 0.5f;
                final File f = file;
                if (f != null) {
                    loadfile(f);
                }
            }
        });
        scaleMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent ae) {
                scale = scale < 1.5 ? 1 : scale - 0.5f;
                final File f = file;
                if (f != null) {
                    loadfile(f);
                }
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
                    logger.debug("Error finding next file:{}", e);
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

        loadFirstFile();
    }

    protected boolean isRRFile(File fileEntry) {
        boolean isRRFile = fileEntry.getName().toLowerCase().endsWith(".rrmap")
                || fileEntry.getName().toLowerCase().endsWith(".gz");
        return isRRFile;
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

    private void updateStatusLine() {
        final File f = this.file;
        if (f != null) {
            statusbarL.setText(f.getName());
        } else {
            statusbarL.setText("");
        }
        statusbarR.setText("zoom: " + Float.toString(scale) + "x ");
    }

    private void loadfile(File file) {
        try {
            logger.info("Loading " + file.getPath());
            RRMapDraw rrMap = RRMapDraw.loadImage(file);
            textArea.setText(rrMap.toString());
            parent.setTitle(TITLE + " " + file.getName());
            rrDrawPanel.setImage(rrMap.getImage(scale));
            rrMap.writePic(file.getPath() + ".jpg", "JPEG", scale);
            updateStatusLine();
            rrDrawPanel.setSize(rrMap.getWidth(), rrMap.getHeight());
        } catch (Exception e) {
            textArea.append("Error while loading: " + e.getMessage());
            logger.info("Error while loading {}", e);
        }
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

    @Override
    protected void paintComponent(@Nullable Graphics g) {
        super.paintComponent(g);
        final BufferedImage image = this.image;
        if (g != null && image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    public void setImage(BufferedImage bi) {
        image = bi;
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
}

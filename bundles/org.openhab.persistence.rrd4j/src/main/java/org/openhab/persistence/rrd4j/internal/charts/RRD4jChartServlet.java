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
package org.openhab.persistence.rrd4j.internal.charts;

import static java.util.Map.entry;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.ui.chart.ChartProvider;
import org.openhab.core.ui.items.ItemUIRegistry;
import org.openhab.persistence.rrd4j.internal.RRD4jPersistenceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.RrdDb;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphConstants.FontTag;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet generates time-series charts for a given set of items.
 * It accepts the following HTTP parameters:
 * <ul>
 * <li>w: width in pixels of image to generate</li>
 * <li>h: height in pixels of image to generate</li>
 * <li>period: the time span for the x-axis. Value can be h,4h,8h,12h,D,3D,W,2W,M,2M,4M,Y</li>
 * <li>items: A comma separated list of item names to display
 * <li>groups: A comma separated list of group names, whose members should be displayed
 * </ul>
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Chris Jackson - a few improvements
 * @author Jan N. Klug - a few improvements
 *
 */
@NonNullByDefault
@Component(service = ChartProvider.class)
public class RRD4jChartServlet implements Servlet, ChartProvider {

    private final Logger logger = LoggerFactory.getLogger(RRD4jChartServlet.class);

    private static final int DEFAULT_HEIGHT = 240;
    private static final int DEFAULT_WIDTH = 480;

    /** the URI of this servlet */
    public static final String SERVLET_NAME = "/rrdchart.png";

    protected static final Color[] LINECOLORS = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA,
            Color.ORANGE, Color.CYAN, Color.PINK, Color.DARK_GRAY, Color.YELLOW };
    protected static final Color[] AREACOLORS = new Color[] { new Color(255, 0, 0, 30), new Color(0, 255, 0, 30),
            new Color(0, 0, 255, 30), new Color(255, 0, 255, 30), new Color(255, 128, 0, 30),
            new Color(0, 255, 255, 30), new Color(255, 0, 128, 30), new Color(255, 128, 128, 30),
            new Color(255, 255, 0, 30) };

    private static final Duration DEFAULT_PERIOD = Duration.ofDays(1);

    private static final Map<String, Duration> PERIODS = Map.ofEntries( //
            entry("h", Duration.ofHours(1)), entry("4h", Duration.ofHours(4)), //
            entry("8h", Duration.ofHours(8)), entry("12h", Duration.ofHours(12)), //
            entry("D", Duration.ofDays(1)), entry("2D", Duration.ofDays(2)), //
            entry("3D", Duration.ofDays(3)), entry("W", Duration.ofDays(7)), //
            entry("2W", Duration.ofDays(14)), entry("M", Duration.ofDays(30)), //
            entry("2M", Duration.ofDays(60)), entry("4M", Duration.ofDays(120)), //
            entry("Y", Duration.ofDays(365))//
    );

    private final HttpService httpService;
    private final ItemUIRegistry itemUIRegistry;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public RRD4jChartServlet(final @Reference HttpService httpService, final @Reference ItemUIRegistry itemUIRegistry,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpService = httpService;
        this.itemUIRegistry = itemUIRegistry;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Activate
    protected void activate() {
        try {
            logger.debug("Starting up rrd chart servlet at {}", SERVLET_NAME);
            httpService.registerServlet(SERVLET_NAME, this, new Hashtable<>(), httpService.createDefaultHttpContext());
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(SERVLET_NAME);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        logger.debug("RRD4J received incoming chart request: {}", req);

        int width = parseInt(req.getParameter("w"), DEFAULT_WIDTH);
        int height = parseInt(req.getParameter("h"), DEFAULT_HEIGHT);
        String periodParam = req.getParameter("period");
        Duration period = periodParam == null ? DEFAULT_PERIOD : PERIODS.getOrDefault(periodParam, DEFAULT_PERIOD);

        // Create the start and stop time
        ZonedDateTime timeEnd = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        ZonedDateTime timeBegin = timeEnd.minus(period);

        try {
            BufferedImage chart = createChart(null, null, timeBegin, timeEnd, height, width, req.getParameter("items"),
                    req.getParameter("groups"), null, null);
            // Set the content type to that provided by the chart provider
            res.setContentType("image/" + getChartType());
            ImageIO.write(chart, getChartType().toString(), res.getOutputStream());
        } catch (ItemNotFoundException e) {
            logger.debug("Item not found error while generating chart", e);
            throw new ServletException("Item not found error while generating chart: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("Illegal argument in chart", e);
            throw new ServletException("Illegal argument in chart: " + e.getMessage());
        }
    }

    private int parseInt(@Nullable String s, int defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            logger.debug("'{}' is not an integer, using default: {}", s, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Adds a line for the item to the graph definition.
     * The color of the line is determined by the counter, it simply picks the according index from LINECOLORS (and
     * rolls over if necessary).
     *
     * @param graphDef the graph definition to fill
     * @param item the item to add a line for
     * @param counter defines the number of the datasource and is used to determine the line color
     */
    protected void addLine(RrdGraphDef graphDef, Item item, int counter) {
        Color color = LINECOLORS[counter % LINECOLORS.length];
        String label = itemUIRegistry.getLabel(item.getName());
        String rrdName = RRD4jPersistenceService.DB_FOLDER + File.separator + item.getName() + ".rrd";
        ConsolFun consolFun;
        if (label != null && label.contains("[") && label.contains("]")) {
            label = label.substring(0, label.indexOf('['));
        }
        try {
            RrdDb db = RrdDb.of(rrdName);
            consolFun = db.getRrdDef().getArcDefs()[0].getConsolFun();
            db.close();
        } catch (IOException e) {
            consolFun = ConsolFun.MAX;
        }
        if (item instanceof NumberItem) {
            // we only draw a line
            graphDef.datasource(Integer.toString(counter), rrdName, "state", consolFun); // RRD4jService.getConsolidationFunction(item));
            graphDef.line(Integer.toString(counter), color, label, 2);
        } else {
            // we draw a line and fill the area beneath it with a transparent color
            graphDef.datasource(Integer.toString(counter), rrdName, "state", consolFun); // RRD4jService.getConsolidationFunction(item));
            Color areaColor = AREACOLORS[counter % LINECOLORS.length];

            graphDef.area(Integer.toString(counter), areaColor);
            graphDef.line(Integer.toString(counter), color, label, 2);
        }
    }

    @Override
    public void init(@Nullable ServletConfig config) throws ServletException {
    }

    @Override
    public @Nullable ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public @Nullable String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }

    // ----------------------------------------------------------
    // The following methods implement the ChartServlet interface

    @Override
    public String getName() {
        return "rrd4j";
    }

    @Override
    public BufferedImage createChart(@Nullable String service, @Nullable String theme, ZonedDateTime startTime,
            ZonedDateTime endTime, int height, int width, @Nullable String items, @Nullable String groups,
            @Nullable Integer dpi, @Nullable Boolean legend) throws ItemNotFoundException {
        RrdGraphDef graphDef = new RrdGraphDef(startTime.toEpochSecond(), endTime.toEpochSecond());
        graphDef.setWidth(width);
        graphDef.setHeight(height);
        graphDef.setAntiAliasing(true);
        graphDef.setImageFormat("PNG");
        graphDef.setTextAntiAliasing(true);
        graphDef.setFont(FontTag.TITLE, new Font("SansSerif", Font.PLAIN, 15));
        graphDef.setFont(FontTag.DEFAULT, new Font("SansSerif", Font.PLAIN, 11));

        int seriesCounter = 0;

        // Loop through all the items
        if (items != null) {
            String[] itemNames = items.split(",");
            for (String itemName : itemNames) {
                Item item = itemUIRegistry.getItem(itemName);
                addLine(graphDef, item, seriesCounter++);
            }
        }

        // Loop through all the groups and add each item from each group
        if (groups != null) {
            String[] groupNames = groups.split(",");
            for (String groupName : groupNames) {
                Item item = itemUIRegistry.getItem(groupName);
                if (item instanceof GroupItem) {
                    GroupItem groupItem = (GroupItem) item;
                    for (Item member : groupItem.getMembers()) {
                        addLine(graphDef, member, seriesCounter++);
                    }
                } else {
                    throw new ItemNotFoundException("Item '" + item.getName() + "' defined in groups is not a group.");
                }
            }
        }

        // Write the chart as a PNG image
        try {
            RrdGraph graph = new RrdGraph(graphDef);
            BufferedImage bi = new BufferedImage(graph.getRrdGraphInfo().getWidth(),
                    graph.getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
            graph.render(bi.getGraphics());
            return bi;
        } catch (IOException e) {
            throw new UncheckedIOException("Error generating RrdGraph", e);
        }
    }

    @Override
    public ImageType getChartType() {
        return ImageType.png;
    }
}

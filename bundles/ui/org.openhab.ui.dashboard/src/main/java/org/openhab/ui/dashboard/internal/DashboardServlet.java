/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.dashboard.internal;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.ui.dashboard.DashboardTile;

/**
 * This servlet constructs the main HTML page for the dashboard, listing all DashboardTiles
 * that are registered as a service.
 * 
 * @author Kai Kreuzer
 *
 */
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = -5154582000538034381L;

    private String indexTemplate;

    private String entryTemplate;

    private Set<DashboardTile> tiles;

    public DashboardServlet(String indexTemplate, String entryTemplate, Set<DashboardTile> tiles) {
        this.indexTemplate = indexTemplate;
        this.entryTemplate = entryTemplate;
        this.tiles = tiles;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder entries = new StringBuilder();        
        for(DashboardTile tile : tiles) {
            String entry = entryTemplate.replace("<!--name-->", tile.getName());
            entry = entry.replace("<!--url-->", tile.getUrl());
            entry = entry.replace("<!--overlay-->", tile.getOverlay());
            entry = entry.replace("<!--icon-->", tile.getImageUrl());
            entries.append(entry);
        }
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().append(indexTemplate.replace("<!--entries-->", entries.toString()));
        resp.getWriter().close();
    }
}

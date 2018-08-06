/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energenie.internal.rest.RestClient;

/**
 * @author Svilen Valkanov - Initial contribution
 */
public class EnergenieServlet extends HttpServlet {

    public static final String EMPTY_DATA_ARRAY = "{'status':'success', 'data':[]}";
    public static final String EMPTY_DATA_OBJECT = "{'status':'success', 'data':{}}";

    private String content;

    public EnergenieServlet(String content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpStatus.OK_200);
        resp.setContentType(RestClient.CONTENT_TYPE);
        PrintWriter out = resp.getWriter();
        out.print(this.content);
    }

}

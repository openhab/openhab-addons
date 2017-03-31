/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test;

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.jetty.http.HttpStatus
import org.openhab.binding.mihome.internal.rest.RestClient

/**
 * @author Svilen Valkanov
 */
public class MiHomeServlet extends HttpServlet {

    public static final String EMPTY_DATA_ARRAY ="{'status':'success', 'data':[]}"
    public static final String EMPTY_DATA_OBJECT = "{'status':'success', 'data':{}}"

    private String content

    public MiHomeServlet(String content) {
        this.content = content
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpStatus.OK_200)
        resp.setContentType(RestClient.CONTENT_TYPE)

        PrintWriter out = resp.getWriter()
        out.print(this.content)
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

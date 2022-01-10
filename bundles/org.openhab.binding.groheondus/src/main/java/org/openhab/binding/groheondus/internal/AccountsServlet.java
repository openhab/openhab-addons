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
package org.openhab.binding.groheondus.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
@Component(service = AccountsServlet.class, scope = ServiceScope.SINGLETON)
public class AccountsServlet extends HttpServlet {
    private static final long serialVersionUID = -9183159739446995608L;

    private static final String SERVLET_URL = "/groheondus";

    private final Logger logger = LoggerFactory.getLogger(AccountsServlet.class);
    private final List<Thing> accounts = new ArrayList<>();
    private HttpService httpService;

    @Activate
    public AccountsServlet(@Reference HttpService httpService) {
        this.httpService = httpService;

        try {
            httpService.registerServlet(SERVLET_URL, this, null, httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            logger.warn("Register servlet fails", e);
        }
    }

    public void addAccount(Thing accountThing) {
        accounts.add(accountThing);
    }

    public void removeAccount(Thing accountThing) {
        accounts.remove(accountThing);
    }

    public void deactivate() {
        httpService.unregister(SERVLET_URL);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null || resp == null) {
            return;
        }

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<html>");
        htmlString.append("<head>");
        htmlString.append("<title>GROHE Ondus Account login</title>");
        htmlString.append("</head>");
        htmlString.append("<body>");
        if (accounts.isEmpty()) {
            htmlString.append(
                    "Please first create an GROHE ONDUS account thing in openHAB in order to log into this account.");
        } else {
            htmlString.append(
                    "You've the following GROHE ONDUS account things, click on the one you want to manage:<br />");
            htmlString.append("<ul>");
            accounts.forEach(account -> {
                String accountId = account.getUID().getId();
                htmlString.append("<li>");
                htmlString.append("<a href=\"");
                htmlString.append(SERVLET_URL);
                htmlString.append("/");
                htmlString.append(accountId);
                htmlString.append("\">");
                htmlString.append(accountId);
                htmlString.append("</a>");
                htmlString.append("</li>");
            });
            htmlString.append("</ul>");
        }
        htmlString.append("</body>");
        htmlString.append("</html>");

        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().write(htmlString.toString());
    }
}

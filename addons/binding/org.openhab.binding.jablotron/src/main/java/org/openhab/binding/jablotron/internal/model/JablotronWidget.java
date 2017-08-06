/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model;

/**
 * The {@link JablotronWidget} class defines the widgets
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronWidget {
    private String name;
    private int id;
    private String url;
    private String templateService;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTemplateService() {
        return templateService;
    }
}

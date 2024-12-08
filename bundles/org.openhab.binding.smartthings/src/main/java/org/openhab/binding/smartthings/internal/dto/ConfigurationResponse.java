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
package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings configuration response
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class ConfigurationResponse {
    public class configurationData {
        public class initialize {
            public String name;
            public String description;
            public String id;
            public String firstPageId;

            public String[] permissions;
        }

        public initialize initialize;

        public class Page {
            public String pageId;
            public String name;
            public String nextPageId;
            public String previousPageId;
            public Boolean complete;

            public class Section {
                public String name;

                public class Setting {
                    public String id;
                    public String name;
                    public String description;
                    public String type;
                    public Boolean required;
                    public Boolean multiple;
                    public String defaultValue;

                    public String[] capabilities;
                    public String[] permissions;
                }

                public Setting[] settings;
            }

            public Section[] sections;
        }

        public Page page;
    }

    public configurationData configurationData;
}

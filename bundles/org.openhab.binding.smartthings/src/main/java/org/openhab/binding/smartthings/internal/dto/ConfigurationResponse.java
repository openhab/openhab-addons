package org.openhab.binding.smartthings.internal.dto;

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

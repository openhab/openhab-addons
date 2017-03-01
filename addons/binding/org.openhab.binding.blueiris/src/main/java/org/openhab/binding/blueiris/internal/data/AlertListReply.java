package org.openhab.binding.blueiris.internal.data;

import java.util.List;

/**
 * Alertlist data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class AlertListReply {
    private List<Data> alertlist;

    /** Get all the alert list data bits. */
    public List<Data> getAlertlist() {
        return alertlist;
    }

    /**
     * Contains the data about each individual alert.
     *
     * @author David Bennett - Initial Contribution
     */
    public static class Data {
        private String camera;
        private String jpeg;
        private String path;
        private Integer offset;
        private Integer flags;
        private Integer date;
        private Integer color;

        public String getCamera() {
            return camera;
        }

        public String getJpeg() {
            return jpeg;
        }

        public String getPath() {
            return path;
        }

        public Integer getOffset() {
            return offset;
        }

        public Integer getFlags() {
            return flags;
        }

        public Integer getDate() {
            return date;
        }

        public Integer getColor() {
            return color;
        }

    }
}

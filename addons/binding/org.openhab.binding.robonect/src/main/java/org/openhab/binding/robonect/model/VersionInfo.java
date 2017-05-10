package org.openhab.binding.robonect.model;

/**
 * {"robonect": {"serial": "05D92D32-38355048-43203030", "version": "V0.9", "compiled": "2017-03-25 20:10:00",
 * "comment": "V0.9c"}, "successful": true}
 */
public class VersionInfo extends RobonectAnswer {

    public class RobonectVersion {
        private String serial;

        private String version;

        private String compiled;

        private String comment;

        public String getSerial() {
            return serial;
        }

        public String getVersion() {
            return version;
        }

        public String getCompiled() {
            return compiled;
        }

        public String getComment() {
            return comment;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setCompiled(String compiled) {
            this.compiled = compiled;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
    
    private RobonectVersion robonect;

    public RobonectVersion getRobonect() {
        return robonect;
    }

    public void setRobonect(RobonectVersion robonect) {
        this.robonect = robonect;
    }
}

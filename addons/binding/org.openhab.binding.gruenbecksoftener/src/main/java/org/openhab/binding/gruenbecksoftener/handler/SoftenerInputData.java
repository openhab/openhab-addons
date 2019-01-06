package org.openhab.binding.gruenbecksoftener.handler;

public class SoftenerInputData {

    private String datapointId;
    private SoftenerDataType datatype;
    private String code;
    private String group;

    public String getDatapointId() {
        return datapointId;
    }

    public void setDatapointId(String datapointId) {
        this.datapointId = datapointId;
    }

    public SoftenerDataType getDatatype() {
        return datatype;
    }

    public void setDatatype(SoftenerDataType datatype) {
        this.datatype = datatype;
    }

    public String getCode() {
        if (code == null) {
            return "";
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}

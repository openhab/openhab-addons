package org.openhab.binding.siemenshvac.internal.Metadata;

public class SiemensHvacMetadataDevice {
    private String name;
    private String addr;
    private String type;
    private String serialNr;
    private String treeDate;
    private String treeTime;
    private boolean treeGenerated;
    private int treeId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNr() {
        return serialNr;
    }

    public void setSerialNr(String serialNr) {
        this.serialNr = serialNr;
    }

    public String getTreeDate() {
        return treeDate;
    }

    public void setTreeDate(String treeDate) {
        this.treeDate = treeDate;
    }

    public String getTreeTime() {
        return treeTime;
    }

    public void setTreeTime(String treeTime) {
        this.treeTime = treeTime;
    }

    public boolean getTreeGenerated() {
        return treeGenerated;
    }

    public void setTreeGenerated(boolean treeGenerated) {
        this.treeGenerated = treeGenerated;
    }

    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

}

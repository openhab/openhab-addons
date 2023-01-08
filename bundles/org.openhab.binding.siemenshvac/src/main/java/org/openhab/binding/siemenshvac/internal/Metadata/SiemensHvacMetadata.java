package org.openhab.binding.siemenshvac.internal.Metadata;

public class SiemensHvacMetadata {
    private int Id = -1;
    private int menuId = -1;
    private int groupId = -1;
    private int catId = -1;
    private String shortDesc = null;
    private String longDesc = null;
    private transient SiemensHvacMetadata parent;

    public SiemensHvacMetadata() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public SiemensHvacMetadata getParent() {
        return parent;
    }

    public void setParent(SiemensHvacMetadata parent) {
        this.parent = parent;
    }
}

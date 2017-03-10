package org.openhab.binding.insteonplm.handler;

/**
 * The details of the node tracking if we have pinged it for details yet or not.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class InsteonNodeDetails {
    private boolean queried;
    private byte deviceCategory;
    private byte deviceSubcategory;
    private int productKey;

    public boolean isQueried() {
        return queried;
    }

    public void setQueried(boolean queried) {
        this.queried = queried;
    }

    public byte getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(byte deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public byte getDeviceSubcategory() {
        return deviceSubcategory;
    }

    public void setDeviceSubcategory(byte deviceSubcategory) {
        this.deviceSubcategory = deviceSubcategory;
    }

    public int getProductKey() {
        return productKey;
    }

    public void setProductKey(int productKey) {
        this.productKey = productKey;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InsteonNodeDetails [");
        builder.append("queried=");
        builder.append(queried);
        builder.append(", deviceCategory=");
        builder.append(deviceCategory);
        builder.append(", deviceSubcategory=");
        builder.append(deviceSubcategory);
        builder.append(", productKey=");
        builder.append(productKey);
        builder.append("]");
        return builder.toString();
    }
}

package org.openhab.binding.vitaled.internal;

/**
 * The {@link vitaLEDZone} is responsible for handling a zone.
 *
 * @author Marcel Salein - Initial contribution
 */
public class vitaLEDZone {
    private String ZoneDescription = new String();
    private int red;
    private int blue;
    private int green;
    private int white;
    private int xCoord;
    private int yCoord;
    private int achromaticLight;
    private int intensity;
    private int speed;
    private int colourSaturation;
    private int activeMode;
    private int scene1;
    private int scene2;
    private int scene3;
    private int scene4;
    private int scene5;
    private int scene6;

    public String getZoneDescription() {
        return ZoneDescription;
    }

    public void setZoneDescription(String zoneDescription) {
        ZoneDescription = zoneDescription;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(int white) {
        this.white = white;
    }

    public int getxCoord() {
        return xCoord;
    }

    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public int getAchromaticLight() {
        return achromaticLight;
    }

    public void setAchromaticLight(int achromaticLight) {
        this.achromaticLight = achromaticLight;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getColourSaturation() {
        return colourSaturation;
    }

    public void setColourSaturation(int colourSaturation) {
        this.colourSaturation = colourSaturation;
    }

    public int getActiveMode() {
        return activeMode;
    }

    public void setActiveMode(int activeMode) {
        this.activeMode = activeMode;
    }

    public int getScene1() {
        return scene1;
    }

    public void setScene1(int scene1) {
        this.scene1 = scene1;
    }

    public int getScene2() {
        return scene2;
    }

    public void setScene2(int scene2) {
        this.scene2 = scene2;
    }

    public int getScene3() {
        return scene3;
    }

    public void setScene3(int scene3) {
        this.scene3 = scene3;
    }

    public int getScene4() {
        return scene4;
    }

    public void setScene4(int scene4) {
        this.scene4 = scene4;
    }

    public int getScene5() {
        return scene5;
    }

    public void setScene5(int scene5) {
        this.scene5 = scene5;
    }

    public int getScene6() {
        return scene6;
    }

    public void setScene6(int scene6) {
        this.scene6 = scene6;
    }
}

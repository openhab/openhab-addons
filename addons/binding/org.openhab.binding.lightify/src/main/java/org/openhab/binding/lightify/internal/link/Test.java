package org.openhab.binding.lightify.internal.link;

public class Test {

    public static void main(String[] args) {
        LightifyLink link = new LightifyLink("172.25.100.141");
        link.performSearch(l -> {
            if (l instanceof LightifyZone) {
                l.setSwitch(!l.isPowered(), null);
            }
        });
    }

}

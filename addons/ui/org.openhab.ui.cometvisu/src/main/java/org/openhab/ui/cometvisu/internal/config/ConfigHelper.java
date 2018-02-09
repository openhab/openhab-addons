/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.icon.IconSet.Format;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.config.beans.Address;
import org.openhab.ui.cometvisu.internal.config.beans.CDataStatus;
import org.openhab.ui.cometvisu.internal.config.beans.Entry;
import org.openhab.ui.cometvisu.internal.config.beans.Group;
import org.openhab.ui.cometvisu.internal.config.beans.Icon;
import org.openhab.ui.cometvisu.internal.config.beans.IconDefinition;
import org.openhab.ui.cometvisu.internal.config.beans.Icons;
import org.openhab.ui.cometvisu.internal.config.beans.Info;
import org.openhab.ui.cometvisu.internal.config.beans.Label;
import org.openhab.ui.cometvisu.internal.config.beans.Layout;
import org.openhab.ui.cometvisu.internal.config.beans.Line;
import org.openhab.ui.cometvisu.internal.config.beans.Mapping;
import org.openhab.ui.cometvisu.internal.config.beans.Mappings;
import org.openhab.ui.cometvisu.internal.config.beans.Multitrigger;
import org.openhab.ui.cometvisu.internal.config.beans.Navbar;
import org.openhab.ui.cometvisu.internal.config.beans.NavbarPositionType;
import org.openhab.ui.cometvisu.internal.config.beans.ObjectFactory;
import org.openhab.ui.cometvisu.internal.config.beans.Page;
import org.openhab.ui.cometvisu.internal.config.beans.Pagejump;
import org.openhab.ui.cometvisu.internal.config.beans.Pages;
import org.openhab.ui.cometvisu.internal.config.beans.Plugin;
import org.openhab.ui.cometvisu.internal.config.beans.Plugins;
import org.openhab.ui.cometvisu.internal.config.beans.Statusbar;
import org.openhab.ui.cometvisu.internal.config.beans.StylingEntry;
import org.openhab.ui.cometvisu.internal.config.beans.Stylings;
import org.openhab.ui.cometvisu.internal.config.beans.Text;
import org.openhab.ui.cometvisu.internal.config.beans.Trigger;
import org.openhab.ui.cometvisu.internal.config.beans.Widgetinfo;
import org.openhab.ui.cometvisu.internal.servlet.CometVisuApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a set of helper methods to convert an openHAB sitemap to a CometVisu config
 * XML file.
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class ConfigHelper {
    public enum Transform {
        NUMBER,
        SWITCH,
        STRING,
        ROLLERSHUTTER,
        DATETIME,
        TIME,
        CONTACT,
        DIMMER,
        COLOR,
        LOCATION
    }

    // MashMap(openHAB-Icon,CometVisu-Icon)
    // private static HashMap<String,String> iconMapping = new
    // HashMap<String,String>();

    private final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);
    private Pages pages;

    private String sitemapName;

    private CometVisuApp app;

    private ObjectFactory factory = new ObjectFactory();

    private HashMap<String, Mapping> mappings = new HashMap<String, Mapping>();

    private HashMap<String, String> chartPeriodMapping = new HashMap<String, String>();

    public final String defaultChartHeight = "300px";

    private Pattern labelPattern = Pattern.compile(".+\\[(MAP\\(.+\\))?:?(.+)\\]$");

    public ConfigHelper(Pages pages, CometVisuApp app, String sitemapName) {
        this.pages = pages;
        this.app = app;
        this.sitemapName = sitemapName;

        logger.info("icon mapping enabled: {}, {} known mappings",
                Config.iconConfig.get(Config.COMETVISU_ICON_ENABLE_MAPPING_PROPERTY), Config.iconMappings.size());

        // this.initIconMapping();
        this.initBasicMappings();
        this.initBasicStylings();
        this.initStatusBar();

        chartPeriodMapping.put("h", "hour");
        chartPeriodMapping.put("d", "day");
        chartPeriodMapping.put("w", "week");
        chartPeriodMapping.put("m", "month");
        chartPeriodMapping.put("y", "year");
    }

    public String getCvChartPeriod(String ohChartPeriod) {
        if (chartPeriodMapping.containsKey(ohChartPeriod.toLowerCase())) {
            return chartPeriodMapping.get(ohChartPeriod.toLowerCase());
        } else {
            return ohChartPeriod.toLowerCase();
        }
    }

    /**
     * add the default statusbar know from the CometVisu´s demo config
     */
    private void initStatusBar() {
        Statusbar statusbar = new Statusbar();
        CDataStatus confStatus = new CDataStatus();
        confStatus.setType("html");
        confStatus.setValue(
                "<img src=\"icon/comet_64_ff8000.png\" alt=\"CometVisu\" /> by <a href=\"http://www.cometvisu.org/\">CometVisu.org</a>");
        statusbar.getStatus().add(confStatus);

        CDataStatus reloadStatus = new CDataStatus();
        reloadStatus.setType("html");
        reloadStatus.setHrefextend("config");
        reloadStatus.setValue(" - <a href=\".?forceReload=true\">Reload</a>");
        statusbar.getStatus().add(reloadStatus);

        CDataStatus defaultStatus = new CDataStatus();
        defaultStatus.setType("html");
        defaultStatus.setValue(" - <a href=\".\">Default Config</a>");
        statusbar.getStatus().add(defaultStatus);

        if (this.app.getServlet().isPhpEnabled()) {
            CDataStatus editStatus = new CDataStatus();
            editStatus.setType("html");
            editStatus.setCondition("!edit");
            editStatus.setHrefextend("config");
            editStatus.setValue(" - <a href=\"editor/\">Edit</a>");
            statusbar.getStatus().add(editStatus);

            CDataStatus checkStatus = new CDataStatus();
            checkStatus.setType("html");
            checkStatus.setHrefextend("config");
            checkStatus.setValue("- <a href=\"check_config.php\">Check Config</a>");
            statusbar.getStatus().add(checkStatus);
        }

        // add download link
        CDataStatus downloadConfig = new CDataStatus();
        downloadConfig.setType("html");
        downloadConfig
                .setValue(" - <a href=\"config/" + this.sitemapName + "\" download target=\"_blank\">Download</a>");
        statusbar.getStatus().add(downloadConfig);

        // version information
        CDataStatus versionHint = new CDataStatus();
        versionHint.setType("html");
        versionHint.setHrefextend("config");
        String version = "autogenerated from openHAB " + sitemapName + " sitemap";
        versionHint.setValue("<div style=\"float:right;padding-right:0.5em\">Version: " + version + "</div>");
        statusbar.getStatus().add(versionHint);

        pages.getMeta().getPluginsOrIconsOrMappings().add(statusbar);
    }

    public boolean hasIconMapping(String ohIconName) {
        return Config.iconConfig.get(Config.COMETVISU_ICON_ENABLE_MAPPING_PROPERTY) != null
                && Config.iconConfig.get(Config.COMETVISU_ICON_ENABLE_MAPPING_PROPERTY).equals("true")
                && Config.iconMappings.get(ohIconName) != null;
    }

    public String getIconMapping(String ohIconName) {
        return (String) Config.iconMappings.get(ohIconName);
    }

    /**
     * add some basic stylings
     */
    private void initBasicStylings() {
        StylingEntry styling = new StylingEntry();
        styling.setName("RedGreen");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("0", "red");
        map.put("1", "green");
        for (String value : map.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(map.get(value));
            styling.getEntry().add(entry);
        }
        addToStylings(styling);

        styling = new StylingEntry();
        styling.setName("GreyGreen");
        map.clear();
        map.put("0", "grey");
        map.put("1", "green");
        for (String value : map.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(map.get(value));
            styling.getEntry().add(entry);
        }
        addToStylings(styling);

        styling = new StylingEntry();
        styling.setName("GreenGrey");
        map.clear();
        map.put("0", "green");
        map.put("1", "grey");
        for (String value : map.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(map.get(value));
            styling.getEntry().add(entry);
        }
        addToStylings(styling);
    }

    /**
     * add some basic mappings
     */
    private void initBasicMappings() {
        // Rollershutter mapping
        Mapping mapping = new Mapping();
        mapping.setName("shutter");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("UP", "&#8593;");
        map.put("STOP", "o");
        map.put("DOWN", "&#8595;");
        for (String value : map.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(map.get(value));
            mapping.getEntry().add(entry);
        }
        addToMappings(mapping);

        // On/Off
        mapping = new Mapping();
        mapping.setName("OnOff");
        map.clear();
        map.put("0", "O");
        map.put("1", "I");
        for (String value : map.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            if (value == "0") {
                entry.setDefault(true);
            }
            entry.getContent().add(map.get(value));
            mapping.getEntry().add(entry);
        }
        addToMappings(mapping);

        // Open/Close (for contacts)
        mapping = new Mapping();
        mapping.setName("OpenClose");
        HashMap<String, Icon> iconMap = new HashMap<String, Icon>();
        iconMap.put("1", createIcon("fts_window_1w_open", "red"));
        iconMap.put("0", createIcon("fts_window_1w", null));
        for (String value : iconMap.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(factory.createEntryIcon(iconMap.get(value)));
            mapping.getEntry().add(entry);
        }
        addToMappings(mapping);

        // Up/Down
        mapping = new Mapping();
        mapping.setName("UpDown");
        iconMap.clear();
        iconMap.put("1", createIcon("control_down", null));
        iconMap.put("0", createIcon("control_up", null));
        for (String value : iconMap.keySet()) {
            Entry entry = new Entry();
            entry.setValue(value);
            entry.getContent().add(factory.createEntryIcon(iconMap.get(value)));
            mapping.getEntry().add(entry);
        }
        addToMappings(mapping);
    }

    private Icon createIcon(String name, String color) {
        Icon icon = new Icon();
        icon.setName(name);
        if (color != null && !color.isEmpty()) {
            icon.setColor(color);
        }
        return icon;
    }

    /**
     * reads the address from the given item {@link Item } creates an
     * {@link Address } element and adds it to the element object
     *
     * @param element
     *            - the element to which the Address should be added
     *            {@link Object }
     * @param item
     *            - the item name is used to create the Address element
     *            {@link Item }
     * @return - returns the created Address element {@link Address }
     */
    public Address addAddress(Object element, Item item) {
        return addAddress(element, item, Transform.STRING);
    }

    /**
     * reads the address from the given item {@link Item } creates an
     * {@link Address } element with the given {@link Transform } and adds it to
     * the element object
     *
     * @param element
     *            - the element to which the Address should be added
     *            {@link Object }
     * @param item
     *            - the item name is used to create the Address element
     *            {@link Item }
     * @param transform
     *            {@link Transform} - the transform type of the address
     * @return - returns the created Address element {@link Address }
     */
    public Address addAddress(Object element, Item item, Transform transform) {
        return addAddress(element, item, transform, null);
    }

    /**
     * reads the address from the given item {@link Item } creates an
     * {@link Address } element with the given {@link Transform } and adds it to
     * the element object
     *
     * @param element
     *            - the element to which the Address should be added
     *            {@link Object }
     * @param item
     *            - the item name is used to create the Address element
     *            {@link Item }
     * @param transform
     *            {@link Transform} - the transform type of the address
     * @param variant
     *            - variant of address (read,readwrite,write)
     * @return - returns the created Address element {@link Address }
     */
    @SuppressWarnings("unchecked")
    public Address addAddress(Object element, Item item, Transform transform, String variant) {
        if (element == null || item == null) {
            return null;
        }
        try {
            Method getAddress = element.getClass().getMethod("getAddress");
            Address address = getAddress(item, transform, variant);
            if (getAddress != null && address != null) {
                ((List<Address>) getAddress.invoke(element)).add(address);
                return address;
            }
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return null;
    }

    public Address getAddress(Item item, Transform transform) {
        return getAddress(item, transform, null);
    }

    public Address getAddress(Item item, Transform transform, String variant) {
        if (item == null) {
            return null;
        }
        Address address = new Address();
        address.setTransform("OH:" + transform.toString().toLowerCase());
        address.setValue(item.getName());
        if (variant != null) {
            address.setVariant(variant);
        }
        return address;
    }

    public Label addLabel(Object element, Widget widget) {
        return addLabel(element, getLabel(widget), null);
    }

    public Label addLabel(Object element, String name) {
        return addLabel(element, name, null);
    }

    public Label addLabel(Object element, String name, String iconName) {
        if (element == null) {
            return null;
        }
        try {
            Method setter = element.getClass().getMethod("setLabel", Label.class);
            if (setter != null) {
                Label label = new Label();
                if (iconName != null) {
                    Icon icon = new Icon();
                    icon.setName(iconName);
                    label.getContent().add(factory.createLabelIcon(icon));
                }
                label.getContent().add(name);
                setter.invoke(element, label);
                return label;
            }
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("{}", e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("{}", e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("{}", e.getMessage());
        }
        return null;
    }

    public Mapping createMapping(String name, EList<org.eclipse.smarthome.model.sitemap.Mapping> sitemapMapping) {
        Mapping mapping = null;
        if (mappings.containsKey(name)) {
            mapping = mappings.get(name);
        } else {
            // create Mapping from sitemap mapping
            mapping = new Mapping();
            mapping.setName(name);

            for (org.eclipse.smarthome.model.sitemap.Mapping map : sitemapMapping) {
                Entry entry = new Entry();
                entry.setValue(map.getCmd());
                entry.getContent().add(map.getLabel());
                mapping.getEntry().add(entry);
            }
            mappings.put(name, mapping);
            addToMappings(mapping);
        }
        return mapping;
    }

    public Mapping addMapping(Object element, Mapping mapping) {
        if (element != null && mapping != null) {
            try {
                Method setter = element.getClass().getMethod("setMapping", String.class);
                setter.invoke(element, mapping.getName());
                return mapping;
            } catch (NoSuchMethodException | SecurityException e) {
                logger.error("{}", e.getMessage());
            } catch (IllegalAccessException e) {
                logger.error("{}", e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.error("{}", e.getMessage());
            } catch (InvocationTargetException e) {
                logger.error("{}", e.getMessage());
            }
        }
        return null;
    }

    public void addRollershutter(Object element, Item item, Widget widget) {
        Group bean = new Group();
        bean.setNowidget(true);
        bean.setLayout(createLayout(6));

        // Text element as Label
        Text text = new Text();
        text.setLayout(createLayout(3));
        addLabel(text, getLabel(widget), "fts_shutter");
        addToRoot(bean, factory.createPageText(text));

        Address address = getAddress(item, Transform.ROLLERSHUTTER);

        Trigger upTrigger = new Trigger();
        upTrigger.setValue("UP");
        upTrigger.setMapping("shutter");
        upTrigger.getAddress().add(address);
        upTrigger.setLayout(createLayout(1));
        addToRoot(bean, factory.createPageTrigger(upTrigger));

        Trigger stopTrigger = new Trigger();
        stopTrigger.setValue("STOP");
        stopTrigger.setMapping("shutter");
        stopTrigger.getAddress().add(address);
        stopTrigger.setLayout(createLayout(1));
        addToRoot(bean, factory.createPageTrigger(stopTrigger));

        Trigger downTrigger = new Trigger();
        downTrigger.setValue("DOWN");
        downTrigger.setMapping("shutter");
        downTrigger.getAddress().add(address);
        downTrigger.setLayout(createLayout(1));
        addToRoot(bean, factory.createPageTrigger(downTrigger));

        addToRoot(element, factory.createPageGroup(bean));
    }

    public void mapToTriggers(Object element, Item item, Widget widget) {
        EList<org.eclipse.smarthome.model.sitemap.Mapping> sitemapMapping = getMapping(widget);

        int groupColumns = 6;

        Group bean = new Group();
        bean.setNowidget(true);
        bean.setLayout(createLayout(groupColumns));

        int textColumns = Math.min(3, groupColumns - sitemapMapping.size());
        int triggerColumns = Math.max(1, Math.round((groupColumns - textColumns) / sitemapMapping.size()));

        // Text element as Label
        Text text = new Text();
        text.setLayout(createLayout(textColumns));
        addLabel(text, getLabel(widget));

        addToRoot(bean, factory.createGroupText(text));

        Transform transform = Transform.NUMBER;

        List<Class<? extends Command>> states = new ArrayList<Class<? extends Command>>();
        states.add(DecimalType.class);
        states.add(OnOffType.class);
        states.add(OpenClosedType.class);
        states.add(UpDownType.class);
        states.add(StringType.class);

        for (org.eclipse.smarthome.model.sitemap.Mapping map : sitemapMapping) {
            Command command = TypeParser.parseCommand(states, map.getCmd());
            if (!(command instanceof DecimalType)) {
                // no number command
                transform = Transform.STRING;
                break;
            }
        }

        Address address = getAddress(item, transform);
        String mappingName = String.valueOf(sitemapMapping.hashCode());
        Mapping mapping = createMapping(mappingName, sitemapMapping);
        addToMappings(mapping);

        for (org.eclipse.smarthome.model.sitemap.Mapping map : sitemapMapping) {
            Trigger trigger = new Trigger();

            trigger.setValue(map.getCmd());
            trigger.setMapping(mappingName);
            trigger.getAddress().add(address);
            trigger.setLayout(createLayout(triggerColumns));

            addToRoot(bean, factory.createGroupTrigger(trigger));
        }
        addToRoot(element, factory.createPageGroup(bean));
    }

    /**
     * up to 4 mapping a multitrigger can be uses
     *
     * @param element
     * @param item
     * @param widget
     */
    public void mapToMultiTrigger(Object element, Item item, Widget widget) {
        EList<org.eclipse.smarthome.model.sitemap.Mapping> sitemapMapping = getMapping(widget);

        Transform transform = Transform.NUMBER;

        List<Class<? extends Command>> states = new ArrayList<Class<? extends Command>>();
        states.add(DecimalType.class);
        states.add(OnOffType.class);
        states.add(OpenClosedType.class);
        states.add(UpDownType.class);
        states.add(StringType.class);

        for (org.eclipse.smarthome.model.sitemap.Mapping map : sitemapMapping) {
            Command command = TypeParser.parseCommand(states, map.getCmd());
            if (!(command instanceof DecimalType)) {
                // no number command
                transform = Transform.STRING;
                break;
            }
        }

        Address address = getAddress(item, transform);
        // String mappingName = String.valueOf(sitemapMapping.hashCode());
        // Mapping mapping = createMapping(mappingName, sitemapMapping);
        // addToMappings(mapping);

        Multitrigger mtrigger = factory.createMultitrigger();
        mtrigger.setShowstatus("true");
        addLabel(mtrigger, getLabel(widget));
        mtrigger.getAddress().add(address);
        mtrigger.setLayout(createLayout(6));

        int i = 1;
        for (org.eclipse.smarthome.model.sitemap.Mapping map : sitemapMapping) {
            switch (i) {
                case 1:
                    mtrigger.setButton1Label(map.getLabel());
                    mtrigger.setButton1Value(map.getCmd());
                    break;
                case 2:
                    mtrigger.setButton2Label(map.getLabel());
                    mtrigger.setButton2Value(map.getCmd());
                    break;
                case 3:
                    mtrigger.setButton3Label(map.getLabel());
                    mtrigger.setButton3Value(map.getCmd());
                    break;
                case 4:
                    mtrigger.setButton4Label(map.getLabel());
                    mtrigger.setButton4Value(map.getCmd());
                    break;
            }
            i++;
        }
        addToRoot(element, factory.createPageMultitrigger(mtrigger));
    }

    @SuppressWarnings("unchecked")
    private EList<org.eclipse.smarthome.model.sitemap.Mapping> getMapping(Widget widget) {
        EList<org.eclipse.smarthome.model.sitemap.Mapping> mapping = null;
        try {
            Method getter = widget.getClass().getMethod("getMappings");
            mapping = (EList<org.eclipse.smarthome.model.sitemap.Mapping>) getter.invoke(widget);
        } catch (NoSuchMethodException | SecurityException e) {
            // do nothing, normal behaviour for item that have no mappingdefined
        } catch (IllegalAccessException e) {
            logger.error("{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("{}", e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("{}", e.getMessage());
        }
        return mapping;
    }

    public Mapping addMapping(Object element, Widget widget) {
        Mapping mapping = null;
        EList<org.eclipse.smarthome.model.sitemap.Mapping> smap = getMapping(widget);
        if (smap != null && smap.size() > 0) {
            mapping = addMapping(element, String.valueOf(smap.hashCode()), smap);
        }

        return mapping;
    }

    public Mapping addMapping(Object element, String name,
            EList<org.eclipse.smarthome.model.sitemap.Mapping> sitemapMapping) {
        Mapping mapping = createMapping(name, sitemapMapping);
        addMapping(element, mapping);
        return mapping;
    }

    public void addToMappings(Mapping mapping) {
        Mappings mappings = null;
        for (Object mp : pages.getMeta().getPluginsOrIconsOrMappings()) {
            if (mp instanceof Mappings) {
                mappings = (Mappings) mp;
                for (Mapping map : mappings.getMapping()) {
                    if (map.getName().equals(mapping.getName())) {
                        // Mapping already exists
                        return;
                    }
                }
            }
        }
        if (mappings == null) {
            mappings = new Mappings();
            pages.getMeta().getPluginsOrIconsOrMappings().add(mappings);
        }
        mappings.getMapping().add(mapping);
    }

    /**
     * map a @org.eclipse.smarthome.model.sitemap.ColorArray to an CometVisu
     * styling TODO: CometVisu only knows min/max ranges for stylings, the
     * mapping of openHAB´s conditions to these ranges is quite complex and not
     * yet implemented
     *
     * @param bean
     *            the JAXB bean the styling is added to
     * @param widget
     *            the openHAB @Widget the styling is read from
     */
    public void addStyling(Object bean, Widget widget) {
        // StylingEntry styling = null;
        // EList<org.eclipse.smarthome.model.sitemap.ColorArray> smap = getStyling(widget);
        // for (org.eclipse.smarthome.model.sitemap.ColorArray colors : smap) {
        // Entry entry = new Entry();
        // entry.getContent().add(colors.getArg());
        //
        // }
    }

    public void addToStylings(StylingEntry styling) {
        Stylings stylings = null;
        for (Object mp : pages.getMeta().getPluginsOrIconsOrMappings()) {
            if (mp instanceof Stylings) {
                stylings = (Stylings) mp;
                for (StylingEntry style : stylings.getStyling()) {
                    if (style.getName().equals(styling.getName())) {
                        // Styling already exists
                        return;
                    }
                }
            }
        }
        if (stylings == null) {
            stylings = new Stylings();
            pages.getMeta().getPluginsOrIconsOrMappings().add(stylings);
        }
        stylings.getStyling().add(styling);
    }

    public void addPlugin(Plugin plugin) {
        boolean found = false;
        Plugins plugins = null;
        for (Object rp : pages.getMeta().getPluginsOrIconsOrMappings()) {
            if (rp instanceof Plugins) {
                plugins = (Plugins) rp;
                for (Plugin p : plugins.getPlugin()) {
                    if (p.getName().equals(plugin.getName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                break;
            }

        }
        if (!found) {
            if (plugins == null) {
                plugins = new Plugins();
                pages.getMeta().getPluginsOrIconsOrMappings().add(plugins);
            }
            plugins.getPlugin().add(plugin);
        }
    }

    public void addToRoot(Object root, JAXBElement<?> child) {
        if (root instanceof Page) {
            ((Page) root).getPageOrGroupOrNavbar().add(child);
        } else if (root instanceof Group) {
            ((Group) root).getPageOrGroupOrLine().add(child);
        }
    }

    public Layout createLayout(int colspan) {
        return createLayout(colspan, 1);
    }

    public Layout createLayout(int colspan, int rowspan) {
        Layout layout = new Layout();
        layout.setColspan(new BigDecimal(colspan));
        if (rowspan != 1) {
            layout.setRowspan(new BigDecimal(rowspan));
        }
        return layout;
    }

    public String getLabel(Widget widget) {
        String label = app.getItemUIRegistry().getLabel(widget);
        // remove format
        label = label.replaceAll("\\[.*\\]$", "");
        return StringEscapeUtils.escapeXml(label);
    }

    // public JAXBElement<?> convertToJAXBElement(Object bean) {
    // return new JAXBElement(new QName(bean.getClass().getSimpleName()
    // .toLowerCase()), bean.getClass(), bean);
    // }

    /**
     * add the separating line in the navbar
     *
     * @param page
     *            - the page the navbar should be searched in
     * @param position
     *            - the position of the navbar
     * @param ifNotEmpty
     *            - true: only add the separator ot the navbar has already
     *            content (e.g. prevent a seperator at the beginning of the
     *            navbar)
     */
    public void addSeparatorToNavbar(Page page, NavbarPositionType position, boolean ifNotEmpty) {
        Navbar navbar = getNavbar(page, position);
        if (navbar != null) {
            if (!ifNotEmpty || navbar.getPageOrGroupOrLine().size() > 0) {
                Line line = new Line();
                line.setLayout(createLayout(0));
                navbar.getPageOrGroupOrLine().add(factory.createNavbarLine(line));
            }
        }
    }

    public String getExistingIconName(String ohIcon) {
        if (ohIcon == null || ohIcon.isEmpty()) {
            return null;
        }
        if (hasIconMapping(ohIcon)) {
            return getIconMapping(ohIcon);
        } else {
            // add the oh-icon to the icon definitions
            for (IconProvider provider : app.getIconProviders()) {
                logger.debug("searching for icon '{}' on provider '{}'", ohIcon, provider);
                if (provider.hasIcon(ohIcon, "classic", Format.PNG) != null) {
                    IconDefinition iconDef = factory.createIconDefinition();
                    iconDef.setUri("/icon/" + ohIcon + "?format=png");
                    iconDef.setName("OH_" + ohIcon);
                    addIconDefinition(iconDef);
                    return "OH_" + ohIcon;
                }
            }
            logger.debug("no icon named '{}' found", ohIcon);
            return null;
        }
    }

    /**
     * add a pagejump for the given page to the navbar
     *
     * @param page
     *            the page for wich the pagejump should be created
     * @param widget
     */
    public void addToNavbar(Page barPage, Page targetPage, org.eclipse.smarthome.model.sitemap.Group widget,
            NavbarPositionType position, Item item) {
        Pagejump pagejump = new Pagejump();
        pagejump.setBindClickToWidget(true);
        pagejump.setTarget(targetPage.getName());
        String ohIcon = app.getItemUIRegistry().getCategory(widget);
        if (NavbarPositionType.TOP.equals(position) || NavbarPositionType.BOTTOM.equals(position)) {
            addLabel(pagejump, targetPage.getName(), getExistingIconName(ohIcon));
        } else {
            // on LEFT / RIGHT navbar we use a different labeling approach
            addLabel(pagejump, "", getExistingIconName(ohIcon));
            pagejump.setName(targetPage.getName());
        }
        if (item != null && item.getLabel() != null) {
            Matcher m = labelPattern.matcher(item.getLabel());
            String format = null;
            if (m.matches()) {
                format = m.group(2);
                if (format.contains("%d")) {
                    Widgetinfo infowidget = new Widgetinfo();
                    Info info = new Info();
                    info.setFormat(format);
                    info.setLayout(createLayout(0));
                    Address address = new Address();
                    address.setTransform("OH:" + Transform.NUMBER.toString().toLowerCase());
                    address.setValue(Transform.NUMBER.toString().toLowerCase() + ":" + item.getName());
                    info.getAddress().add(address);
                    infowidget.setInfo(info);
                    pagejump.setWidgetinfo(infowidget);
                }
            }

        }
        // add to Root page
        addToNavbar(barPage, pagejump, position);
    }

    public void addIconDefinition(IconDefinition iconDef) {
        Icons icons = null;
        for (Object entry : pages.getMeta().getPluginsOrIconsOrMappings()) {
            if (entry instanceof Icons) {
                icons = (Icons) entry;
                break;
            }
        }
        if (icons == null) {
            icons = factory.createIcons();
            pages.getMeta().getPluginsOrIconsOrMappings().add(icons);
            icons.getIconDefinition().add(iconDef);
        } else {
            // check if definition already exists
            boolean found = false;
            for (IconDefinition def : icons.getIconDefinition()) {
                if (def.getName().equals(iconDef.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                icons.getIconDefinition().add(iconDef);
            }
        }

        logger.trace("adding icon-def '{}' url '{}'", iconDef.getName(), iconDef.getUri());
    }

    private Navbar getNavbar(Page page, NavbarPositionType position) {
        Navbar navbar = null;
        for (JAXBElement<?> elem : page.getPageOrGroupOrNavbar()) {
            if (elem.getValue() instanceof Navbar && ((Navbar) elem.getValue()).getPosition().equals(position)) {
                navbar = (Navbar) elem.getValue();
                break;
            }
        }
        return navbar;
    }

    /**
     * add the pagejump to the page´s navbar
     *
     * @param page
     * @param pagejump
     * @param position
     *            top, bottom, left, right navbar
     */
    private void addToNavbar(Page page, Pagejump pagejump, NavbarPositionType position) {
        Navbar navbar = getNavbar(page, position);
        if (navbar == null) {
            // create the navbar as it does not exist yet
            navbar = new Navbar();
            navbar.setPosition(position);
            navbar.setDynamic(true);
            page.getPageOrGroupOrNavbar().add(factory.createPageNavbar(navbar));
            switch (position) {
                case BOTTOM:
                    page.setShownavbarBottom(true);
                    break;
                case LEFT:
                    navbar.setWidth("200px");
                    page.setShownavbarLeft(true);
                    break;
                case RIGHT:
                    navbar.setWidth("200px");
                    page.setShownavbarRight(true);
                    break;
                case TOP:
                    // page.setShowtopnavigation(true);
                    page.setShownavbarTop(true);
                    break;
            }
        }
        if (NavbarPositionType.TOP.equals(position)) {
            pagejump.setLayout(createLayout(0));
        }
        navbar.getPageOrGroupOrLine().add(factory.createNavbarPagejump(pagejump));
    }

    public void addFormat(Object elem, String label) {
        try {
            Method method = elem.getClass().getMethod("setFormat", String.class);
            Matcher m = labelPattern.matcher(label);
            String format = null;
            if (m.matches() && !m.group(2).equals("%s")) { // ignore simple string formats, they work out-of-the box
                format = m.group(2);
                method.invoke(elem, format);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("{}", e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("{}", e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("{}", e.getMessage());
        }

    }

    /**
     * clean up the tree (e.g. remove groups with no visible elements)
     *
     * @param pages
     *            - Pages,Page,Group element wich should be checked
     */
    public void cleanup(Object page, Pages pages) {
        List<JAXBElement<?>> children = new ArrayList<JAXBElement<?>>();
        if (page instanceof Pages) {
            cleanup(((Pages) page).getPage(), pages);
        } else if (page instanceof Page) {
            children = ((Page) page).getPageOrGroupOrNavbar();
        } else if (page instanceof Group) {
            children = ((Group) page).getPageOrGroupOrLine();
        }
        List<JAXBElement<?>> childsToAdd = new ArrayList<JAXBElement<?>>();
        List<JAXBElement<?>> groupsToDelete = new ArrayList<JAXBElement<?>>();
        for (JAXBElement<?> element : children) {
            if (element.getValue() instanceof Page) {
                // check if this page only has invisible subpages and a pagejump
                // in the navbar => change the pagejump to the first subpage
                Page p = (Page) element.getValue();
                int visible = 0;
                Page firstChildPage = null;
                for (JAXBElement<?> ge : p.getPageOrGroupOrNavbar()) {
                    if (ge.getValue() instanceof Page) {
                        if (firstChildPage == null) {
                            firstChildPage = (Page) ge.getValue();
                        }
                        if (((Page) ge.getValue()).isVisible() == null
                                || ((Page) ge.getValue()).isVisible().booleanValue() == true) {
                            visible++;
                        }
                    } else if (ge.getValue() instanceof Group) {
                        visible++;
                    }
                }
                if (visible == 0 && firstChildPage != null) {
                    // find the pagejumps (only on the root page)
                    for (JAXBElement<?> e : pages.getPage().getPageOrGroupOrNavbar()) {
                        if (e.getValue() instanceof Navbar) {
                            Navbar navbar = (Navbar) e.getValue();
                            for (JAXBElement<?> ne : navbar.getPageOrGroupOrLine()) {
                                if (ne.getValue() instanceof Pagejump) {
                                    Pagejump pj = (Pagejump) ne.getValue();
                                    if (pj.getTarget().equals(p.getName())) {
                                        pj.setTarget(firstChildPage.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                cleanup(element.getValue(), pages);
            } else if (element.getValue() instanceof Group) {
                Group group = (Group) element.getValue();
                // check for visible elements
                int visible = 0;
                for (JAXBElement<?> ge : group.getPageOrGroupOrLine()) {
                    if (ge == null || ge.getValue() == null) {
                        continue;
                    }
                    if (ge.getValue() instanceof Page) {
                        Page p = (Page) ge.getValue();
                        if (p.isVisible() == null || p.isVisible().booleanValue() == true) {
                            visible++;
                        }
                    } else {
                        // all other elements are visible
                        visible++;
                    }
                }

                if (visible == 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("group '{}' has no visible elements", group.getName());
                    }
                    // group is empty move all pages to the groups parent page
                    // and delete the group
                    for (JAXBElement<?> ge : group.getPageOrGroupOrLine()) {
                        childsToAdd.add(ge);
                    }
                    groupsToDelete.add(element);
                }
                cleanup(group, pages);
            }
        }
        if (childsToAdd.size() > 0 && page instanceof Page) {
            if (logger.isTraceEnabled()) {
                logger.trace("there are '{}' children to be added to '{}'", childsToAdd.size(),
                        ((Page) page).getName());
            }
        }
        for (JAXBElement<?> element : childsToAdd) {
            if (page instanceof Page) {
                ((Page) page).getPageOrGroupOrNavbar().add(element);
            } else if (page instanceof Group) {
                ((Group) page).getPageOrGroupOrLine().add(element);
            }
        }
        for (JAXBElement<?> element : groupsToDelete) {
            if (page instanceof Page) {
                if (logger.isTraceEnabled()) {
                    logger.trace("removing group '{}' from '{}'", element, ((Page) page).getName());
                }
                ((Page) page).getPageOrGroupOrNavbar().remove(element);
            } else if (page instanceof Group) {
                if (logger.isTraceEnabled()) {
                    logger.trace("removing group '{}' from '{}'", element, ((Group) page).getName());
                }
                ((Group) page).getPageOrGroupOrLine().remove(element);
            }
        }
    }
}

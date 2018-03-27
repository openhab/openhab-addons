/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.config;

import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Setpoint;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.Webview;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.config.ConfigHelper.Transform;
import org.openhab.ui.cometvisu.internal.config.beans.Address;
import org.openhab.ui.cometvisu.internal.config.beans.Colorchooser;
import org.openhab.ui.cometvisu.internal.config.beans.Diagram;
import org.openhab.ui.cometvisu.internal.config.beans.Group;
import org.openhab.ui.cometvisu.internal.config.beans.Image;
import org.openhab.ui.cometvisu.internal.config.beans.Info;
import org.openhab.ui.cometvisu.internal.config.beans.LibVersion;
import org.openhab.ui.cometvisu.internal.config.beans.Mapping;
import org.openhab.ui.cometvisu.internal.config.beans.Meta;
import org.openhab.ui.cometvisu.internal.config.beans.NavbarPositionType;
import org.openhab.ui.cometvisu.internal.config.beans.ObjectFactory;
import org.openhab.ui.cometvisu.internal.config.beans.Page;
import org.openhab.ui.cometvisu.internal.config.beans.Pages;
import org.openhab.ui.cometvisu.internal.config.beans.Plugin;
import org.openhab.ui.cometvisu.internal.config.beans.Rrd;
import org.openhab.ui.cometvisu.internal.config.beans.SchemaPages;
import org.openhab.ui.cometvisu.internal.config.beans.Slide;
import org.openhab.ui.cometvisu.internal.config.beans.Switch;
import org.openhab.ui.cometvisu.internal.config.beans.Text;
import org.openhab.ui.cometvisu.internal.config.beans.Trigger;
import org.openhab.ui.cometvisu.internal.config.beans.Video;
import org.openhab.ui.cometvisu.internal.config.beans.Web;
import org.openhab.ui.cometvisu.internal.servlet.CometVisuApp;
import org.rrd4j.ConsolFun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Generates a CometVisu visu_config.xml settings file from an openHAB sitemap
 * the result of this automatic configuration should be taken as a basis for
 * customizing an own visu_config.xml, because thats the only way you can
 * benefit from all the features the CometVisu offers (e.g.
 * multi-column-layouts)
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 *
 */
public class VisuConfig {
    private final Logger logger = LoggerFactory.getLogger(VisuConfig.class);

    private Sitemap sitemap;

    /**
     * relative path to the config XSD schema
     */
    private final String schemaFile = "visu_config.xsd";

    private CometVisuApp app;

    private ConfigHelper configHelper;
    private File rootFolder;

    private ObjectFactory factory = new ObjectFactory();

    public VisuConfig(Sitemap sitemap, CometVisuApp app, File rootFolder) {
        this.sitemap = sitemap;
        this.app = app;
        this.rootFolder = rootFolder;
    }

    /**
     * generates a CometVisu config file from a sitemap
     *
     * @return valid XML config
     */
    public String getConfigXml(HttpServletRequest req) {
        SchemaPages pagesBean = new SchemaPages();
        pagesBean.setBackend("oh2");
        pagesBean.setDesign("metal");
        pagesBean.setMaxMobileScreenWidth(new BigDecimal(480));
        pagesBean.setBindClickToWidget(true);

        pagesBean.setLibVersion(BigInteger.valueOf(LibVersion.no));
        pagesBean.setScrollSpeed(new BigDecimal(0));

        // set relative path to XSD file
        File rootFolder = new File(Config.COMETVISU_WEBFOLDER);
        File sitemap = new File(rootFolder, req.getPathInfo());
        String relXsd = "";
        File parent = sitemap.getParentFile();
        File schema = new File(parent, "visu_config.xsd");
        while (parent != rootFolder && !schema.exists()) {
            parent = parent.getParentFile();
            relXsd += "../";
            schema = new File(parent, "visu_config.xsd");
        }
        pagesBean.setNoNamespaceSchemaLocation(relXsd + schemaFile);

        Meta meta = new Meta();
        pagesBean.setMeta(meta);

        configHelper = new ConfigHelper(pagesBean, app, sitemap.getName());
        createPages(pagesBean);

        return marshal(pagesBean, schema.getPath());
    }

    private String marshal(Pages bean, String xsdSchema) {
        String res = "";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(bean.getClass());
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = (xsdSchema == null || xsdSchema.trim().length() == 0) ? null
                    : schemaFactory.newSchema(new File(xsdSchema));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            marshaller.marshal(bean, sw);
            res = sw.toString();
        } catch (JAXBException e) {
            logger.error("{}", e.getMessage(), e);
        } catch (SAXException e) {
            logger.error("{}", e.getMessage(), e);
        }
        return res;
    }

    private Pages createPages(Pages pagesBean) {
        Page rootPage = new Page();
        rootPage.setName(sitemap.getName());
        pagesBean.setPage(rootPage);
        try {
            for (Widget widget : sitemap.getChildren()) {
                processWidget(rootPage, widget, pagesBean, 0);
            }
            configHelper.cleanup(rootPage, pagesBean);
        } catch (Exception e) {
            logger.error("", e);
            // show the exception in the config
            Text text = new Text();
            text.setFlavour("lithium");
            configHelper.addLabel(text, e.getClass() + ": " + e.getMessage());
            rootPage.getPageOrGroupOrNavbar().add(factory.createPageText(text));
        }
        return pagesBean;
    }

    /**
     * traverse recursively through the sitemap tree (breadth-first) and
     * generate the beans for later JAXB marshalling
     *
     * @param rootPage
     *            the current root page to which the widget should be added to
     * @param widget
     *            the openHAB widget, which will be
     * @param pages
     *            the root pages element of the config
     * @param level
     *            the current tree level
     */
    private void processWidget(Object rootPage, Widget widget, Pages pages, int level) {
        Item item = null;
        if (widget.getItem() != null) {
            try {
                item = app.getItemUIRegistry().getItem(widget.getItem());
            } catch (ItemNotFoundException e) {
                logger.debug("{}", e.getMessage());
            }
        }

        if (widget instanceof LinkableWidget) {
            EList<Widget> children = app.getItemUIRegistry().getChildren((LinkableWidget) widget);
            if (children.size() == 0) {
                processItemWidget(rootPage, widget, item, pages, level);
            } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Frame) {
                Group group = new Group();
                group.setLayout(configHelper.createLayout(6));
                group.setName(configHelper.getLabel(widget));
                configHelper.addToRoot(rootPage, factory.createPageGroup(group));
                for (Widget child : children) {
                    processWidget(group, child, pages, level + 1);
                }
            } else {
                Page page = new Page();
                page.setName(configHelper.getLabel(widget));
                configHelper.addToRoot(rootPage, factory.createPagePage(page));
                if (widget instanceof org.eclipse.smarthome.model.sitemap.Group) {
                    org.eclipse.smarthome.model.sitemap.Group group = (org.eclipse.smarthome.model.sitemap.Group) widget;
                    // add Group item to the Navbar
                    // logger.debug("page '{}' on level {}",page.getName(),level);
                    NavbarPositionType position = (level <= 1) ? NavbarPositionType.TOP : NavbarPositionType.LEFT;
                    Page barPage = (rootPage instanceof Page) ? (Page) rootPage : pages.getPage();
                    if (NavbarPositionType.TOP.equals(position)) {
                        // add top navbar always to the root page
                        barPage = pages.getPage();
                    }
                    // if (level==1) {
                    // configHelper.addSeparatorToNavbar(barPage, position,
                    // true);
                    // }

                    configHelper.addToNavbar(barPage, page, group, position, item);

                    // as the page is accessible via pagejump from a navbar we
                    // dont need the pagelink anymore
                    page.setVisible(false);
                }
                for (Widget child : children) {
                    processWidget(page, child, pages, level + 1);
                }
            }
        } else {
            processItemWidget(rootPage, widget, item, pages, level);
        }
    }

    private void processItemWidget(Object rootPage, Widget widget, Item item, Pages pages, int level) {
        if (widget instanceof org.eclipse.smarthome.model.sitemap.Switch) {
            org.eclipse.smarthome.model.sitemap.Switch switchWidget = (org.eclipse.smarthome.model.sitemap.Switch) widget;

            if (item instanceof RollershutterItem) {
                // in the demo-sitemap a rullershutter item is defined as
                // switch???
                configHelper.addRollershutter(rootPage, item, switchWidget);
            } else if (switchWidget.getMappings().size() > 0) {
                // up to 5 mapping we can use a multitrigger
                if (switchWidget.getMappings().size() <= 4) {
                    configHelper.mapToMultiTrigger(rootPage, item, switchWidget);
                } else {
                    configHelper.mapToTriggers(rootPage, item, switchWidget);
                }
            } else {

                Switch switchBean = new Switch();
                switchBean.setStyling("GreyGreen");
                switchBean.setMapping("OnOff");
                configHelper.addAddress(switchBean, item, Transform.SWITCH);
                configHelper.addLabel(switchBean, configHelper.getLabel(widget));

                configHelper.addMapping(switchBean, widget);
                configHelper.addStyling(switchBean, widget);
                configHelper.addToRoot(rootPage, factory.createPageSwitch(switchBean));
            }
        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Text) {
            Info info = new Info();
            Transform transform = Transform.STRING;
            boolean skipFormat = false;
            if (item instanceof ContactItem) {
                transform = Transform.CONTACT;
            } else if (item instanceof DateTimeItem) {
                transform = Transform.DATETIME;
                skipFormat = true; // dont use the defined format for datetime as the cometvisu can´t handle it
            }
            Address address = configHelper.addAddress(info, item, transform);
            if (address != null) {
                address.setMode("read");
                if (item != null && !skipFormat) {
                    configHelper.addFormat(info, item.getLabel());
                }
                configHelper.addLabel(info, widget);
                if (Transform.CONTACT.equals(transform)) {
                    info.setMapping("OpenClose");
                } else {
                    configHelper.addMapping(info, widget);
                }
                configHelper.addStyling(info, widget);
                configHelper.addToRoot(rootPage, factory.createPageInfo(info));
            } else {
                Text text = new Text();
                configHelper.addLabel(text, widget);
                configHelper.addStyling(text, widget);
                configHelper.addMapping(text, widget);
                configHelper.addToRoot(rootPage, factory.createPageText(text));
            }

        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Slider) {
            Slide bean = new Slide();
            bean.setFormat("%d%%");
            configHelper.addAddress(bean, item, Transform.DIMMER);
            configHelper.addLabel(bean, widget);
            configHelper.addToRoot(rootPage, factory.createPageSlide(bean));
        } else if (widget instanceof Setpoint) {
            Setpoint setpoint = (Setpoint) widget;
            Slide bean = new Slide();
            bean.setFormat("%d");
            bean.setMin(setpoint.getMinValue());
            bean.setMax(setpoint.getMaxValue());
            bean.setStep(setpoint.getStep());

            configHelper.addAddress(bean, item, Transform.DIMMER);
            configHelper.addLabel(bean, widget);

            configHelper.addToRoot(rootPage, factory.createPageSlide(bean));
        } else if (widget instanceof Selection) {
            Selection selection = (Selection) widget;
            // Map a Selection to a Group of triggers
            Group bean = new Group();
            bean.setNowidget(true);
            bean.setName(configHelper.getLabel(widget));

            Address address = configHelper.getAddress(item, Transform.NUMBER);
            String mappingName = String.valueOf(selection.getMappings().hashCode());
            Mapping mapping = configHelper.createMapping(mappingName, selection.getMappings());
            configHelper.addToMappings(mapping);

            for (org.eclipse.smarthome.model.sitemap.Mapping map : selection.getMappings()) {
                Trigger trigger = new Trigger();
                trigger.setValue(map.getCmd());
                trigger.setMapping(mappingName);
                trigger.getAddress().add(address);
                trigger.setLayout(configHelper.createLayout(Math.max(1, 6 / selection.getMappings().size())));

                configHelper.addToRoot(bean, factory.createPageTrigger(trigger));
            }

            configHelper.addToRoot(rootPage, factory.createPageGroup(bean));
        } else if (widget instanceof Webview) {
            Webview webview = (Webview) widget;
            Web bean = new Web();
            bean.setHeight(String.valueOf(webview.getHeight()) + "%");
            bean.setWidth("100%");
            bean.setSrc(webview.getUrl());
            bean.setRefresh(new BigDecimal(60));

            configHelper.addLabel(bean, widget);

            configHelper.addToRoot(rootPage, factory.createPageWeb(bean));
        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Image) {
            org.eclipse.smarthome.model.sitemap.Image image = (org.eclipse.smarthome.model.sitemap.Image) widget;
            Image bean = new Image();
            bean.setSrc(image.getUrl());
            bean.setRefresh(new BigDecimal(image.getRefresh()));

            configHelper.addLabel(bean, widget);

            configHelper.addToRoot(rootPage, factory.createPageImage(bean));
        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Video) {
            org.eclipse.smarthome.model.sitemap.Video video = (org.eclipse.smarthome.model.sitemap.Video) widget;
            Video bean = new Video();
            bean.setSrc(video.getUrl());

            configHelper.addLabel(bean, widget);

            configHelper.addToRoot(rootPage, factory.createPageVideo(bean));
        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Chart && item != null) {
            Plugin plugin = new Plugin();
            plugin.setName("diagram");
            configHelper.addPlugin(plugin);
            org.eclipse.smarthome.model.sitemap.Chart chart = (org.eclipse.smarthome.model.sitemap.Chart) widget;
            Diagram bean = new Diagram();
            bean.setSeries(configHelper.getCvChartPeriod(chart.getPeriod()));
            bean.setRefresh(new BigInteger(String.valueOf(chart.getRefresh())));
            bean.setHeight(configHelper.defaultChartHeight);

            if (item instanceof GroupItem) {
                for (Item member : ((GroupItem) item).getMembers()) {
                    Rrd rrd = new Rrd();
                    rrd.setValue(member.getName());
                    if (member instanceof NumberItem) {
                        rrd.setConsolidationFunction(ConsolFun.AVERAGE.toString());
                    } else {
                        rrd.setConsolidationFunction(ConsolFun.MAX.toString());
                    }
                    bean.getRrd().add(rrd);
                }
            } else {
                Rrd rrd = new Rrd();
                rrd.setValue(item.getName());
                if (item instanceof NumberItem) {
                    rrd.setConsolidationFunction(ConsolFun.AVERAGE.toString());
                } else {
                    rrd.setConsolidationFunction(ConsolFun.MAX.toString());
                }
                bean.getRrd().add(rrd);
            }

            configHelper.addToRoot(rootPage, factory.createPageDiagram(bean));
        } else if (widget instanceof org.eclipse.smarthome.model.sitemap.Colorpicker) {
            Plugin plugin = new Plugin();
            plugin.setName("colorchooser");
            configHelper.addPlugin(plugin);

            Colorchooser bean = new Colorchooser();
            configHelper.addAddress(bean, item, Transform.COLOR, "rgb");
            configHelper.addLabel(bean, widget);

            configHelper.addToRoot(rootPage, factory.createPageColorchooser(bean));
        } else {
            logger.error("unhandled widget '{}'", widget);
        }
    }

}

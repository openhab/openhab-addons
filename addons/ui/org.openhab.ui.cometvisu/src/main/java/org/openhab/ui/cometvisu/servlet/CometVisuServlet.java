/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.servlet;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.config.ConfigHelper.Transform;
import org.openhab.ui.cometvisu.internal.config.VisuConfig;
import org.openhab.ui.cometvisu.internal.editor.dataprovider.beans.DataBean;
import org.openhab.ui.cometvisu.internal.editor.dataprovider.beans.ItemBean;
import org.openhab.ui.cometvisu.internal.rrs.beans.Feed;
import org.openhab.ui.cometvisu.php.PHProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Servlet for CometVisu files
 *
 * @author Tobias Bräutigam
 */
public class CometVisuServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 4448918908615003303L;
    private static final Logger logger = LoggerFactory.getLogger(CometVisuServlet.class);

    private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.
    private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1
                                                                // week.
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    private Pattern sitemapPattern = Pattern.compile(".*/visu_config_(oh_)?([^\\.]+)\\.xml");
    private Pattern configStorePattern = Pattern.compile("config/visu_config_oh_([a-z0-9_]+)\\.xml");

    private String rrsLogPath = "/plugins/rsslog/rsslog_oh.php";
    private final String rssLogMessageSeparator = "\\|";
    private DateFormat rssPubDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    protected String root;
    protected File rootFolder;
    protected File userFileFolder;
    // protected String serverAlias;
    protected String defaultUserDir;
    protected PHProvider engine;
    protected ServletContext _servletContext;
    protected ServletConfig _config;

    protected boolean phpEnabled = false;

    private CometVisuApp cometVisuApp;

    public CometVisuServlet(String filesystemDir, CometVisuApp cometVisuApp) {
        root = filesystemDir;
        rootFolder = new File(root);
        userFileFolder = new File(org.eclipse.smarthome.config.core.ConfigConstants.getConfigFolder()
                + Config.COMETVISU_WEBAPP_USERFILE_FOLDER);
        defaultUserDir = System.getProperty("user.dir");
        this.cometVisuApp = cometVisuApp;

        PHProvider prov = cometVisuApp.getPHProvider();
        if (prov != null) {
            this.setPHProvider(prov);
        }
    }

    public void setPHProvider(PHProvider prov) {
        this.engine = prov;
        this.initQuercusEngine();
    }

    public void unsetPHProvider() {
        this.engine = null;
        this.phpEnabled = false;
    }

    private void initQuercusEngine() {
        try {
            this.engine.createQuercusEngine();
            this.engine.setIni("include_path", ".:" + rootFolder.getAbsolutePath());
            if (_servletContext != null) {
                this.engine.init(rootFolder.getAbsolutePath(), defaultUserDir, _servletContext);
                phpEnabled = true;
            }
        } catch (Exception e) {
            phpEnabled = false;
        }
    }

    /**
     * initialize the script manager.
     */
    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        _config = config;
        _servletContext = config.getServletContext();

        // init php service if available
        if (this.engine != null) {
            this.engine.init(rootFolder.getAbsolutePath(), defaultUserDir, _servletContext);
            phpEnabled = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File requestedFile = getRequestedFile(req);
        Matcher match = configStorePattern.matcher(req.getParameter("config"));
        if (requestedFile.getName().endsWith("save_config.php") && match.find() && req.getParameter("type") != null
                && req.getParameter("type").equals("xml")) {
            saveConfig(req, resp);
        } else {
            processPhpRequest(requestedFile, req, resp);
        }
    }

    private Sitemap getSitemap(String sitemapname) {
        for (SitemapProvider provider : cometVisuApp.getSitemapProviders()) {
            Sitemap sitemap = provider.getSitemap(sitemapname);
            if (sitemap != null) {
                return sitemap;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File requestedFile = getRequestedFile(req);

        String path = req.getPathInfo() != null ? req.getPathInfo() : "/index.html";
        Matcher matcher = sitemapPattern.matcher(path);
        if (matcher.find()) {
            // add headers for cometvisu clients autoconfiguration
            resp.setHeader("X-CometVisu-Backend-LoginUrl",
                    "/rest/" + Config.COMETVISU_BACKEND_ALIAS + "/" + Config.COMETVISU_BACKEND_LOGIN_ALIAS);
            resp.setHeader("X-CometVisu-Backend-Name", "openhab2");

            // serve autogenerated config from openhab sitemap if no real config file exists
            if (!requestedFile.exists()) {
                Sitemap sitemap = getSitemap(matcher.group(2));
                if (sitemap != null) {
                    logger.debug("reading sitemap '{}'", sitemap);
                    VisuConfig config = new VisuConfig(sitemap, cometVisuApp, rootFolder);

                    // logger.info("response: "+config.getConfigXml());
                    resp.setContentType(MediaType.APPLICATION_XML);
                    resp.getWriter().write(config.getConfigXml(req));
                    resp.flushBuffer();

                    return;
                } else {
                    logger.debug("Config file not found. Neither as normal config ('{}') nor as sitemap ('{}.sitemap')",
                            requestedFile, matcher.group(2));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }
        // logger.info("Path: " + req.getPathInfo());
        if (path.matches(".*editor/dataproviders/.+\\.(php|json)$") || path.matches(".*designs/get_designs\\.php$")) {
            dataProviderService(requestedFile, req, resp);
        } else if (path.equalsIgnoreCase(rrsLogPath)) {
            processRssLogRequest(requestedFile, req, resp);
        } else if (requestedFile.getName().endsWith(".php")) {
            processPhpRequest(requestedFile, req, resp);
        } else {
            processStaticRequest(requestedFile, req, resp, true);
        }
    }

    protected void processPhpRequest(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!this.phpEnabled) {
            // try to initialize the php service
            initQuercusEngine();
        }
        if (this.phpEnabled) {
            this.engine.phpService(file, request, response);
        } else {
            logger.debug("php service is not available please install com.caucho.quercus bundle");
        }
    }

    protected File getRequestedFile(HttpServletRequest req) throws UnsupportedEncodingException {
        String requestedFile = req.getPathInfo();
        File file = null;

        // check services folder if a file exists there
        if (requestedFile != null) {
            file = new File(userFileFolder, URLDecoder.decode(requestedFile, "UTF-8"));
        }
        // serve the file from the cometvisu src directory
        if (file == null || !file.exists() || file.isDirectory()) {
            file = requestedFile != null ? new File(rootFolder, URLDecoder.decode(requestedFile, "UTF-8")) : rootFolder;
        }
        if (file.isDirectory()) {
            // search for an index file
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("index.") && (name.endsWith(".php") || name.endsWith(".html"));
                }
            };
            for (String dirFile : file.list(filter)) {
                // take the first one found
                file = new File(file, dirFile);
                break;
            }
        }
        return file;
    }

    /**
     * serves an RSS-Feed from a persisted string item backend for the CometVisu
     * rrslog-plugin
     *
     * @param file
     * @param request
     * @param response
     */
    private void processRssLogRequest(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // retrieve the item
        if (request.getParameter("f") == null) {
            return;
        }

        String[] itemNames = request.getParameter("f").split(",");
        List<Item> items = new ArrayList<Item>();

        for (String name : itemNames) {
            try {
                Item item = cometVisuApp.getItemRegistry().getItem(name);
                items.add(item);
            } catch (ItemNotFoundException e) {
                logger.error("item '{}' not found", name);
            }
        }

        if (items.size() > 0) {
            // Fallback to first persistenceService from list
            if (!CometVisuApp.getPersistenceServices().entrySet().iterator().hasNext()) {
                throw new IllegalArgumentException("No Persistence service found.");
            }

            if (request.getParameter("c") != null) {
                if (items.size() == 1) {
                    // new log message should be store
                    String title = request.getParameter("h");
                    String message = request.getParameter("c");
                    String state = request.getParameter("state");
                    // Build message
                    Command command = new StringType(title + rssLogMessageSeparator + message + rssLogMessageSeparator
                            + state + rssLogMessageSeparator + items.get(0).getName());
                    // Use the event publisher to store the item in the defined
                    // persistance services
                    cometVisuApp.getEventPublisher()
                            .post(ItemEventFactory.createCommandEvent(items.get(0).getName(), command));
                }
                // send empty response??
                response.setContentType("text/plain");
                response.getWriter().write("");
                response.flushBuffer();
            } else if (request.getParameter("dump") != null) {
            } else if (request.getParameter("r") != null) {
                // delete all log lines older than the timestamp and optional a
                // filter
                // => not possible to remove data from persistence service
                response.setContentType("text/plain");
                response.getWriter().write(
                        "Cannot execute query: It is not possible to delete data from openHAB PersistenceService");
                response.flushBuffer();
            } else if (request.getParameter("u") != null) {
                // update state
                response.setContentType("text/plain");
                response.getWriter().write(
                        "Cannot execute query: It is not possible to update data from openHAB PersistenceService");
                response.flushBuffer();
            } else if (request.getParameter("d") != null) {
                // update state
                response.setContentType("text/plain");
                response.getWriter().write(
                        "Cannot execute query: It is not possible to delete data from openHAB PersistenceService");
                response.flushBuffer();
            } else {
                Feed feed = new Feed();
                feed.feedUrl = request.getRequestURL().toString();
                feed.title = "RSS supplied logs";
                feed.link = request.getRequestURL().toString();
                feed.author = "";
                feed.description = "RSS supplied logs";
                feed.type = "rss20";
                // Define the data filter
                FilterCriteria filter = new FilterCriteria();
                Calendar start = Calendar.getInstance();
                // retrieve only the historic states from the last 7 days + BeginDate is required for RRD4j service
                start.add(Calendar.DAY_OF_YEAR, -7);
                // Date end = new Date();
                filter.setBeginDate(start.getTime());
                // filter.setEndDate(end);
                filter.setPageSize(25);
                filter.setOrdering(Ordering.DESCENDING);

                for (Item item : items) {
                    filter.setItemName(item.getName());
                    Iterator<Entry<String, QueryablePersistenceService>> pit = CometVisuApp.getPersistenceServices()
                            .entrySet().iterator();
                    QueryablePersistenceService persistenceService = pit.next().getValue();
                    // Get the data from the persistence store
                    Iterable<HistoricItem> result = persistenceService.query(filter);
                    Iterator<HistoricItem> it = result.iterator();
                    boolean forceStop = false;
                    while (!forceStop && !it.hasNext()) {
                        if (pit.hasNext()) {
                            persistenceService = pit.next().getValue();
                            result = persistenceService.query(filter);
                        } else {
                            // no persisted data found for this item in any of
                            // the available persistence services
                            forceStop = true;
                        }
                    }
                    if (it.hasNext()) {
                        logger.debug("persisted data for item {} found in service {}", item.getName(),
                                persistenceService.getId());
                    }

                    // Iterate through the data
                    int i = 0;
                    while (it.hasNext()) {
                        i++;
                        HistoricItem historicItem = it.next();
                        if (historicItem.getState() == null || historicItem.getState().toString().isEmpty()) {
                            continue;
                        }
                        org.openhab.ui.cometvisu.internal.rrs.beans.Entry entry = new org.openhab.ui.cometvisu.internal.rrs.beans.Entry();
                        entry.publishedDate = historicItem.getTimestamp().getTime();
                        logger.info(rssPubDateFormat.format(entry.publishedDate) + ": " + historicItem.getState());
                        entry.tags = historicItem.getName();
                        String[] content = historicItem.getState().toString().split(rssLogMessageSeparator);
                        if (content.length == 0) {
                            entry.content = historicItem.getState().toString();
                        } else if (content.length == 1) {
                            entry.content = content[0];
                        } else if (content.length == 2) {
                            entry.title = content[0];
                            entry.content = content[1];
                        } else if (content.length == 3) {
                            entry.title = content[0];
                            entry.content = content[1];
                            entry.state = content[2];
                        } else if (content.length == 4) {
                            entry.title = content[0];
                            entry.content = content[1];
                            entry.state = content[2];
                            // ignore tags in content[3] as is is already known
                            // by item name
                        }
                        feed.entries.add(entry);
                    }
                    if ("rrd4j".equals(persistenceService.getId())
                            && FilterCriteria.Ordering.DESCENDING.equals(filter.getOrdering())) {
                        // the RRD4j PersistenceService does not support descending ordering so we do it manually
                        Collections.sort(feed.entries,
                                new Comparator<org.openhab.ui.cometvisu.internal.rrs.beans.Entry>() {
                                    @Override
                                    public int compare(org.openhab.ui.cometvisu.internal.rrs.beans.Entry o1,
                                            org.openhab.ui.cometvisu.internal.rrs.beans.Entry o2) {
                                        return Long.compare(o2.publishedDate, o1.publishedDate);
                                    }
                                });
                    }
                    logger.debug("querying {} item from {} to {} => {} results on service {}", filter.getItemName(),
                            filter.getBeginDate(), filter.getEndDate(), i, persistenceService.getId());
                }
                if (request.getParameter("j") != null) {
                    // request data in JSON format
                    response.setContentType("application/json");
                    response.getWriter().write("{\"responseData\": { \"feed\": " + marshalJson(feed)
                            + "},\"responseDetails\":null,\"responseStatus\":200}");
                } else {
                    // request data in RSS format
                    response.setContentType(MediaType.APPLICATION_ATOM_XML);
                    // as the json bean structure does not map the rss structure
                    // we cannot just marshal an XML
                    String rss = "<?xml version=\"1.0\"?>\n<rss version=\"2.0\">\n<channel>\n";
                    rss += "<title>" + feed.title + "</title>\n";
                    rss += "<link>" + feed.link + "</link>\n";
                    rss += "<desrciption>" + feed.description + "</desription>\n";

                    for (org.openhab.ui.cometvisu.internal.rrs.beans.Entry entry : feed.entries) {
                        rss += "<item>";
                        rss += "<title>" + entry.title + "</title>";
                        rss += "<description>" + entry.content + "</description>";
                        Date pubDate = new Date(entry.publishedDate);
                        rss += "<pubDate>" + rssPubDateFormat.format(pubDate) + "</pubDate>";
                        rss += "</item>\n";
                    }

                    rss += "</channel></rss>";
                    response.getWriter().write(rss);

                }
                response.flushBuffer();

            }
        }

    }

    /**
     * Process the actual request.
     *
     * @param request
     *            The request to be processed.
     * @param response
     *            The response to be created.
     * @param content
     *            Whether the request body should be written (GET) or not
     *            (HEAD).
     * @throws IOException
     *             If something fails at I/O level.
     *
     * @author BalusC
     * @link
     *       http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and
     *       .html
     */
    private void processStaticRequest(File file, HttpServletRequest request, HttpServletResponse response,
            boolean content) throws IOException {
        // Validate the requested file
        // ------------------------------------------------------------

        if (file == null) {
            // Get requested file by path info.
            String requestedFile = request.getPathInfo();

            // Check if file is actually supplied to the request URL.
            if (requestedFile == null) {
                // Do your thing if the file is not supplied to the request URL.
                // Throw an exception, or send 404, or show default/warning
                // page, or
                // just ignore it.
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // URL-decode the file name (might contain spaces and on) and
            // prepare
            // file object.
            file = new File(rootFolder, URLDecoder.decode(requestedFile, "UTF-8"));
        }
        // Check if file actually exists in filesystem.
        if (!file.exists()) {
            // Do your thing if the file appears to be non-existing.
            // Throw an exception, or send 404, or show default/warning page, or
            // just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Prepare some variables. The ETag is an unique identifier of the file.
        String fileName = file.getName();
        long length = file.length();
        long lastModified = file.lastModified();
        String eTag = fileName + "_" + length + "_" + lastModified;
        long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;

        // Validate request headers for caching
        // ---------------------------------------------------

        // If-None-Match header should contain "*" or ETag. If so, then return
        // 304.
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setHeader("ETag", eTag); // Required in 304.
            response.setDateHeader("Expires", expires); // Postpone cache with 1
                                                        // week.
            return;
        }

        // If-Modified-Since header should be greater than LastModified. If so,
        // then return 304.
        // This header is ignored if any If-None-Match header is specified.
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setHeader("ETag", eTag); // Required in 304.
            response.setDateHeader("Expires", expires); // Postpone cache with 1
                                                        // week.
            return;
        }

        // Validate request headers for resume
        // ----------------------------------------------------

        // If-Match header should contain "*" or ETag. If not, then return 412.
        String ifMatch = request.getHeader("If-Match");
        if (ifMatch != null && !matches(ifMatch, eTag)) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        // If-Unmodified-Since header should be greater than LastModified. If
        // not, then return 412.
        long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
        if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        // Validate and process range
        // -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<Range>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader("Range");
        if (range != null) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not,
            // then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length); // Required
                                                                          // in
                                                                          // 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // If-Range header should either match ETag or be greater then
            // LastModified. If not,
            // then return full file.
            String ifRange = request.getHeader("If-Range");
            if (ifRange != null && !ifRange.equals(eTag)) {
                try {
                    long ifRangeTime = request.getDateHeader("If-Range"); // Throws
                                                                          // IAE
                                                                          // if
                                                                          // invalid.
                    if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }

            // If any valid If-Range header, then process each part of byte
            // range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following
                    // examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20
                    // (length-20=80 to length=100).
                    long start = sublong(part, 0, part.indexOf("-"));
                    long end = sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then
                    // return 416.
                    if (start > end) {
                        response.setHeader("Content-Range", "bytes */" + length); // Required
                                                                                  // in
                                                                                  // 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    ranges.add(new Range(start, end, length));
                }
            }
        }

        // Prepare and initialize response
        // --------------------------------------------------------

        // Get content type by file name and set default GZIP support and
        // content disposition.
        String contentType = getServletContext().getMimeType(fileName);
        boolean acceptsGzip = false;
        String disposition = "inline";

        // If content type is unknown, then set the default value.
        // For all content types, see:
        // http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // If content type is text, then determine whether GZIP content encoding
        // is supported by
        // the browser and expand content type with the one and right character
        // encoding.
        if (contentType.startsWith("text")) {
            String acceptEncoding = request.getHeader("Accept-Encoding");
            acceptsGzip = acceptEncoding != null && accepts(acceptEncoding, "gzip");
            contentType += ";charset=UTF-8";
        }

        // Else, expect for images, determine content disposition. If content
        // type is supported by
        // the browser, then set to inline, else attachment which will pop a
        // 'save as' dialogue.
        else if (!contentType.startsWith("image")) {
            String accept = request.getHeader("Accept");
            disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
        }

        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", eTag);
        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", expires);

        // Send requested file (part(s)) to client
        // ------------------------------------------------

        // Prepare streams.
        RandomAccessFile input = null;
        OutputStream output = null;

        try {
            // Open streams.
            input = new RandomAccessFile(file, "r");
            output = response.getOutputStream();

            if (ranges.isEmpty() || ranges.get(0) == full) {

                // Return full file.
                Range r = full;
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);

                if (content) {
                    if (acceptsGzip) {
                        // The browser accepts GZIP, so GZIP the content.
                        response.setHeader("Content-Encoding", "gzip");
                        output = new GZIPOutputStream(output, DEFAULT_BUFFER_SIZE);
                    } else {
                        // Content length is not directly predictable in case of
                        // GZIP.
                        // So only add it if there is no means of GZIP, else
                        // browser will hang.
                        response.setHeader("Content-Length", String.valueOf(r.length));
                    }

                    // Copy full range.
                    copy(input, output, r.start, r.length);
                }

            } else if (ranges.size() == 1) {

                // Return single part of file.
                Range r = ranges.get(0);
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                response.setHeader("Content-Length", String.valueOf(r.length));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Copy single part range.
                    copy(input, output, r.start, r.length);
                }

            } else {

                // Return multiple parts of file.
                response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Cast back to ServletOutputStream to get the easy println
                    // methods.
                    ServletOutputStream sos = (ServletOutputStream) output;

                    // Copy multi part range.
                    for (Range r : ranges) {
                        // Add multipart boundary and header fields for every
                        // range.
                        sos.println();
                        sos.println("--" + MULTIPART_BOUNDARY);
                        sos.println("Content-Type: " + contentType);
                        sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

                        // Copy single part range of multi part range.
                        copy(input, output, r.start, r.length);
                    }

                    // End with multipart boundary.
                    sos.println();
                    sos.println("--" + MULTIPART_BOUNDARY + "--");
                }
            }
        } finally {
            // Gently close streams.
            close(output);
            close(input);
        }
    }

    /**
     * Save config file send by editor
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private final void saveConfig(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getParameter("config");
        File file = new File(userFileFolder, URLDecoder.decode(fileName, "UTF-8"));

        response.setContentType(MediaType.APPLICATION_JSON);

        class Response {
            public Boolean success = false;
            public String message = "";

            Response() {
            }
        }
        Response resp = new Response();

        if (file.exists()) {
            // file exists and we only write if the file exists (creating new files this way is prohibited for security
            // reasons
            logger.debug("save config file'{}' requested", file);

            // check is backup folder exists
            File backupFolder = new File(userFileFolder, "config/backup/");
            boolean backup = true;
            if (!backupFolder.exists()) {
                try {
                    backupFolder.mkdir();
                } catch (SecurityException e) {
                    logger.error("Error creating backup directory for CometVisu config files");
                    backup = false;
                }
            }

            if (backup) {
                // Backup existing file
                File backupFile = new File(backupFolder, file.getName() + "-" + System.currentTimeMillis());
                FileUtils.copyFile(file, backupFile);
            }

            // write data to file
            String data = request.getParameter("data");
            FileUtils.writeStringToFile(file, data);
            resp.success = true;
            resp.message = "File saved";
        }
        response.getWriter().write(marshalJson(resp));
        response.flushBuffer();
    }

    /**
     * replaces the dataproviders in
     * <cometvisu-src>/editor/dataproviders/*.(php|json) +
     *
     * @param file
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private final void dataProviderService(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("dataprovider '{}' requested", file.getName());
        List<Object> beans = new ArrayList<Object>();
        if (file.getName().equals("dpt_list.json")) {
            // return all transforms available for openhab
            for (Transform transform : Transform.values()) {
                DataBean bean = new DataBean();
                bean.label = transform.toString().toLowerCase();
                bean.value = "OH:" + bean.label;
                beans.add(bean);
            }
        } else if (file.getName().equals("list_all_addresses.php")) {
            // all item names
            for (Item item : this.cometVisuApp.getItemRegistry().getItems()) {
                ItemBean bean = new ItemBean();
                bean.value = item.getName();
                bean.label = item.getName();
                // TODO handle other types
                bean.hints.put("transform", "OH:string");
                beans.add(bean);
            }

        } else if (file.getName().equals("list_all_icons.php")) {
            // all item names
            File iconDir = new File(rootFolder, "icon/knx-uf-iconset/128x128_white/");
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    // TODO Auto-generated method stub
                    return name.endsWith(".png");
                }
            };
            File[] icons = iconDir.listFiles(filter);
            Arrays.sort(icons);
            for (File iconFile : icons) {
                if (iconFile.isFile()) {
                    String iconName = iconFile.getName().replace(".png", "");
                    DataBean bean = new DataBean();
                    bean.label = iconName;
                    bean.value = iconName;
                    beans.add(bean);
                }
            }
        } else if (file.getName().equals("list_all_plugins.php")) {
            // all plugins
            // all item names
            File pluginDir = new File(rootFolder, "plugins/");
            File[] plugins = pluginDir.listFiles();
            Arrays.sort(plugins);
            for (File icon : plugins) {
                if (icon.isDirectory()) {
                    DataBean bean = new DataBean();
                    bean.label = icon.getName();
                    bean.value = icon.getName();
                    beans.add(bean);
                }
            }

        } else if (file.getName().equals("get_designs.php")) {
            // all designs
            File designDir = new File(rootFolder, "designs/");
            File[] designs = designDir.listFiles();
            Arrays.sort(designs);
            for (File design : designs) {
                if (design.isDirectory()) {
                    beans.add(design.getName());
                }
            }

        } else if (file.getName().equals("list_all_rrds.php")) {
            // all item names

        }
        response.setContentType(MediaType.APPLICATION_JSON);
        response.getWriter().write(marshalJson(beans));
        response.flushBuffer();
    }

    private String marshalJson(Object bean) {
        Gson gson = new Gson();
        return gson.toJson(bean);
    }

    /**
     * Returns true if the given accept header accepts the given value.
     *
     * @param acceptHeader
     *            The accept header.
     * @param toAccept
     *            The value to be accepted.
     * @return True if the given accept header accepts the given value.
     */
    private static boolean accepts(String acceptHeader, String toAccept) {
        String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
        Arrays.sort(acceptValues);
        return Arrays.binarySearch(acceptValues, toAccept) > -1
                || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                || Arrays.binarySearch(acceptValues, "*/*") > -1;
    }

    /**
     * Returns true if the given match header matches the given value.
     *
     * @param matchHeader
     *            The match header.
     * @param toMatch
     *            The value to be matched.
     * @return True if the given match header matches the given value.
     */
    private static boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1 || Arrays.binarySearch(matchValues, "*") > -1;
    }

    /**
     * Returns a substring of the given string value from the given begin index
     * to the given end index as a long. If the substring is empty, then -1 will
     * be returned
     *
     * @param value
     *            The string value to return a substring as long for.
     * @param beginIndex
     *            The begin index of the substring to be returned as long.
     * @param endIndex
     *            The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or -1 if substring
     *         is empty.
     */
    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    /**
     * Copy the given byte range of the given input to the given output.
     *
     * @param input
     *            The input to copy the given range to the given output for.
     * @param output
     *            The output to copy the given range from the given input for.
     * @param start
     *            Start of the byte range.
     * @param length
     *            Length of the byte range.
     * @throws IOException
     *             If something fails at I/O level.
     */
    private static void copy(RandomAccessFile input, OutputStream output, long start, long length) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;

        if (input.length() == length) {
            // Write full range.
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } else {
            // Write partial range.
            input.seek(start);
            long toRead = length;

            while ((read = input.read(buffer)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    break;
                }
            }
        }
    }

    /**
     * Close the given resource.
     *
     * @param resource
     *            The resource to be closed.
     */
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
                // Ignore IOException. If you want to handle this anyway, it
                // might be useful to know
                // that this will generally only be thrown when the client
                // aborted the request.
            }
        }
    }

    /**
     * This class represents a byte range.
     */
    protected class Range {
        long start;
        long end;
        long length;
        long total;

        /**
         * Construct a byte range.
         *
         * @param start
         *            Start of the byte range.
         * @param end
         *            End of the byte range.
         * @param total
         *            Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

    }
}

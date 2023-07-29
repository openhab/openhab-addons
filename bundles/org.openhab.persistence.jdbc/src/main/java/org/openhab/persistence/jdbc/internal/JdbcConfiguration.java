/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.jdbc.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.persistence.jdbc.internal.db.JdbcBaseDAO;
import org.openhab.persistence.jdbc.internal.utils.MovingAverage;
import org.openhab.persistence.jdbc.internal.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
@NonNullByDefault
public class JdbcConfiguration {
    private final Logger logger = LoggerFactory.getLogger(JdbcConfiguration.class);

    private static final Pattern EXTRACT_CONFIG_PATTERN = Pattern.compile("^(.*?)\\.([0-9.a-zA-Z]+)$");
    private static final String DB_DAO_PACKAGE = "org.openhab.persistence.jdbc.internal.db.Jdbc";

    private Map<Object, Object> configuration;

    private @NonNullByDefault({}) JdbcBaseDAO dBDAO;
    private @Nullable String dbName;
    boolean dbConnected = false;
    boolean driverAvailable = false;

    private @Nullable String serviceName;
    private String name = "jdbc";
    public final boolean valid;

    // private String url;
    // private String user;
    // private String password;
    private int numberDecimalcount = 3;
    private boolean tableUseRealItemNames = false;
    private boolean tableCaseSensitiveItemNames = false;
    private String itemsManageTable = "items";
    private String tableNamePrefix = "item";
    private int tableIdDigitCount = 4;
    private boolean rebuildTableNames = false;

    private int errReconnectThreshold = 0;

    public int timerCount = 0;
    public int time1000Statements = 0;
    public long timer1000 = 0;
    public MovingAverage timeAverage50arr = new MovingAverage(50);
    public MovingAverage timeAverage100arr = new MovingAverage(100);
    public MovingAverage timeAverage200arr = new MovingAverage(200);
    public boolean enableLogTime = false;

    public JdbcConfiguration(Map<Object, Object> configuration) {
        logger.debug("JDBC::JdbcConfiguration");
        this.configuration = configuration;
        valid = updateConfig();
    }

    private boolean updateConfig() {
        logger.debug("JDBC::updateConfig: configuration size = {}", configuration.size());

        String user = (String) configuration.get("user");
        String password = (String) configuration.get("password");

        // mandatory url
        String url = (String) configuration.get("url");

        if (url == null) {
            logger.error("Mandatory url parameter is missing in configuration!");
            return false;
        }

        Properties parsedURL = StringUtilsExt.parseJdbcURL(url);

        if (user == null || user.isBlank()) {
            logger.debug("No jdbc:user parameter defined in jdbc.cfg");
        }
        if (password == null || password.isBlank()) {
            logger.debug("No jdbc:password parameter defined in jdbc.cfg.");
        }

        if (url.isBlank()) {
            logger.debug(
                    "JDBC url is missing - please configure in jdbc.cfg like 'jdbc:<service>:<host>[:<port>;<attributes>]'");
            return false;
        }

        if ("false".equalsIgnoreCase(parsedURL.getProperty("parseValid"))) {
            Enumeration<?> en = parsedURL.propertyNames();
            String enstr = "";
            for (Object key : Collections.list(en)) {
                enstr += key + " = " + parsedURL.getProperty("" + key) + "\n";
            }
            logger.warn(
                    "JDBC url is not well formatted: {}\nPlease configure in openhab.cfg like 'jdbc:<service>:<host>[:<port>;<attributes>]'",
                    enstr);
            return false;
        }

        logger.debug("JDBC::updateConfig: user={}", user);
        logger.debug("JDBC::updateConfig: password exists? {}", password != null && !password.isBlank());
        logger.debug("JDBC::updateConfig: url={}", url);

        // set database type and database type class
        setDBDAOClass(parsedURL.getProperty("dbShortcut")); // derby, h2, hsqldb, mariadb, mysql, postgresql,
                                                            // sqlite, timescaledb
        // set user
        if (user != null && !user.isBlank()) {
            dBDAO.databaseProps.setProperty("dataSource.user", user);
        }

        // set password
        if (password != null && !password.isBlank()) {
            dBDAO.databaseProps.setProperty("dataSource.password", password);
        }

        // set sql-types from external config
        setSqlTypes();

        final Pattern isNumericPattern = Pattern.compile("\\d+(\\.\\d+)?");
        String et = (String) configuration.get("reconnectCnt");
        if (et != null && !et.isBlank() && isNumericPattern.matcher(et).matches()) {
            errReconnectThreshold = Integer.parseInt(et);
            logger.debug("JDBC::updateConfig: errReconnectThreshold={}", errReconnectThreshold);
        }

        String mt = (String) configuration.get("itemsManageTable");
        if (mt != null && !mt.isBlank()) {
            itemsManageTable = mt;
            logger.debug("JDBC::updateConfig: itemsManageTable={}", itemsManageTable);
        }

        String np = (String) configuration.get("tableNamePrefix");
        if (np != null && !np.isBlank()) {
            tableNamePrefix = np;
            logger.debug("JDBC::updateConfig: tableNamePrefix={}", tableNamePrefix);
        }

        String dd = (String) configuration.get("numberDecimalcount");
        if (dd != null && !dd.isBlank() && isNumericPattern.matcher(dd).matches()) {
            numberDecimalcount = Integer.parseInt(dd);
            logger.debug("JDBC::updateConfig: numberDecimalcount={}", numberDecimalcount);
        }

        String rn = (String) configuration.get("tableUseRealItemNames");
        if (rn != null && !rn.isBlank()) {
            tableUseRealItemNames = "true".equals(rn) ? Boolean.parseBoolean(rn) : false;
            logger.debug("JDBC::updateConfig: tableUseRealItemNames={}", tableUseRealItemNames);
        }

        String lc = (String) configuration.get("tableCaseSensitiveItemNames");
        if (lc != null && !lc.isBlank()) {
            tableCaseSensitiveItemNames = Boolean.parseBoolean(lc);
            logger.debug("JDBC::updateConfig: tableCaseSensitiveItemNames={}", tableCaseSensitiveItemNames);
        }

        String td = (String) configuration.get("tableIdDigitCount");
        if (td != null && !td.isBlank() && isNumericPattern.matcher(td).matches()) {
            tableIdDigitCount = Integer.parseInt(td);
            logger.debug("JDBC::updateConfig: tableIdDigitCount={}", tableIdDigitCount);
        }

        String rt = (String) configuration.get("rebuildTableNames");
        if (rt != null && !rt.isBlank()) {
            rebuildTableNames = Boolean.parseBoolean(rt);
            logger.debug("JDBC::updateConfig: rebuildTableNames={}", rebuildTableNames);
        }

        // undocumented
        String ac = (String) configuration.get("maximumPoolSize");
        if (ac != null && !ac.isBlank()) {
            dBDAO.databaseProps.setProperty("maximumPoolSize", ac);
        }

        // undocumented
        String ic = (String) configuration.get("minimumIdle");
        if (ic != null && !ic.isBlank()) {
            dBDAO.databaseProps.setProperty("minimumIdle", ic);
        }

        // undocumented
        String it = (String) configuration.get("idleTimeout");
        if (it != null && !it.isBlank()) {
            dBDAO.databaseProps.setProperty("idleTimeout", it);
        }
        // undocumented
        String ent = (String) configuration.get("enableLogTime");
        if (ent != null && !ent.isBlank()) {
            enableLogTime = "true".equals(ent) ? Boolean.parseBoolean(ent) : false;
        }
        logger.debug("JDBC::updateConfig: enableLogTime {}", enableLogTime);

        // undocumented
        String fd = (String) configuration.get("driverClassName");
        if (fd != null && !fd.isBlank()) {
            dBDAO.databaseProps.setProperty("driverClassName", fd);
        }

        // undocumented
        String ds = (String) configuration.get("dataSourceClassName");
        if (ds != null && !ds.isBlank()) {
            dBDAO.databaseProps.setProperty("dataSourceClassName", ds);
        }

        // undocumented
        String dn = dBDAO.databaseProps.getProperty("driverClassName");
        if (dn == null) {
            dn = dBDAO.databaseProps.getProperty("dataSourceClassName");
        } else {
            dBDAO.databaseProps.setProperty("jdbcUrl", url);
        }

        // test if JDBC driver bundle is available
        testJDBCDriver(dn);

        logger.debug("JDBC::updateConfig: configuration complete. service={}", getName());

        return true;
    }

    private void setDBDAOClass(String sn) {
        String serviceName;

        // set database type
        if (sn.isBlank() || sn.length() < 2) {
            logger.error(
                    "JDBC::updateConfig: Required database url like 'jdbc:<service>:<host>[:<port>;<attributes>]' - please configure the jdbc:url parameter in openhab.cfg");
            serviceName = "none";
        } else {
            serviceName = sn;
        }
        this.serviceName = serviceName;
        logger.debug("JDBC::updateConfig: found serviceName = '{}'", serviceName);

        // set class for database type
        String ddp = DB_DAO_PACKAGE + serviceName.toUpperCase().charAt(0) + serviceName.toLowerCase().substring(1)
                + "DAO";

        logger.debug("JDBC::updateConfig: Init Data Access Object Class: '{}'", ddp);
        try {
            dBDAO = (JdbcBaseDAO) Class.forName(ddp).getConstructor().newInstance();
            logger.debug("JDBC::updateConfig: dBDAO ClassName={}", dBDAO.getClass().getName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                | NoSuchMethodException e) {
            logger.error("JDBC::updateConfig: Exception: {}", e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.warn("JDBC::updateConfig: no Configuration for serviceName '{}' found. ClassNotFoundException: {}",
                    serviceName, e.getMessage());
            logger.debug("JDBC::updateConfig: using default Database Configuration: JdbcBaseDAO !!");
            dBDAO = new JdbcBaseDAO();
            logger.debug("JDBC::updateConfig: dBConfig done");
        }
    }

    private void setSqlTypes() {
        Set<Object> keys = configuration.keySet();

        for (Object k : keys) {
            String key = (String) k;
            Matcher matcher = EXTRACT_CONFIG_PATTERN.matcher(key);
            if (!matcher.matches()) {
                continue;
            }
            matcher.reset();
            matcher.find();
            if (!"sqltype".equals(matcher.group(1))) {
                continue;
            }
            String itemType = matcher.group(2);
            if (!itemType.startsWith("table")) {
                itemType = itemType.toUpperCase() + "ITEM";
            }
            String value = (String) configuration.get(key);
            logger.debug("JDBC::updateConfig: set sqlTypes: itemType={} value={}", itemType, value);
            if (value != null) {
                dBDAO.sqlTypes.put(itemType, value);
            }
        }
    }

    private void testJDBCDriver(String driver) {
        driverAvailable = true;
        try {
            Class.forName(driver);
            logger.debug("JDBC::updateConfig: load JDBC-driverClass was successful: '{}'", driver);
        } catch (ClassNotFoundException e) {
            driverAvailable = false;
            logger.error(
                    "JDBC::updateConfig: could NOT load JDBC-driverClassName or JDBC-dataSourceClassName. ClassNotFoundException: '{}'",
                    e.getMessage());
            String warn = ""
                    + "\n\n\t!!!\n\tTo avoid this error, place an appropriate JDBC driver file for serviceName '{}' in addons directory.\n"
                    + "\tCopy missing JDBC-Driver-jar to your openHab/addons Folder.\n\t!!!\n" + "\tDOWNLOAD: \n";
            String serviceName = this.serviceName;
            if (serviceName != null) {
                switch (serviceName) {
                    case "derby":
                        warn += "\tDerby:     version >= 10.14.2.0 from          https://mvnrepository.com/artifact/org.apache.derby/derby\n";
                        break;
                    case "h2":
                        warn += "\tH2:        version >= 1.4.189 from            https://mvnrepository.com/artifact/com.h2database/h2\n";
                        break;
                    case "hsqldb":
                        warn += "\tHSQLDB:    version >= 2.3.3 from              https://mvnrepository.com/artifact/org.hsqldb/hsqldb\n";
                        break;
                    case "mariadb":
                        warn += "\tMariaDB:   version >= 3.0.8 from              https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client\n";
                        break;
                    case "mysql":
                        warn += "\tMySQL:     version >= 8.0.31 from             https://mvnrepository.com/artifact/com.mysql/mysql-connector-j\n";
                        break;
                    case "postgresql":
                        warn += "\tPostgreSQL:version >= 42.4.3 from             https://mvnrepository.com/artifact/org.postgresql/postgresql\n";
                        break;
                    case "sqlite":
                        warn += "\tSQLite:    version >= 3.40.0.0 from           https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc\n";
                        break;
                }
            }
            logger.warn(warn, serviceName);
        }
    }

    public Properties getHikariConfiguration() {
        return dBDAO.getConnectionProperties();
    }

    public String getName() {
        // return serviceName;
        return name;
    }

    public @Nullable String getServiceName() {
        return serviceName;
    }

    public String getItemsManageTable() {
        return itemsManageTable;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public int getErrReconnectThreshold() {
        return errReconnectThreshold;
    }

    public boolean getRebuildTableNames() {
        return rebuildTableNames;
    }

    public int getNumberDecimalcount() {
        return numberDecimalcount;
    }

    public boolean getTableUseRealItemNames() {
        return tableUseRealItemNames;
    }

    public boolean getTableCaseSensitiveItemNames() {
        return tableCaseSensitiveItemNames;
    }

    /**
     * Checks if real item names (without number suffix) is enabled.
     *
     * @return true if both tableUseRealItemNames and tableCaseSensitiveItemNames are enabled.
     */
    public boolean getTableUseRealCaseSensitiveItemNames() {
        return tableUseRealItemNames && tableCaseSensitiveItemNames;
    }

    public int getTableIdDigitCount() {
        return tableIdDigitCount;
    }

    public JdbcBaseDAO getDBDAO() {
        return dBDAO;
    }

    public @Nullable String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public boolean isDbConnected() {
        return dbConnected;
    }

    public void setDbConnected(boolean dbConnected) {
        logger.debug("JDBC::setDbConnected {}", dbConnected);
        // Initializing step, after db is connected.
        // Initialize sqlTypes, depending on DB version for example
        dBDAO.initAfterFirstDbConnection();
        // Running once again to prior external configured SqlTypes!
        setSqlTypes();
        this.dbConnected = dbConnected;
    }

    public boolean isDriverAvailable() {
        return driverAvailable;
    }
}

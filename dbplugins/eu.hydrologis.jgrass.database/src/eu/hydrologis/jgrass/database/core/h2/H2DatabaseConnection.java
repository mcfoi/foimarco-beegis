/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.database.core.h2;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.h2.tools.Server;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.IDatabaseConnection;
import eu.hydrologis.jgrass.database.earlystartup.AnnotatedClassesCollector;
import eu.hydrologis.jgrass.database.interfaces.Utils;

/**
 * A H2 database connection.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class H2DatabaseConnection implements IDatabaseConnection {
    private String user;
    private String passwd;
    private String databasePath;
    private String databaseName;
    private String port;
    private String connectionString;

    private SessionFactory sessionFactory;
    private Server tcpServer = null;
    private Server webServer = null;
    private boolean dbIsAlive = false;
    private AnnotationConfiguration annotationConfiguration;

    private List<String> annotatedClassesList = new ArrayList<String>();
    private boolean doLog;

    @Override
    public void setConnectionParameters( DatabaseConnectionProperties connectionProperties ) {
        user = connectionProperties.getUser();
        passwd = connectionProperties.getPassword();
        databasePath = connectionProperties.getPath();
        databaseName = connectionProperties.getDatabaseName();
        port = connectionProperties.getPort();
        doLog = connectionProperties.doLogSql();

        final String database = databasePath + File.separator + databaseName;
        connectionString = "jdbc:h2:tcp://localhost:" + port + "/" + database;
    }

    @Override
    public SessionFactory getSessionFactory() throws Exception {
        if (sessionFactory == null) {
            startWebserver();
            int timeout = 0;
            while( !dbIsAlive ) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (timeout++ > 50) {
                    throw new RuntimeException("An error occurred while starting the embedded database."); //$NON-NLS-1$
                }
            }
            sessionFactory = getAnnotationConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    @Override
    public Session openSession() {
        return sessionFactory.openSession();
    }

    @Override
    public void closeSessionFactory() throws Exception {
        if (sessionFactory == null) {
            return;
        }
        sessionFactory.close();
        if (tcpServer != null) {
            tcpServer.stop();
        }
        if (webServer != null) {
            webServer.stop();
        }
    }

    @Override
    public AnnotationConfiguration getAnnotationConfiguration() throws Exception {
        if (annotationConfiguration == null) {
            Properties dbProps = new Properties();
            dbProps.put(Environment.DRIVER, "org.h2.Driver");
            dbProps.put(Environment.URL, connectionString);
            dbProps.put(Environment.USER, user);
            dbProps.put(Environment.PASS, passwd);
            dbProps.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect"); //$NON-NLS-1$
            dbProps.put(Environment.SHOW_SQL, String.valueOf(doLog));
            dbProps.put(Environment.FORMAT_SQL, String.valueOf(doLog));

            annotationConfiguration = new AnnotationConfiguration();

            File configFile = Utils.generateConfigFile();
            annotationConfiguration = annotationConfiguration.configure(configFile);
            annotatedClassesList.addAll(AnnotatedClassesCollector.getAnnotatedClassesList());
            annotationConfiguration.setProperties(dbProps);

            for( String annotatedClassString : annotatedClassesList ) {
                annotationConfiguration.addAnnotatedClass(Class.forName(annotatedClassString));
            }
        }
        return annotationConfiguration;
    }

    @Override
    public DataStore getSpatialDataStore() {
        // DataStoreFactorySpi factory = null;
        // HashMap<String, Serializable> connectParameters = new HashMap<String, Serializable>();
        // connectParameters.put("host", host);
        // connectParameters.put("database", database);
        // connectParameters.put("schema", schema);
        // connectParameters.put("dbtype", "HATBOX-H2");
        // connectParameters.put("user", user);
        // factory = new HatBoxH2DataStoreFactory();

        return null;
    }

    @Override
    public boolean checkTables( String... tables ) throws Exception {
        StringBuilder sB = new StringBuilder();
        sB.append("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE ");
        for( int i = 0; i < tables.length; i++ ) {
            String tableName = tables[i];
            if (i == 0) {
                sB.append("UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')");
            } else {
                sB.append(" OR UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')");
            }
        }

        SessionFactory hibernateSessionFactory = getSessionFactory();
        Session session = hibernateSessionFactory.openSession();
        SQLQuery sqlQuery = session.createSQLQuery(sB.toString());
        Number foundNum = (Number) sqlQuery.list().get(0);

        session.close();

        if (tables.length == foundNum.intValue()) {
            return true;
        }
        return false;
    }

    @Override
    public void createSchemas( boolean doUpdate ) throws Exception {
        getSessionFactory();
        if (doUpdate) {
            SchemaUpdate schemaUpdate = new SchemaUpdate(getAnnotationConfiguration());
            schemaUpdate.execute(true, true);
        } else {
            SchemaExport schemaExport = new SchemaExport(getAnnotationConfiguration());
            schemaExport.create(true, true);
        }
    }

    /**
     * start the database instance
     */
    private void startWebserver() {
        Thread h2WebserverThread = new Thread(){

            public void run() {
                try {
                    if (!dbIsAlive) {
                        String[] args = {"-tcp", "-tcpPort", String.valueOf(port)};
                        tcpServer = Server.createTcpServer(args).start();
                        args = new String[]{"-web", "-webPort", String.valueOf(port + 1)};
                        webServer = Server.createWebServer(args).start();
                        dbIsAlive = true;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        h2WebserverThread.start();
    }

}

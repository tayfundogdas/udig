/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.internal.postgis.ui;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.service.database.DatabaseConnectionRunnable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.DataSourceException;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * A runnable that attempts to connect to a postgis database. If it does it will get a list of all
 * the databases and store them for later access. If it does not then it will store an error
 * message.
 * 
 * @author jesse
 * @since 1.1.0
 */
public class PostgisDatabaseConnectionRunnable implements DatabaseConnectionRunnable {

    private volatile boolean ran = false;
    private volatile String result = null;
    private final Set<String> databaseNames = new HashSet<String>();
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public PostgisDatabaseConnectionRunnable( String host2, int port2, String username2,
            String password2 ) {
        this.host = host2;
        this.port = port2;
        this.username = username2;
        this.password = password2;
    }

    public void run( IProgressMonitor monitor ) throws InvocationTargetException,
            InterruptedException {

        try {
            DataSource source = PostgisDataStoreFactory.getDefaultDataSource(host, username, password, port, "template1", 10, 4, true);
            Connection connection = source.getConnection();
            try {
                
                Statement statement = connection.createStatement();
                if (statement.execute("SELECT datname FROM pg_database")) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        databaseNames.add(resultSet.getString("datname"));
                    }
                }
                statement.close();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            checkSqlException(e);
        } catch (DataSourceException e) {
            if( e.getCause() instanceof SQLException){
                checkSqlException((SQLException) e.getCause());
            }else{
                PostgisPlugin.log("Error connecting to datasource", e);
                result = "Unrecognized connection failure.  Check parameters and database.";
            }
        }
        ran = true;
    }

    private void checkSqlException( SQLException e ) {
        if( e.getMessage().contains("FATAL: no pg_hba.conf entry for host") && e.getMessage().contains("template1") ){ //$NON-NLS-1$ //$NON-NLS-2$
            // this is understandable the template1 database is not accessible to this user/location so it is not an error
        }else if( e.getMessage().contains("FATAL: role") && e.getMessage().contains("does not exist") ){  //$NON-NLS-1$//$NON-NLS-2$
                // this is understandable the template1 database is not accessible to this user/location so it is not an error
            result = "Username or password is incorrect";
        }else {
            PostgisPlugin.log("Error connecting to database template1", e);
            result = "Unrecognized connection failure.  Check parameters and database.";
        }
    }

    public String canConnect() throws IllegalStateException {
        if (!ran) {
            throw new IllegalStateException(
                    "run must complete running before this method is called.");
        }
        return result;
    }

    public String[] getDatabaseNames() {
        return databaseNames.toArray(new String[0]);
    }

}
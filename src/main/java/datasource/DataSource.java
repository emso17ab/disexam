package datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import utils.Config;
import java.sql.Connection;
import java.sql.SQLException;

//This is our connection pool - inspired by https://www.baeldung.com/hikaricp
public class DataSource {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    //We configure our pool in this static initialization block
    static {
        config.setJdbcUrl("jdbc:mysql://"
                        + Config.getDatabaseHost()
                        + ":"
                        + Config.getDatabasePort()
                        + "/"
                        + Config.getDatabaseName()
                        + "?serverTimezone=CET");
        config.setUsername(Config.getDatabaseUsername());
        config.setPassword(Config.getDatabasePassword());
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "50");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "400");
        ds = new HikariDataSource(config);
    }

    private DataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

package connectors;

import org.flywaydb.core.Flyway;

public class FlywayConnector {

    public Flyway createFlywayByDatasource(DatabaseConnector connector) {
        return Flyway.configure().locations("db/migration").dataSource(connector.createDatasource()).load();
    }
}

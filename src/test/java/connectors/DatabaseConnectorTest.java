package connectors;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {

    @Test
    void createDatasourceTest() {
        DatabaseConnector connector = new DatabaseConnector();
        MysqlDataSource dataSource = connector.createDatasource();

        assertEquals("testuser", dataSource.getUser());
        assertEquals("jdbc:mysql://localhost:3306/testdb?useUnicode=true", dataSource.getUrl());
        assertEquals("testpassword", dataSource.getPassword());
    }
}
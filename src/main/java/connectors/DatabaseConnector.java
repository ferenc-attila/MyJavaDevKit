package connectors;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConnector {

    private static final Path initDbPath = Path.of("docker/db/initdb.sql");
    public static final String CANNOT_READ_INITDB_SQL = "Cannot read file: 'docker/db/initdb.sql'!";

    public MysqlDataSource createDatasource() {
        validateUserGetPrivileges();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(parseDataBaseUrl());
        dataSource.setUser(parseUser());
        dataSource.setPassword(parsePassword());
        return dataSource;
    }

    public void validateUserGetPrivileges() {
        try (BufferedReader br = Files.newBufferedReader(initDbPath)) {
            String userGetPrivileges = getUserNameWithPrivileges(br);
            if (userGetPrivileges.isBlank() || !userGetPrivileges.equals(parseUser())) {
                throw new IllegalArgumentException("Created user does not match user get privileges!");
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(CANNOT_READ_INITDB_SQL, ioe);
        }
    }

    private String getUserNameWithPrivileges(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("TO ")) {
                return line.substring(4).replace("'", "").split("@")[0];
            }
        }
        return "";
    }

    private String parseDataBaseUrl() {
        try (BufferedReader br = Files.newBufferedReader(initDbPath)) {
            String databaseName = getDatabaseName(br);
            validateParameter(databaseName, "name of database");
            return "jdbc:mysql://localhost:3306/" + databaseName + "?useUnicode=true";
        } catch (IOException ioe) {
            throw new IllegalStateException(CANNOT_READ_INITDB_SQL, ioe);
        }
    }

    private String getDatabaseName(BufferedReader br) throws IOException {
        String line;
        String databaseName = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("CREATE SCHEMA IF NOT EXISTS")) {
                databaseName = line.substring(29).replace("`", "");
            }
        }
        return databaseName;
    }

    private String parseUser() {
        try (BufferedReader br = Files.newBufferedReader(initDbPath)) {
            String userName = getUserName(br);
            validateParameter(userName, "username");
            return userName;
        } catch (IOException ioe) {
            throw new IllegalStateException(CANNOT_READ_INITDB_SQL, ioe);
        }
    }

    private String getUserName(BufferedReader br) throws IOException {
        String userName = "";
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("CREATE USER ")) {
                userName = line.substring(13).replace("'", "").split("@")[0];
            }
        }
        return userName;
    }

    private String parsePassword() {
        try (BufferedReader br = Files.newBufferedReader(initDbPath)) {
            String password = getPassword(br);
            validateParameter(password, "password");
            return password;
        } catch (IOException ioe) {
            throw new IllegalStateException(CANNOT_READ_INITDB_SQL, ioe);
        }
    }

    private String getPassword(BufferedReader br) throws IOException {
        String password = "";
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("IDENTIFIED BY ")) {
                password = line.substring(15).replace("'", "").replace(";", "");
            }
        }
        return password;
    }

    private void validateParameter(String parameter, String nameOfParameter) {
        if (parameter.isBlank()) {
            throw new IllegalArgumentException("Cannot find " + nameOfParameter + " in init.db file!");
        }
    }
}

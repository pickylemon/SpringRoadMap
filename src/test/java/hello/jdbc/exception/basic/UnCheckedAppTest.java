package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void unChecked(){
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                //.isInstanceOf(Exception.class);
                .isInstanceOf(RuntimeSQLException.class);
    }
    @Test
    void printEx(){
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            log.info("ex", e);
        }
    }
    static class Controller {
        Service service = new Service();
        public void request(){
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();
        public void logic() {
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient {
        public void call() { //checkedException
            try {
                connect();
            } catch (ConnectException e) {
                throw new RuntimeConnectException(e);
            }
        }
        public void connect() throws ConnectException {
            throw new ConnectException("연결 실패");
        }

    }
    static class Repository {
        public void call() { //checkedException
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }
        public void runSQL() throws SQLException {
            throw new SQLException();
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(Throwable cause) {
            super(cause);
        }
    }
}

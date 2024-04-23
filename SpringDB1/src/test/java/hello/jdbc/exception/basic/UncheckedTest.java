package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class UncheckedTest {

    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
     */

    @Test
    void unchecked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throws(){
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrows())
                .isInstanceOf(MyUncheckedException.class);
    }
    static class MyUncheckedException extends RuntimeException{
        MyUncheckedException(String msg){
            super(msg);
        }
    }

    /**
     * UnChecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */

    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch(){
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                //예외 처리 로직을 적는다.
                log.info("예외처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 명시적으로 선언하지 않아도 알아서 던져준다.
         */
        public void callThrows(){
            repository.call();
        }

    }

    static class Repository { //unchecked는 메서드에 throws 명시 안해줘도 됨
        public void call(){
            throw new MyUncheckedException("ex");
        }
    }
}

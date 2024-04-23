package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.util.ProxyUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {
    @Autowired
    CallService callService; // internal()에 @Transactional이 있어서 프록시가 주입됨

    @Test
    void printProxy(){
        log.info("callService class={}", callService.getClass());
        log.info("call Service is proxy?={}", AopUtils.isAopProxy(callService));
    }

    @Test
    void internalCall(){
        callService.internal();
    }

    @Test
    void callExternal(){
        callService.external();
        //internal호출의 Tx도 false로 나온다.
    }


    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService(){
            return new CallService();
        }
    }

    static class CallService {
        public void external(){
            log.info("call external");
            printTxInfo();
            internal();
            //this.internal()과 같다. 즉, 여기서 this는 target객체(프록시가 아닌 service 객체)가 됨
        }

        @Transactional
        public void internal(){
            log.info("call internal");
            printTxInfo();

        }


        private void printTxInfo(){
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }
}

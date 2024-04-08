package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class BasicTxTest {
    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        } //원래 스프링부트는 자동으로 TxManager를 등록해주지만, 여기서는 수동으로 DataSourceTxManager를 등록
    }

    @Test
    void commit(){
        log.info("\n트랜잭션 시작\n");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        //Attribute가 DefaultTransactionDefinition을 상속

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback(){
        log.info("\n트랜잭션 시작\n");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        //Attribute가 DefaultTransactionDefinition을 상속

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    @DisplayName("독립된 두 물리적 Tx - 각각의 Tx가 순차적으로 진행됨")
    void double_commit(){
        log.info("트랜잭션1 시작");
        TransactionStatus tx1Status = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(tx1Status);
        log.info("트랜잭션1 커밋 완료");

        //트랜잭션할 때는 본인이 시작했던 Tx에 대한 status를 가지고 진행해야 한다.
        log.info("트랜잭션2 시작");
        TransactionStatus tx2Status = txManager.getTransaction(new DefaultTransactionAttribute());

        txManager.commit(tx2Status);
        log.info("트랜잭션2 커밋 완료");
    }
    @Test
    void double_commit_rollback(){
        log.info("트랜잭션1 시작");
        TransactionStatus tx1Status = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(tx1Status);
        log.info("트랜잭션1 커밋 완료");

        //트랜잭션할 때는 본인이 시작했던 Tx에 대한 status를 가지고 진행해야 한다.
        log.info("트랜잭션2 시작");
        TransactionStatus tx2Status = txManager.getTransaction(new DefaultTransactionAttribute());

        txManager.rollback(tx2Status);
        log.info("트랜잭션2 롤백 완료");
    }

    @Test
    @DisplayName("외부커밋-내부커밋")// 외부 Tx 실행 중에(완료x) 내부 Tx 시작됨
    void inner_commit(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outerStatus.isNewTransaction()={}", outerStatus.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("innerStatus.isNewTransaction()={}", innerStatus.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(innerStatus);

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outerStatus);
    }
    @Test
    @DisplayName("외부롤백-내부커밋")
    void outer_rollback_inner_commit(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outerStatus.isNewTransaction()={}", outerStatus.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("innerStatus.isNewTransaction()={}", innerStatus.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(innerStatus);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outerStatus);
    }
    @Test
    @DisplayName("외부커밋-내부롤백")
    void outer_commit_inner_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outerStatus.isNewTransaction()={}", outerStatus.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("innerStatus.isNewTransaction()={}", innerStatus.isNewTransaction());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(innerStatus); //Global Tx에 rollback-only marking
        assertThat(outerStatus.isRollbackOnly()).isTrue();

        log.info("외부 트랜잭션 커밋");
        assertThatThrownBy(() -> txManager.commit(outerStatus))
             .isInstanceOf(UnexpectedRollbackException.class);
        //txManager.commit(outerStatus);
    }

    @Test
    @DisplayName("내부롤백-REQUIRES_NEW")
    void inner_rollback_REQUIRES_NEW(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outerStatus.isNewTransaction()={}", outerStatus.isNewTransaction());

        log.info("내부 트랜잭션 시작");
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        DefaultTransactionAttribute def = new DefaultTransactionAttribute();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus innerStatus = txManager.getTransaction(def);
        log.info("innerStatus.isNewTransaction()={}", innerStatus.isNewTransaction());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(innerStatus);
        assertThat(outerStatus.isRollbackOnly()).isFalse();
        //옵션이 REQUIRED가 아니라 REQUIRES_NEW이므로 별개의 connection.
        //외부tx에 rollbackOnly같은 marking 없음

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outerStatus);
    }

}

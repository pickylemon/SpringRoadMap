package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource; // 서비스 계층에 JDBC 구현기술을 알아야 하는게 문제
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction - Transaction Manager
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());


        try{

            //비즈니스 로직 (트랜잭션 관련 코드와 종류가 다른 코드라 따로 메서드로 분리)
            bizLogic(fromId, toId, money);
            transactionManager.commit(status);
        } catch(Exception e) {
            log.error("error", e);
            transactionManager.rollback(status);
            throw new IllegalStateException(e);
        } //release를 service에서 할 필요도 없음
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

//    private void release(Connection con) {
//        if(con != null){
//            try {
//                // JDBCUtils를 바로 안 쓴 이유
//                //autoCommit을 다시 초기화(true)로 바꿔줘고 풀에 돌려준다.
//                con.setAutoCommit(true);
//                con.close();
//            } catch (Exception e) {
//                log.info("error", e); //exception을 로그로 남길때는 {} 안써도 됨. 그냥 exception을 넣으면 됨
//            }
//        }
//    }

}

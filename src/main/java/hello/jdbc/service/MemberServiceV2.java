package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 적용 (파라미터 연동, 풀을 고려한 종료)
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try{
            //트랜잭션 시작 (autoCommit = flase)
            con.setAutoCommit(false);
            //비즈니스 로직 (트랜잭션 관련 코드와 종류가 다른 코드라 따로 메서드로 분리)
            bizLogic(con, fromId, toId, money);
            con.commit(); //성공시 수동 커밋! 중요
        } catch(Exception e) {
            log.error("error", e);
            con.rollback(); //실패시 롤백! 중요
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection con) {
        if(con != null){
            try {
                // JDBCUtils를 바로 안 쓴 이유
                //autoCommit을 다시 초기화(true)로 바꿔줘고 풀에 돌려준다.
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
                log.info("error", e); //exception을 로그로 남길때는 {} 안써도 됨. 그냥 exception을 넣으면 됨
            }
        }
    }

}

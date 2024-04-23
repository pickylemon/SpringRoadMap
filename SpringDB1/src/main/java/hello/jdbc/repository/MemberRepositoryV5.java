package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBCTemplate을 사용해서 반복 코드를 제거하기
 *
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{
    private final JdbcTemplate template;
    public MemberRepositoryV5(DataSource datasource) {
        this.template = new JdbcTemplate(datasource);

    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member (member_id, money) values (?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
        //객체 하나 조회는 queryForObject
    }

    private RowMapper<Member> memberRowMapper() {
        return ((rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        });
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId); //파라미터 순서 조심
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    //JdbcTemplate을 사용하면 connection 동기화 닫는 것 까지 전부 다 해줌.

//    private void close(Connection con, Statement stmt, ResultSet rs){
//        JdbcUtils.closeResultSet(rs);
//        JdbcUtils.closeStatement(stmt);
//        //JdbcUtils.closeConnection(con);
//        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
//        DataSourceUtils.releaseConnection(con, dataSource);
//    }
//
//    private Connection getConnection() throws SQLException {
//        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야한다.
//        //Spring이 제공하는 DataSourceUtils.
//        //내부적으로 Tx동기화 매니저를 통해 connection을 얻어온다.
//        Connection con = DataSourceUtils.getConnection(dataSource);
//        log.info("get connection={}, class={}", con, con.getClass());
//        return con;
//    }
}

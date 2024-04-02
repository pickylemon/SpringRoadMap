package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용 (low level)
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch(SQLException e){
            log.error("db error", e);
            throw e; //로그 남기고 던져버린다.
        } finally {
//            pstmt.close();
//            con.close(); //시작과 역순으로 리소스를 닫아준다.
            //외부 리소스를 사용하는 것이므로 connection을 닫아주어야함
            //리소스 누수가 발생해 커넥션 부족으로 장애가 발생할 수 있다.


            //문제1. pstmt.close(); 호출 도중 exception 발생?
            // -> con.close();는 실행이 되지 않는다.
            // 결국 finally 구문 내에서도 try-catch를 써야함.

            close(con, pstmt, null);
            //항상 호출될 것을 보장하기 위해서 finally에서 닫아준다.
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            //여기서는 pk를 콕찝어서 조회한거라 반환되는 row가 1 또는 0이다.
            //만약 여러개를 조회하면 while문을 돌린다.
            if(rs.next()){ //커서를 움직이기 위해 최초 한번은 호출해주어야 한다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { //조회되는 회원이 없을 경우(데이터가 없음)
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch(SQLException e) {
            log.error("error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch(SQLException e) {
            log.error("error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs){
        //각 자원을 각각 try-catch로 묶어서 처리.
        //각 자원을 닫다가 예외가 터져도 catch블럭에서 잡아서
        //다음 자원을 닫는 코드를 수행할 수 있다.
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
        if(stmt != null) {
           try {
               stmt.close();
           } catch (SQLException e) {
               log.info("error", e); //처리할 수 있는 방법이 없으므로 로그 정도만 남긴다.
           }
       }
       if(con != null){
           try {
               con.close();
           } catch (SQLException e) {
               log.info("error", e);
           }
       }


    }

    private Connection getConnection(){
        return DBConnectionUtil.getConnection();
    }
}

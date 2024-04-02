package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {
    MemberRepositoryV1 repository;
    @BeforeEach
    void beforeEach() throws InterruptedException {
        //1. 기본 DriverManager - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
//        repository = new MemberRepositoryV1(dataSource);
        // 매번 새로운 connection을 얻어온다 conn0 ~ conn5-> 성능 좋지 않음

        //2.hikari pooling
        HikariDataSource dataSource = new HikariDataSource();
        //설정때문에 DataSource 인터페이스타입이 아닌 히카리로 받음
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        repository = new MemberRepositoryV1(dataSource);
        Thread.sleep(2000);
        //6개 쿼리가 모두 conn0 하나로 나옴.
        //connectionPool을 사용할 경우, conn을 close()하면 다시 pool에 반환되는것
        //pool에서 꺼낸 con은 pool이 감싸고 있어서 close()호출하더라도 다시 pool에 반환이 됨.
        //proxy객체만 갈아끼워서 계속 같은 conn을 제공할 수 있다.
    }
    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV0", 10000);
        Member savedMember = repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(findMember).isEqualTo(member); //@Data로 Equals 오버라이딩 되었기 때문에
        assertThat(findMember).isNotSameAs(member); //그러나 주소값은 다르다.

        //update: money : 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() ->
            repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

    }

}
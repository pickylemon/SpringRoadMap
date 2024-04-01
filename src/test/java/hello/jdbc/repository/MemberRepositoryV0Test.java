package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {
    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV3", 10000);
        Member savedMember = repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(findMember).isEqualTo(member); //@Data로 Equals 오버라이딩 되었기 때문에
        assertThat(findMember).isNotSameAs(member); //그러나 주소값은 다르다.
    }

}
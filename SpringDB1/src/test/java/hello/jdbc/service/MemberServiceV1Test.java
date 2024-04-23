package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

/**
 * 기본 동작, 트랜잭션이 없어서 문제가 발생
 */
class MemberServiceV1Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach
    void before() throws SQLException {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }
    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        int money = 5000;
        Member fromMember = new Member(MEMBER_A, 10000);
        Member toMember = new Member(MEMBER_B, 10000);
        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        //when
        memberService.accountTransfer(fromMember.getMemberId(), toMember.getMemberId(), money);
        //then
//        assertThat(fromMember.getMoney()).isEqualTo(5000);
//        assertThat(toMember.getMoney()).isEqualTo(15000);
        //이렇게 하면 안됨!!!

        Member findFromMember = memberRepository.findById(fromMember.getMemberId());
        Member findToMember = memberRepository.findById(toMember.getMemberId());

        assertThat(findFromMember.getMoney()).isEqualTo(5000);
        assertThat(findToMember.getMoney()).isEqualTo(15000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        int money = 5000;
        Member fromMember = new Member(MEMBER_A, 10000);
        Member toMember = new Member(MEMBER_EX, 10000);
        memberRepository.save(fromMember);
        memberRepository.save(toMember);

        //when
        //then
        assertThatThrownBy(() -> memberService.accountTransfer(fromMember.getMemberId(), toMember.getMemberId(), money))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이체중 예외 발생");

        Member findFromMember = memberRepository.findById(fromMember.getMemberId());
        Member findToMember = memberRepository.findById(toMember.getMemberId());

        assertThat(findFromMember.getMoney()).isEqualTo(5000);
        assertThat(findToMember.getMoney()).isEqualTo(10000);
        assertThat(findToMember.getMoney()).isNotEqualTo(15000);
    }
}
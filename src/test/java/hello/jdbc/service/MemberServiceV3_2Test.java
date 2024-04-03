package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Transaction - Transaction Template
 */
@Slf4j
class MemberServiceV3_2Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";
    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_2 memberService;
    @BeforeEach
    void before(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_2(transactionManager, memberRepository);
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
        log.info("START TX");
        memberService.accountTransfer(fromMember.getMemberId(), toMember.getMemberId(), money);
        log.info("END TX");
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
                .isInstanceOf(IllegalStateException.class);

        Member findFromMember = memberRepository.findById(fromMember.getMemberId());
        Member findToMember = memberRepository.findById(toMember.getMemberId());

        assertThat(findFromMember.getMoney()).isNotEqualTo(5000);
        assertThat(findFromMember.getMoney()).isEqualTo(10000);
        assertThat(findToMember.getMoney()).isNotEqualTo(15000);
        assertThat(findToMember.getMoney()).isEqualTo(10000);

    }

}
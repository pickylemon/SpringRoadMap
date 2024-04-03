package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Transaction - @Transactional / DataSource, TxManager enrolled by SpringBoot
 */
@Slf4j
@SpringBootTest //스프링을 띄우고 필요한 bean을 등록하고 DI도 이루어짐
class MemberServiceV4Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration
    static class TestConfig {
        //스프링 부트가 기본으로 dataSource와 transactionManager를 bean등록한다.
        //직접 bean등록하면 스프링 부트는 기본 등록 bean을 등록하지 않음.

        //스프링 컨테이너에 자동 등록되어 있는 dataSource를 주입받는다.
        private final DataSource dataSource;

        @Autowired
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean

        MemberRepository memberRepository(){
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberService(){
            return new MemberServiceV4(memberRepository());
        }
    }
//    @BeforeEach
//    void before(){
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
//        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
//
//        memberRepository = new MemberRepositoryV3(dataSource);
//        memberService = new MemberServiceV3_3(memberRepository);
//    }

    @Test
    void AopCheck(){
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }
    @AfterEach
    void after() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer(){
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
    void accountTransferEx() {
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


        //@Transactional을 사용하는 건 스프링 AOP기능을 사용하는 것.
        // 스프링 컨테이너에 bean을 등록해야 AOP 적용이 가능하다.
        // 스프링 안쓰고 단위테스트 진행하면 롤백이 제대로 수행되지 않는다.
        assertThat(findFromMember.getMoney()).isNotEqualTo(5000);
        assertThat(findFromMember.getMoney()).isEqualTo(10000);
        assertThat(findToMember.getMoney()).isNotEqualTo(15000);
        assertThat(findToMember.getMoney()).isEqualTo(10000);

    }

}
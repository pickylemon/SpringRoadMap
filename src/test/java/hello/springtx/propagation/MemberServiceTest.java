package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
//이 테스트 클래스에는 @Transactional 붙이지 않는다.(원하는 Tx 결과를 확인하기 위해)
class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService    @Transactional : OFF
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON
     */
    @Test
    void outerTxOff_success(){
        //given
        String username = "outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        memberService.joinV1(username);
        //then : 모든 데이터가 정상 저장된다.
        Assertions.assertTrue(memberRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
        Assertions.assertTrue(logRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
    }

    /**
     * memberService    @Transactional : OFF
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON, RuntimeExcption발생
     */
    @Test
    void outerTxOff_fail(){
        //given
        String username = "로그예외_outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        //then : log 데이터가 정상 저장된다.
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예외 발생");

        Assertions.assertTrue(memberRepository.find(username).isPresent()); //회원은 저장되고
        Assertions.assertTrue(logRepository.find(username).isEmpty()); //로그는 저장되지 않는다.
    }

    /**
     * memberService    @Transactional : ON
     * memberRepository @Transactional : OFF
     * logRepository    @Transactional : OFF
     */
    @Test
    void singleTx(){
        //given
        String username = "outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        memberService.joinV1(username);
        //then : 모든 데이터가 정상 저장된다.
        Assertions.assertTrue(memberRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
        Assertions.assertTrue(logRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
    }


    /**
     * memberService    @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON
     */
    @Test
    void outerTxOn_success(){
        //given
        String username = "outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        memberService.joinV1(username);
        //then : 모든 데이터가 정상 저장된다.
        Assertions.assertTrue(memberRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
        Assertions.assertTrue(logRepository.find(username).isPresent()); //assertj가 아닌 junit의 assertions
    }

    /**
     * memberService    @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON, RuntimeExcption발생
     */
    @Test
    void outerTxOn_fail(){
        //given
        String username = "로그예외_outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        //then : 모든 데이터 롤백된다.
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예외 발생");

        Assertions.assertTrue(memberRepository.find(username).isEmpty()); //회원은 저장되고
        Assertions.assertTrue(logRepository.find(username).isEmpty()); //로그는 저장되지 않는다.
    }


    /**
     * memberService    @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON, RuntimeExcption발생
     */
    @Test
    void recoverException_fail(){
        //given
        String username = "로그예외_outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        //then : 모든 데이터 롤백된다.
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty()); //회원과 로그 모두 저장되지 않는다.
    }

    /**
     * memberService    @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository    @Transactional : ON(REQUIRES_NEW), RuntimeExcption발생
     */
    @Test
    void recoverException_success(){
        //given
        String username = "로그예외_outerTxOff_success";
        Member member = new Member(username);
        Log logMessage = new Log(username);
        //when
        //then : 로그 데이터만 롤백된다.

        memberService.joinV2(username);

        Assertions.assertTrue(memberRepository.find(username).isPresent()); //회원은 저장되고
        Assertions.assertTrue(logRepository.find(username).isEmpty()); //로그는 저장되지 않는다.
    }


}
package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("정상 결제")
    void complete() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("정상");
        //when
        orderService.order(order);
        //then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    @DisplayName("시스템 에러 발생")
    void runtimeException() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("예외");
        //when
        //then
        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class); //지정한 예외 발생

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional.isEmpty()).isTrue(); //롤백에 의해 DB반영 안 됨을 체크. JPA는 롤백하면 insert쿼리 자체를 안 날림.

        //나
        assertThatThrownBy(()->orderRepository.findById(order.getId()).orElseThrow())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("잔고 부족 예외")
    void bizException(){
        //given
        Order order = new Order();
        order.setUsername("잔고부족");
        //when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
        }
        //then
        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(NotEnoughMoneyException.class); //지정한 예외 발생

        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기"); //commit되었다.

    }
}
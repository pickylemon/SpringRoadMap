package hello.springtx.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders") //sql문법상 order는 예약어라 테이블명으로 못써서..
@Data
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    private String username; // 사용자이름(정상, 예외, 잔고부족)
    private String payStatus;  //Enum으로 만드는게 더 좋다.

}

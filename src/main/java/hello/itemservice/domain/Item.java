package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity //테이블이랑 맵핑되어 관리되는 객체이다.
//@Table(name = "item") //객체명과 테이블명이 같으면 생략 가능
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    //pk이자 ai임을 명시. Identity 전략(db에서 증가한 pk값을 가져올때)을 사용
    private Long id;

    @Column(name = "item_name", length = 30)
    private String itemName;
    private Integer price; //컬럼과 필드명이 같으면 @Column 생략 가능
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}

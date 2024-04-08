package hello.springtx.propagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    private String username;

    public Member() {
    } //*JPA 스펙상 기본 생성자 필요*

    public Member(String username) {
        this.username = username;
    }
}

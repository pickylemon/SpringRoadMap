package hello.springtx.propagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Entity
@Getter @Setter
public class Log { //주의. DB에 남기는 로그!
    @Id
    @GeneratedValue
    private Long id;
    private String message;

    public Log() {
    } //JPA spec이 기본생성자를 필요로함

    public Log(String message) {
        this.message = message;
    }
}

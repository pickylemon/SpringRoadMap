package hello.jdbc.connection;

public abstract class ConnectionConst {
    //상수들을 모아놓은 클래스라서 객체를 생성할 이유가 없음
    //외부에서 객체를 생성할 수 없도록 abstract class로 선언
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}

package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.mybatis.ItemMapper;
import hello.itemservice.repository.mybatis.MyBatisItemRepository;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {
    private final ItemMapper itemMapper;

    @Bean
    public ItemService itemService(){
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository(){
        return new MyBatisItemRepository(itemMapper);
        //myBatis 모듈이 dataSource와 TxManager를 읽어서 mapper와 다 연결시켜줌
    }
}

package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ItemRepositoryTest {
    //인터페이스를 테스트해야 구현체를 바꿔도 같은 테스트로 검증 가능.

    @Autowired
    ItemRepository itemRepository;

    @AfterEach
    void afterEach(){
        //인터페이스에는 clearStore() 메서드가 없어서
        if(itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository)itemRepository).clearStore();
        }
    }

    @Test
    void save(){
        //given
        Item item = new Item("itemA", 10000, 10);
        //when
        Item savedItem = itemRepository.save(item);
        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem(){
        //given
        Item item = new Item("itemA", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        ItemUpdateDto updateParam = new ItemUpdateDto("itemB", 20000, 20);
        //when
        itemRepository.update(itemId,updateParam);
        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems(){
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        //when, then

        //디폴트 (검색조건 없을때 모든 아이템이 검색됨)
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검색 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검색 검증
        test(null, 10000, item1);

        //두 조건 모두 검색
        test("itemA", 10000, item1);

    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).containsExactly(items);
        //containsExactly는 가변인자 순서까지 반영한다(순서가 틀리면 false)
    }
}

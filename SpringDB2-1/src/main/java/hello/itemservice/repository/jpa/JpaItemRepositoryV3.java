package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.*;

@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;
    private final JPAQueryFactory query; //Querydsl의 클래스. JPA의 jpql을 만드는 builder역할

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setQuantity(updateParam.getQuantity());
        findItem.setPrice(updateParam.getPrice());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override //findAllOld()를 아래와 같이 리팩토링 가능
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        //QItem item = new QItem("i"); //i가 alias
        //QItem item = QItem.item;
        //BooleanBuilder와 Querydsl을 통해서 동적쿼리를 자바코드처럼 완성할 수 있다.

        List<Item> result = query.select(item) //static import
                .from(item)
                .where(likeItemName(itemName), maxPrice(maxPrice))
                .fetch();

        return result;
    }

    private BooleanExpression likeItemName(String itemName) {
        if(StringUtils.hasText(itemName)){
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }
    private Predicate maxPrice(Integer maxPrice) {
        if(maxPrice!=null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }

    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        //QItem item = new QItem("i"); //i가 alias
        QItem item = QItem.item;

        //BooleanBuilder와 Querydsl을 통해서 동적쿼리를 자바코드처럼 완성할 수 있다.
        BooleanBuilder builder = new BooleanBuilder();
        if(StringUtils.hasText(itemName)){
            builder.and(item.itemName.like("%" + itemName + "%"));
        }
        if(maxPrice != null){
            builder.and(item.price.loe(maxPrice));
        }

        List<Item> result = query.select(item) //static import
                .from(item)
                .where(builder)
                .fetch();

        return result;
    }
}

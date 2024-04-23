package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate으로 구현
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values (?,?,?)";
        //item을 insert하기 위해서는 item_id가 필요한데, JdbcTemplate을 쓸 때는 keyHolder를 이용한다.
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            //자동 증가 키
            /**
             *
             * Creates a default PreparedStatement object capable of
             * returning the auto-generated keys designated by the given array.
             * This array contains the names of the columns in the target table
             * that contain the auto-generated keys that should be returned
             * connection.preparedStatement(sql, String columnNames[])
             *
             */

            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3,item.getQuantity());
            return ps;

        }, keyHolder); //DB에 item insert

        long key = keyHolder.getKey().longValue();
        item.setId(key); //그 다음 자바 객체에 id를 set
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "Update item set item_name=?, price=?, quantity=? where id=?";
        template.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select * from item where id = ?";
        Item item = null;
        try {
            //queryForObject는 값이 없으면 exception 터진다.
            item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    //rowMapper는 functional Interface다.
    // T mapRow(ResultSet rs, int rowNum)

    private RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select * from item";

        // queryForObject는 하나만 가져올 때
        // query는 List로 가져올때

        //jdbcTemplate으로 동적쿼리를 작성하는 것은 매우 어렵다.
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice); }
        log.info("sql={}", sql);
        return template.query(sql, itemRowMapper(), param.toArray());
    }
}

package com.epam.izh.rd.online.autcion.mappers;

import com.epam.izh.rd.online.autcion.entity.Bid;
import com.epam.izh.rd.online.autcion.entity.Item;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ItemBidMapper implements ResultSetExtractor<Map<Item, Bid>> {
    @Override
    public Map<Item, Bid> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        Map<Item, Bid> maxBidsForEveryItem = new HashMap<>();
        while (resultSet.next()) {
            Item item = new Item(resultSet.getLong("item_id"),
                    resultSet.getDouble("bid_increment"),
                    resultSet.getBoolean("buy_it_now"),
                    resultSet.getString("description"),
                    resultSet.getDate("start_date").toLocalDate(),
                    resultSet.getDouble("start_price"),
                    resultSet.getDate("stop_date").toLocalDate(),
                    resultSet.getString("title"),
                    resultSet.getLong("users_user_id"));
            Bid bid = new Bid(resultSet.getLong("bid_id"),
                    resultSet.getDate("bid_date").toLocalDate(),
                    resultSet.getDouble("bid_value"),
                    resultSet.getLong("item_id"),
                    resultSet.getLong("user_id"));
            maxBidsForEveryItem.put(item, bid);
        }
        return maxBidsForEveryItem;
    }
}

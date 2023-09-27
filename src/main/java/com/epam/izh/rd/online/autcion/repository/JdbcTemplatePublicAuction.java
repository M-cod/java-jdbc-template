package com.epam.izh.rd.online.autcion.repository;

import com.epam.izh.rd.online.autcion.entity.Bid;
import com.epam.izh.rd.online.autcion.entity.Item;
import com.epam.izh.rd.online.autcion.entity.User;
import com.epam.izh.rd.online.autcion.mappers.BidMapper;
import com.epam.izh.rd.online.autcion.mappers.ItemBidMapper;
import com.epam.izh.rd.online.autcion.mappers.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Component
public class JdbcTemplatePublicAuction implements PublicAuction {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ItemBidMapper itemBidMapper;
    @Autowired
    ItemMapper itemMapper;
    @Autowired
    BidMapper bidMapper;


    @Override
    public List<Bid> getUserBids(long id) {
        String query = "SELECT * FROM bids, items WHERE bids.user_id = ?";
        List<Bid> bids = jdbcTemplate.query(query, bidMapper, id);
        return bids;
    }

    @Override
    public List<Item> getUserItems(long id) {
        return jdbcTemplate.query(
                "SELECT * FROM items WHERE items.user_id = ?",
                itemMapper, id);
    }

    @Override
    public Item getItemByName(String name) {
        List<Item> items = jdbcTemplate.query(
                "SELECT * FROM items WHERE  title LIKE ?",
                itemMapper, "%" + name + "%");
        return items.get(0);
    }

    @Override
    public Item getItemByDescription(String name) {
        return jdbcTemplate.query(
                "SELECT * FROM items WHERE  items.description LIKE ?",
                itemMapper, "%" + name + "%").get(0);
    }

    @Override
    public Map<User, Double> getAvgItemCost() {

        String query = "SELECT users.*, AVG(items.start_price) FROM items " +
                "JOIN users ON users.user_id = items.user_id " +
                "GROUP BY items.user_id";


        return jdbcTemplate.query(query, new ResultSetExtractor<Map<User, Double>>() {
            @Override
            public Map<User, Double> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                Map<User, Double> avgItemCost = new HashMap<>();
                while (resultSet.next()) {
                    User user = new User(resultSet.getLong("user_id"),
                            resultSet.getString("billing_address"),
                            resultSet.getString("full_name"),
                            resultSet.getString("login"),
                            resultSet.getString("password"));
                    Double avg = resultSet.getDouble("AVG(items.start_price)");
                    avgItemCost.put(user, avg);
                }
                return avgItemCost;
            }
        });
    }

    @Override
    public Map<Item, Bid> getMaxBidsForEveryItem() {

        String query = "SELECT DISTINCT b.*, items.*, items.user_id as users_user_id\n" +
                "FROM bids b\n" +
                "JOIN (SELECT MAX(bids.bid_value) as bid_value, bids.item_id FROM bids GROUP BY bids.item_id) bb\n" +
                "ON b.bid_value = bb.bid_value\n" +
                "JOIN items ON items.item_id = b.item_id";

        return jdbcTemplate.query(query, itemBidMapper);
    }

    @Override
    public boolean createUser(User user) {
        return jdbcTemplate.update(
                "INSERT INTO users (user_id, full_name, billing_address, password) VALUES (?, ?, ?, ?)",
                user.getUserId(), user.getFullName(), user.getBillingAddress(), user.getPassword()) != 0;
    }

    @Override
    public boolean createItem(Item item) {
        return jdbcTemplate.update(
                "INSERT INTO items (item_id, title, description, start_price, bid_increment, start_date, stop_date, buy_it_now, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                item.getItemId(), item.getTitle(), item.getDescription(), item.getStartPrice(), item.getBidIncrement(), item.getStartDate(), item.getStopDate(), item.getBuyItNow(), item.getUserId()) != 0;
    }

    @Override
    public boolean createBid(Bid bid) {
        return jdbcTemplate.update(
                "INSERT INTO bids (bid_id, bid_date, bid_value, item_id, user_id) VALUES (?, ?, ?, ?, ?)",
                bid.getBidId(), bid.getBidDate(), bid.getBidValue(), bid.getItemId(), bid.getUserId()) != 0;
    }

    @Override
    public boolean deleteUserBids(long id) {
        return jdbcTemplate.update("DELETE FROM bids WHERE bids.user_id = ?", id) != 0;
    }

    @Override
    public boolean doubleItemsStartPrice(long id) {
        return jdbcTemplate.update("UPDATE items SET start_price = start_price * 2 WHERE items.user_id = ?", id) != 0;
    }
}

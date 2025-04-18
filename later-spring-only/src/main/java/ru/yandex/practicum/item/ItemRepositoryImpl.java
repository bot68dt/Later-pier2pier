package ru.yandex.practicum.item;


import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private HashMap<Long, ArrayList<Item>> map = new HashMap<>();

    @Override
    public List<Item> findByUserId(long userId) {
        return map.get(userId);
    }

    @Override
    public Item save(long userId, Item item) {
        ArrayList<Item> list = map.get(userId);
        if (list==null)
            list = new ArrayList<>();
        item.setId(Long.valueOf(list.size()+1));
        list.add(item);
        map.replace(userId, list);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        ArrayList<Item> list = map.get(userId);
        int id = 0;
        for (Item i : list) {
            if (i.getId() == itemId)
                return;
            id++;
        }
        list.remove(id);
        map.replace(userId, list);
    }
}
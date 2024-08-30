package com.convallyria.taleofkingdoms.common.shop;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShopParser {
    public enum GUI {
        BLACKSMITH,
        FOOD,
        SELL,
        ITEM,
        STOCK_MARKET,
        BLOCK
    }

    private JsonObject shopJson;
    public static Map<GUI, List<ShopItem>> SHOP_ITEMS = new EnumMap<>(GUI.class);

    public void createShopItems() {
        if(!loadShopJson()) {
            return;
        }

        for (Map.Entry<String, JsonElement> jsonElement : shopJson.entrySet()) {
            JsonArray shopItems = jsonElement.getValue().getAsJsonArray();

            for(JsonElement shopItemElement : shopItems) {
                JsonObject shopItem = shopItemElement.getAsJsonObject();

                try {
                    Item item = getItem(shopItem.get("item").getAsString().toLowerCase(Locale.ROOT));

                    int cost = 0, sell = -1;

                    if (shopItem.has("cost")) {
                        cost = shopItem.get("cost").getAsInt();
                    }

                    if (shopItem.has("sell")) {
                        sell = shopItem.get("sell").getAsInt();
                    }

                    addToShopItems(jsonElement.getKey(), new ShopItem(item, cost, sell));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Copies (if needed) and loads the json file in "config/taleofkingdoms/shop.json"
    private boolean loadShopJson() {
        File internalFile = new File("shop.json");
        File configDirectory = new File("config/" + TaleOfKingdoms.MODID);
        File externalFile = new File(configDirectory, internalFile.getName());

        if(configDirectory.mkdir() || configDirectory.exists()) {
            InputStream fileSrc = Thread.currentThread().getContextClassLoader().getResourceAsStream(internalFile.getPath());

            try {
                if (externalFile.exists() || externalFile.createNewFile()) {
                    Files.copy(fileSrc, externalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                Reader reader = Files.newBufferedReader(externalFile.toPath());

                shopJson = new Gson().fromJson(reader, JsonObject.class);

                return true;
            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private Item getItem(String name) throws ReflectiveOperationException {
        return Registries.ITEM.get(new Identifier(name.toLowerCase(TaleOfKingdoms.DEFAULT_LOCALE)));
    }

    // Make recursive function to look for {} and split by ","

    /**
     * Adds a {@link ShopItem} to the list in the map
     * @param key the key for the entry in the map
     * @param shopItem the ShopItem to be added to the list of ShopItems in the map for that key
     */
    private void addToShopItems(String key, ShopItem shopItem) {
        // gui key from json
        String upperCaseKey = key.toUpperCase(TaleOfKingdoms.DEFAULT_LOCALE);
        if (SHOP_ITEMS.containsKey(GUI.valueOf(upperCaseKey))) {
            SHOP_ITEMS.get(GUI.valueOf(upperCaseKey)).add(shopItem);
        } else {
            SHOP_ITEMS.put(GUI.valueOf(upperCaseKey), new ArrayList<>());
            addToShopItems(key, shopItem);
        }
    }
}

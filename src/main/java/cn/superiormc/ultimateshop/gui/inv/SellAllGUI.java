package cn.superiormc.ultimateshop.gui.inv;

import cn.superiormc.ultimateshop.gui.InvGUI;
import cn.superiormc.ultimateshop.managers.ConfigManager;
import cn.superiormc.ultimateshop.managers.LanguageManager;
import cn.superiormc.ultimateshop.methods.Product.SellProductMethod;
import cn.superiormc.ultimateshop.methods.ProductTradeStatus;
import cn.superiormc.ultimateshop.objects.buttons.ObjectItem;
import cn.superiormc.ultimateshop.objects.items.AbstractSingleThing;
import cn.superiormc.ultimateshop.objects.items.ThingMode;
import cn.superiormc.ultimateshop.objects.items.prices.ObjectPrices;
import cn.superiormc.ultimateshop.utils.InvUtil;
import cn.superiormc.ultimateshop.libs.xserieschanged.XItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SellAllGUI extends InvGUI {

    public SellAllGUI(Player owner) {
        super(owner);
    }

    @Override
    protected void constructGUI() {
        if (Objects.isNull(inv)) {
            inv = InvUtil.createNewInv(owner, ConfigManager.configManager.getInt
                            ("menu.sell-all.size", 54),
                   ConfigManager.configManager.getString("menu.sell-all.title"));
        }
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        return ConfigManager.configManager.getIntList("menu.sell-all.black-slots").contains(slot);
    }

    @Override
    public boolean closeEventHandle(Inventory inventory) {
        if (owner == null) {
            return true;
        }
        int nowAmount = 0;
        int afterAmount = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item != null) {
                nowAmount = nowAmount + item.getAmount();
            }
        }
        Map<AbstractSingleThing, BigDecimal> result = new HashMap<>();
        for (String shop : ConfigManager.configManager.shopConfigs.keySet()) {
            for (ObjectItem products : ConfigManager.configManager.getShop(shop).getProductList()) {
                ProductTradeStatus status = SellProductMethod.startSell(inv,
                        shop,
                        products.getProduct(),
                        owner.getPlayer(),
                        false,
                        false,
                        ConfigManager.configManager.getBoolean("menu.sell-all.hide-message"),
                        true,
                        1);
                if (status.getStatus() == ProductTradeStatus.Status.DONE && status.getGiveResult() != null) {
                    result.putAll(status.getGiveResult().getResultMap());
                }
            }
        }
        if (!result.isEmpty()) {
            for (ItemStack item : inv.getStorageContents()) {
                if (item != null) {
                    afterAmount = afterAmount + item.getAmount();
                }
            }
            LanguageManager.languageManager.sendStringText(owner.getPlayer(), "start-sell-all", "amount", String.valueOf(nowAmount - afterAmount),
                    "reward", ObjectPrices.getDisplayNameInLine(owner,
                    result, ThingMode.ALL
            ));
        }
        ItemStack[] storage = Arrays.stream(inv.getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
        XItemStack.giveOrDrop(owner, storage);
        return true;
    }

    @Override
    public boolean dragEventHandle(Map<Integer, ItemStack> newItems) {
        owner.updateInventory();
        for (int i : newItems.keySet()) {
            if (ConfigManager.configManager.getIntList("menu.sell-all.black-slots").contains(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getChangeable() {
        return true;
    }

}

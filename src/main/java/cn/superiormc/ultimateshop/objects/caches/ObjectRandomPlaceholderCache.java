package cn.superiormc.ultimateshop.objects.caches;

import cn.superiormc.ultimateshop.cache.ServerCache;
import cn.superiormc.ultimateshop.managers.BungeeCordManager;
import cn.superiormc.ultimateshop.managers.ConfigManager;
import cn.superiormc.ultimateshop.objects.buttons.ObjectItem;
import cn.superiormc.ultimateshop.objects.items.shbobjects.ObjectRandomPlaceholder;
import cn.superiormc.ultimateshop.utils.CommonUtil;
import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObjectRandomPlaceholderCache {

    private String nowValue = null;

    private LocalDateTime refreshDoneTime = null;

    private final ObjectRandomPlaceholder placeholder;

    public ObjectRandomPlaceholderCache(ObjectRandomPlaceholder placeholder) {
        this.placeholder = placeholder;
        setNewValue();
    }

    public ObjectRandomPlaceholderCache(ObjectRandomPlaceholder placeholder,
                                        String nowValue,
                                        LocalDateTime refreshDoneTime) {
        this.placeholder = placeholder;
        this.nowValue = nowValue;
        this.refreshDoneTime = refreshDoneTime;
    }

    public ObjectRandomPlaceholder getPlaceholder() {
        return placeholder;
    }

    public LocalDateTime getRefreshDoneTime() {
        return refreshDoneTime;
    }

    public String getNowValue() {
        setNewValue();
        return nowValue;
    }

    public void setNewValue() {
        setNewValue(false);
    }

    public void setNewValue(boolean notUseBungee) {
        String mode = placeholder.getMode();
        String time = placeholder.getConfig().getString("reset-time");
        if (mode == null || time == null) {
            if (nowValue == null) {
                setPlaceholder(notUseBungee);
            }
            return;
        }
        if (mode.equals("ONCE")) {
            setPlaceholder(notUseBungee);
            return;
        }
        if (nowValue == null || refreshDoneTime == null || !refreshDoneTime.isAfter(LocalDateTime.now())) {
            if (mode.equals("TIMED")) {
                refreshDoneTime = getTimedRefreshTime(time);
                setPlaceholder(notUseBungee);
            }
            else if (mode.equals("TIMER")) {
                refreshDoneTime = getTimerRefreshTime(time);
                setPlaceholder(notUseBungee);
            }
        }
    }

    private void setPlaceholder(boolean notUseBungee) {
        nowValue = placeholder.getNewValue();
        if (placeholder.getMode().equals("TIMED") || placeholder.getMode().equals("TIMER")) {
            ServerCache.serverCache.setRandomPlaceholderCache(placeholder.getID(), CommonUtil.timeToString(refreshDoneTime), nowValue);
        }
        if (!notUseBungee && BungeeCordManager.bungeeCordManager != null) {
            BungeeCordManager.bungeeCordManager.sendToOtherServer(
                    placeholder.getID(),
                    nowValue,
                    CommonUtil.timeToString(refreshDoneTime));
        }
    }

    private LocalDateTime getTimedRefreshTime(String time) {
        String tempVal1 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        tempVal1 = tempVal1 + " " + time;
        LocalDateTime refreshResult = CommonUtil.stringToTime(tempVal1);
        if (LocalDateTime.now().isAfter(refreshResult)) {
            refreshResult = refreshResult.plusDays(1L);
        }
        return refreshResult;
    }


    private LocalDateTime getTimerRefreshTime(String time) {
        LocalDateTime refreshResult = LocalDateTime.now();
        refreshResult = refreshResult.plusHours(Long.parseLong(time.split(":")[0]));
        refreshResult = refreshResult.plusMinutes(Long.parseLong(time.split(":")[1]));
        refreshResult = refreshResult.plusSeconds(Long.parseLong(time.split(":")[2]));
        return refreshResult;
    }

}

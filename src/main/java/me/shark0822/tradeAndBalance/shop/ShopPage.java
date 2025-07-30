package me.shark0822.tradeAndBalance.shop;

import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShopPage {
    private final LinkedHashMap<Integer, ShopItem> items; // 슬롯 번호를 키로, ShopItem을 값으로 저장
    private int index; // 페이지 인덱스

    public ShopPage(int index) {
        this.items = new LinkedHashMap<>(); // HashMap 초기화: 키-값 쌍으로 슬롯 관리
        this.index = index;
    }

    // 아이템 추가
    public void addItem(int slot, ShopItem item) {
        items.put(slot, item); // HashMap에 슬롯 번호로 아이템 저장
        Bukkit.getLogger().info("[ShopPage] 아이템 추가 - 페이지: " + index + ", 슬롯: " + slot + ", 아이템: " + item.getOriginalItem().getType());
    }

    // 아이템 제거
    public ShopItem removeItem(int slot) {
        ShopItem item = items.get(slot);
        if (item == null) {
            Bukkit.getLogger().warning("[ShopPage] 제거 시도 - 슬롯: " + slot + ", 페이지: " + index + ", 아이템 없음");
        }
        items.remove(slot);
        Bukkit.getLogger().info("[ShopPage] 아이템 제거 - 페이지: " + index + ", 슬롯: " + slot + ", 아이템: " + (item != null ? item.getOriginalItem().getType() : "null"));
        return item;
    }

    // 슬롯의 아이템 반환
    public ShopItem getItem(int slot) {
        return items.get(slot); // HashMap에서 슬롯 번호로 아이템 조회
    }

    // 페이지의 모든 아이템 반환
    public LinkedHashMap<Integer, ShopItem> getItems() {
        return new LinkedHashMap<>(items); // HashMap 복사본 반환
    }

    @Override
    public ShopPage clone() {
        ShopPage cloned = new ShopPage(this.index);
        for (Map.Entry<Integer, ShopItem> entry : items.entrySet()) {
            ShopItem originalItem = entry.getValue();
            // ShopItem 복사
            ShopItem clonedItem = new ShopItem(
                    originalItem.getOriginalItem().clone(), // ItemStack 깊은 복사
                    originalItem.getBuyPrice(),
                    originalItem.getSellPrice(),
                    originalItem.getLimitType(),
                    originalItem.getLimitAmount()
            );
            clonedItem.setRemainingLimitAmount(originalItem.getRemainingLimitAmount());
            cloned.items.put(entry.getKey(), clonedItem);
        }
        return cloned;
    }

    // 페이지가 비어 있는지 확인
    public boolean isEmpty() {
        return items.isEmpty(); // HashMap이 비어 있으면 true
    }

    // 인덱스 반환
    public int getIndex() {
        return index;
    }

    // 인덱스 설정
    public void setIndex(int index) {
        this.index = index;
        Bukkit.getLogger().info("[ShopPage] 인덱스 설정 - 페이지: " + index);
    }

    // 페이지 비우기
    public void clear() {
        items.clear(); // HashMap의 모든 요소 제거
        Bukkit.getLogger().info("[ShopPage] 페이지 비우기 - 페이지: " + index);
    }
}
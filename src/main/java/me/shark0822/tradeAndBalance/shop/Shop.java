package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.structure.DoublyCircularLinkedList;
import org.bukkit.Bukkit;

import java.util.*;

public class Shop {
    private final String shopID; // 상점 고유 ID
    private String shopName; // 상점 이름
    private TradeType tradeType; // 거래 유형 (BUY, SELL, BOTH)
    private UUID linkedEntityUUID; // 연결된 엔티티 UUID
    private final DoublyCircularLinkedList<ShopPage> pages; // 페이지 리스트
    private ShopPage currentPage; // 현재 페이지
    private int nextPageIndex; // 다음 페이지 인덱스

    public Shop(String shopID, String shopName, TradeType tradeType) {
        this.shopID = shopID;
        this.shopName = shopName;
        this.tradeType = tradeType;
        this.pages = new DoublyCircularLinkedList<>(); // 이중 원형 연결 리스트 초기화
        this.nextPageIndex = 0;
        addPage(new ShopPage(nextPageIndex++)); // 기본 페이지 추가
        this.currentPage = pages.get(0); // 첫 페이지로 설정
        Bukkit.getLogger().info("[Shop] 상점 생성 - 상점: " + shopID + ", 초기 페이지: 0");
    }

    // 페이지 추가
    public void addPage(ShopPage page) {
        page.setIndex(nextPageIndex++); // 페이지 인덱스 설정
        pages.add(page); // 리스트에 페이지 추가
        if (currentPage == null) {
            currentPage = page; // 첫 페이지면 현재 페이지로 설정
        }
        Bukkit.getLogger().info("[Shop] 페이지 추가 - 상점: " + shopID + ", 페이지 인덱스: " + page.getIndex());
    }

    // 현재 페이지 반환
    public ShopPage getCurrentPage() {
        if (currentPage == null || !pages.getAll().contains(currentPage)) {
            if (!pages.isEmpty()) {
                currentPage = pages.get(0); // 리스트가 비어 있지 않으면 첫 페이지로 설정
                Bukkit.getLogger().info("[Shop] 현재 페이지 null 또는 유효하지 않음, 첫 페이지로 설정 - 상점: " + shopID + ", 페이지: 0");
            } else {
                addPage(new ShopPage(nextPageIndex++));
                currentPage = pages.get(0);
                Bukkit.getLogger().info("[Shop] 빈 리스트에 새 페이지 생성 - 상점: " + shopID + ", 페이지: 0");
            }
        }
        return currentPage;
    }

    // 다음 페이지로 이동
    public void nextPage() {
        if (pages.isEmpty()) {
            addPage(new ShopPage(nextPageIndex++));
            currentPage = pages.get(0);
            Bukkit.getLogger().info("[Shop] 빈 리스트에 새 페이지 생성 - 상점: test, 페이지: 0");
            return;
        }
        ShopPage next = pages.getNext(currentPage);
        if (next != null && next != currentPage) {
            currentPage = next;
            Bukkit.getLogger().info("[Shop] 다음 페이지로 이동 - 상점: " + shopID + ", 페이지: " + currentPage.getIndex());
        }
    }

    // 이전 페이지로 이동
    public void prevPage() {
        if (pages.isEmpty()) {
            addPage(new ShopPage(nextPageIndex++));
            currentPage = pages.get(0);
            Bukkit.getLogger().info("[Shop] 빈 리스트에 새 페이지 생성 - 상점: test, 페이지: 0");
            return;
        }
        ShopPage prev = pages.getPrev(currentPage);
        if (prev != null && prev != currentPage) {
            currentPage = prev;
            Bukkit.getLogger().info("[Shop] 이전 페이지로 이동 - 상점: " + shopID + ", 페이지: " + currentPage.getIndex());
        }
    }

    public boolean nextIsNone() {
        if (pages.isEmpty()) return true;
        ShopPage next = pages.getNext(currentPage);
        return next == pages.get(0); // 다음 페이지가 첫 페이지와 같으면 true
    }

    // 빈 페이지 제거 및 슬롯 재정렬
    public void removeEmptyPagesAndRearrange(int[] validSlots) {
        Bukkit.getLogger().info("[Shop] 재정렬 시작 - 상점: " + shopID + ", 페이지 수: " + pages.size() + ", 현재 페이지: " + (currentPage != null ? currentPage.getIndex() : "null"));

        // 현재 페이지 인덱스 저장
        int currentPageIndex = currentPage != null ? currentPage.getIndex() : 0;

        // 모든 아이템 수집 (입력 순서 보존)
        List<Map.Entry<Integer, ShopItem>> allItems = new ArrayList<>();
        List<ShopPage> pageList = pages.getAll();
        pageList.sort(Comparator.comparingInt(ShopPage::getIndex)); // 페이지 인덱스 순으로 정렬
        for (ShopPage page : pageList) {
            allItems.addAll(page.getItems().entrySet()); // LinkedHashMap의 순서 유지
            page.clear();
        }
        Bukkit.getLogger().info("[Shop] 수집된 아이템 수: " + allItems.size());

        // 기존 페이지 제거
        pages.getAll().clear();
        nextPageIndex = 0;

        // 아이템 재배치
        if (!allItems.isEmpty()) {
            int slotIndex = 0;
            ShopPage newPage = new ShopPage(nextPageIndex++);
            pages.add(newPage);

            for (Map.Entry<Integer, ShopItem> entry : allItems) {
                if (slotIndex >= validSlots.length) {
                    slotIndex = 0;
                    newPage = new ShopPage(nextPageIndex++);
                    pages.add(newPage);
                    Bukkit.getLogger().info("[Shop] 새 페이지 생성 - 상점: " + shopID + ", 페이지 인덱스: " + newPage.getIndex());
                }
                newPage.addItem(validSlots[slotIndex], entry.getValue());
                slotIndex++;
            }
        } else {
            // 아이템이 없으면 기본 페이지 추가
            pages.add(new ShopPage(nextPageIndex++));
            Bukkit.getLogger().info("[Shop] 아이템 없음, 기본 페이지 생성 - 상점: " + shopID);
        }

        // 빈 페이지 제거
        List<ShopPage> pagesToRemove = new ArrayList<>();
        for (ShopPage page : pages.getAll()) {
            if (page.isEmpty() && pages.size() > 1) {
                pagesToRemove.add(page);
            }
        }
        for (ShopPage page : pagesToRemove) {
            pages.remove(page);
            Bukkit.getLogger().info("[Shop] 빈 페이지 제거 - 상점: " + shopID + ", 페이지 인덱스: " + page.getIndex());
        }

        // 페이지 인덱스 재설정
        int index = 0;
        for (ShopPage page : pages.getAll()) {
            page.setIndex(index++);
        }
        nextPageIndex = index;

        // 현재 페이지 복원
        currentPageIndex = Math.min(currentPageIndex, pages.size() - 1);
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        }
        currentPage = pages.get(currentPageIndex);
        if (currentPage == null && !pages.isEmpty()) {
            currentPage = pages.get(0);
            Bukkit.getLogger().info("[Shop] 현재 페이지 복원 실패, 첫 페이지로 설정 - 상점: " + shopID + ", 페이지: 0");
        }

        Bukkit.getLogger().info("[Shop] 재정렬 완료 - 상점: " + shopID + ", 페이지 수: " + pages.size() + ", 현재 페이지: " + (currentPage != null ? currentPage.getIndex() : "null"));
    }

    // getter 및 setter
    public String getShopID() {
        return shopID;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public UUID getLinkedEntityUUID() {
        return linkedEntityUUID;
    }

    public void setLinkedEntityUUID(UUID uuid) {
        this.linkedEntityUUID = uuid;
    }

    public DoublyCircularLinkedList<ShopPage> getPages() {
        return pages;
    }

    public int getNextPageIndex() {
        return nextPageIndex;
    }
}
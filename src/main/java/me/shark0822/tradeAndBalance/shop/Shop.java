package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.page.PageNode;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.TradeType;

import java.util.*;

public class Shop {
    private final String shopID;
    private String shopName;
    private TradeType tradeType;
    private UUID linkedEntityUUID;

    private PageNode head; // 첫 번째 페이지 노드
    private PageNode current; // 현재 활성화된 페이지
    private int nextPageIndex = 0; // 페이지 인덱스 관리용

    public Shop(String shopID, String shopName, TradeType tradeType) {
        this.shopID = shopID;
        this.shopName = shopName;
        this.tradeType = tradeType;

        ShopPage defaultPage = new ShopPage();
        addPage(defaultPage);
    }

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

    public void setLinkedEntityUUID(UUID linkedEntityUUID) {
        this.linkedEntityUUID = linkedEntityUUID;
    }

    public void addPage(ShopPage page) {
        PageNode newNode = new PageNode(nextPageIndex++, page);
        if (head == null) {
            head = newNode;
            head.setNext(head);
            head.setPrev(head);
            current = head;
        } else {
            PageNode tail = head.getPrev();
            tail.setNext(newNode);
            newNode.setPrev(tail);
            newNode.setNext(head);
            head.setPrev(newNode);
        }
    }

    public PageNode getCurrentPageNode() {
        if (current == null && head != null) current = head;
        return current;
    }

    public ShopPage getCurrentPage() {
        PageNode currentNode = getCurrentPageNode();
        if (currentNode == null) {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            current = head;
            return newPage;
        }
        return currentNode.getPage();
    }

    public void nextPage() {
        if (current == null) return;

        PageNode next = current.getNext();
        if (next == current) {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            next = current.getNext();
        }
        current = next;
    }

    public void prevPage() {
        if (current == null) return;

        PageNode prev = current.getPrev();
        if (prev == current) {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            prev = current.getPrev();
        }
        current = prev;
    }

    public void removeEmptyPages() {
        if (head == null) return;

        PageNode node = head;
        boolean checkedAll = false;

        while (!checkedAll) {
            PageNode nextNode = node.getNext();

            // 비어있으면 삭제
            if (node.getPage().isEmpty()) {
                // 단일 노드면 모두 삭제 후 종료
                if (node.getNext() == node) {
                    head = null;
                    current = null;
                    nextPageIndex = 0;
                    break;
                } else {
                    // 노드 연결 제거
                    node.getPrev().setNext(node.getNext());
                    node.getNext().setPrev(node.getPrev());

                    // head나 current가 삭제 노드면 업데이트
                    if (node == head) {
                        head = node.getNext();
                    }
                    if (node == current) {
                        current = node.getNext();
                    }
                }
            }

            node = nextNode;
            if (node == head) checkedAll = true;
        }
    }

    public void resetToFirstPage() {
        current = head;
    }

    public void clearAllPages() {
        head = null;
        current = null;
        nextPageIndex = 0;
    }

    public List<ShopItem> getAllItemsFlat() {
        List<ShopItem> all = new ArrayList<>();
        if (head == null) return all;

        PageNode start = head;
        PageNode node = start;

        do {
            all.addAll(node.getPage().getItems());
            node = node.getNext();
        } while (node != null && node != start);

        return all;
    }

    public int getPageCount() {
        if (head == null) return 0;

        int count = 0;
        PageNode node = head;

        do {
            count++;
            node = node.getNext();
        } while (node != null && node != head);

        return count;
    }
}
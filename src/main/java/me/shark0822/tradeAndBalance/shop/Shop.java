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
        System.out.println("[DEBUG] addPage called with nextPageIndex: " + nextPageIndex);
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
        System.out.println("[DEBUG] Page added with index: " + newNode.getIndex());
    }

    public PageNode getCurrentPageNode() {
        if (current == null && head != null) {
            current = head;
        }
        if (current == null) {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            current = head;
        }
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
        current = current.getNext();
    }

    public void prevPage() {
        if (current == null || current.getPrev() == null) return;
        current = current.getPrev();
    }

    public void addNextPage() {
        if (current == null || head == null) {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            current = head;
            current.markAsProcessed(); // 초기 페이지 처리 완료
            System.out.println("[DEBUG] addNextPage: created initial page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
            return;
        }

        PageNode next = current.getNext();
        if (next != head) {
            current = next;
            System.out.println("[DEBUG] addNextPage: moved to existing page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
        } else {
            ShopPage newPage = new ShopPage();
            addPage(newPage);
            current = head.getPrev(); // 새 페이지로 이동
            System.out.println("[DEBUG] addNextPage: added new page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
        }
    }

    public void removeEmptyPages() {
        if (head == null) {
            System.out.println("[DEBUG] removeEmptyPages: no pages to remove");
            return;
        }

        PageNode node = head;
        boolean checkedAll = false;
        int initialPageCount = getPageCount();
        System.out.println("[DEBUG] removeEmptyPages: starting with " + initialPageCount + " pages");

        while (!checkedAll) {
            PageNode nextNode = node.getNext();
            if (node.getPage().isEmpty() && !node.isNewlyAdded()) {
                if (getPageCount() <= 1) {
                    System.out.println("[DEBUG] removeEmptyPages: keeping last page (index=" + node.getIndex() + ")");
                    break;
                }

                System.out.println("[DEBUG] removeEmptyPages: removing empty page (index=" + node.getIndex() + ")");
                node.getPrev().setNext(node.getNext());
                node.getNext().setPrev(node.getPrev());

                if (node == head) {
                    head = node.getNext();
                }
                if (node == current) {
                    current = node.getNext();
                }

                node = nextNode;
                if (head == null) {
                    System.out.println("[DEBUG] removeEmptyPages: all pages removed");
                    checkedAll = true;
                } else if (node == head) {
                    checkedAll = true;
                }
                continue;
            }

            node.markAsProcessed(); // 페이지 처리 완료
            node = nextNode;
            if (node == head) {
                checkedAll = true;
            }
        }
        nextPageIndex = getPageCount(); // 인덱스 재설정
        System.out.println("[DEBUG] removeEmptyPages: finished with " + getPageCount() + " pages, nextPageIndex=" + nextPageIndex);
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
            if (node == null) break; // 순환 끊김 방지
            count++;
            node = node.getNext();
        } while (node != head);

        return count;
    }

    public PageNode getHead() {
        return head;
    }
}
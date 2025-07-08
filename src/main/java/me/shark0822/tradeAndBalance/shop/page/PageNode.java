package me.shark0822.tradeAndBalance.shop.page;

public class PageNode {
    private final int index;
    private final ShopPage page;
    private boolean isNewlyAdded; // 신규 페이지 여부
    private PageNode prev;
    private PageNode next;

    public PageNode(int index, ShopPage page) {
        this.index = index;
        this.page = page;
        this.isNewlyAdded = true; // 새 페이지로 초기화
    }

    public int getIndex() {
        return index;
    }

    public ShopPage getPage() {
        return page;
    }

    public boolean isNewlyAdded() {
        return isNewlyAdded;
    }

    public void markAsProcessed() {
        isNewlyAdded = false;
    }

    public PageNode getPrev() {
        return prev;
    }

    public void setPrev(PageNode prev) {
        this.prev = prev;
    }

    public PageNode getNext() {
        return next;
    }

    public void setNext(PageNode next) {
        this.next = next;
    }
}

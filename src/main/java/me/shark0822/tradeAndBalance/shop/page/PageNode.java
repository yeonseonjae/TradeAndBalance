package me.shark0822.tradeAndBalance.shop.page;

public class PageNode {
    private final int index;
    private final ShopPage page;

    private PageNode prev;
    private PageNode next;

    public PageNode(int index, ShopPage page) {
        this.index = index;
        this.page = page;
    }

    public int getIndex() {
        return index;
    }

    public ShopPage getPage() {
        return page;
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

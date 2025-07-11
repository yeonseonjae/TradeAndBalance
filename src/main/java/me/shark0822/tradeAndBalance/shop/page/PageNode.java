package me.shark0822.tradeAndBalance.shop.page;

public class PageNode {
    private int index; // 페이지의 고유 인덱스를 저장, 리스트 내 페이지 순서 식별
    private final ShopPage page; // 페이지 데이터를 저장하는 불변 필드, ShopPage 객체 참조
    private boolean isNewlyAdded; // 페이지가 새로 추가되었는지 여부를 나타내는 변수
    private PageNode prev; // 이전 노드 참조, 이중 연결 리스트의 역방향 탐색 지원
    private PageNode next; // 다음 노드 참조, 이중 연결 리스트의 순방향 탐색 지원

    public PageNode(int index, ShopPage page) {
        this.index = index; // 인덱스 필드에 입력된 인덱스 값 설정
        this.page = page; // 페이지 데이터 필드에 입력된 ShopPage 객체 저장
        this.isNewlyAdded = true; // 새 노드임을 표시, GUI 초기화 여부 추적
        this.prev = null; // 이전 노드 참조를 null로 초기화, Shop.addPage에서 설정
        this.next = null; // 다음 노드 참조를 null로 초기화, Shop.addPage에서 설정
    }

    public int getIndex() {
        return index; // 현재 노드의 인덱스 반환, 페이지 순서 확인용
    }

    public void setIndex(int index) {
        this.index = index; // 인덱스 값을 갱신, Shop.removeEmptyPages에서 재정렬 시 사용
    }

    public ShopPage getPage() {
        return page; // 저장된 ShopPage 객체 반환, 페이지의 아이템 목록 접근
    }

    public boolean isNewlyAdded() {
        return isNewlyAdded; // isNewlyAdded 변수 반환, 새 페이지 여부 확인
    }

    public void markAsProcessed() {
        isNewlyAdded = false; // isNewlyAdded를 false로 설정, 페이지가 GUI에서 처리됨을 표시
    }

    public PageNode getPrev() {
        return prev; // 이전 노드 참조 반환, 역방향 페이지 탐색 지원
    }

    public void setPrev(PageNode prev) {
        this.prev = prev; // 이전 노드 참조 설정, 리스트 구조 갱신 시 사용
    }

    public PageNode getNext() {
        return next; // 다음 노드 참조 반환, 순방향 페이지 탐색 지원
    }

    public void setNext(PageNode next) {
        this.next = next; // 다음 노드 참조 설정, 리스트 구조 갱신 시 사용
    }
}
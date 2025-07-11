package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.page.PageNode;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.TradeType;

import java.util.*;

public class Shop {
    private final String shopID; // 상점의 고유 ID 저장, 상점 식별용
    private String shopName; // 상점 이름을 저장, GUI에 표시
    private TradeType tradeType; // 상점의 거래 유형(BUY, SELL, BOTH) 저장
    private UUID linkedEntityUUID; // 상점과 연결된 엔티티 UUID 저장, NPC 연결 등에 사용
    private PageNode head; // 이중 원형 연결 리스트의 첫 노드 참조, 리스트 시작점
    private PageNode current; // 현재 활성화된 페이지 노드 참조
    private int nextPageIndex = 0; // 다음 페이지에 할당할 인덱스, 페이지 순서 관리

    public Shop(String shopID, String shopName, TradeType tradeType) {
        this.shopID = shopID; // shopID 필드에 입력된 ID 저장
        this.shopName = shopName; // shopName 필드에 입력된 이름 저장
        this.tradeType = tradeType; // tradeType 필드에 입력된 거래 유형 저장
        ShopPage defaultPage = new ShopPage(); // 기본 페이지 객체 생성
        addPage(defaultPage); // 기본 페이지를 리스트에 추가
    }

    public void addPage(ShopPage page) {
        System.out.println("[DEBUG] addPage called with nextPageIndex: " + nextPageIndex);
        PageNode newNode = new PageNode(nextPageIndex++, page); // 새 PageNode 생성, 인덱스 증가
        if (head == null) { // 리스트가 비어 있는지 확인
            head = newNode; // 새 노드를 head로 설정, 리스트 시작점 지정
            head.setNext(head); // head의 next를 head로 설정, 단일 노드 원형 구조 형성
            head.setPrev(head); // head의 prev를 head로 설정, 단일 노드 원형 구조 형성
            current = head; // 현재 페이지를 head로 설정
        } else {
            PageNode tail = head.getPrev(); // 마지막 노드(tail) 가져오기, head의 prev 참조
            tail.setNext(newNode); // tail의 next를 새 노드로 연결
            newNode.setPrev(tail); // 새 노드의 prev를 tail로 설정
            newNode.setNext(head); // 새 노드의 next를 head로 설정, 원형 구조 유지
            head.setPrev(newNode); // head의 prev를 새 노드로 설정, 원형 구조 완성
        }
        System.out.println("[DEBUG] Page added with index: " + newNode.getIndex());
    }

    public PageNode getCurrentPageNode() {
        if (current == null && head != null) { // current가 null이고 head가 존재하는지 확인
            current = head; // current를 head로 설정, 첫 페이지로 복구
        }
        if (current == null) { // current와 head 모두 null인지 확인
            ShopPage newPage = new ShopPage(); // 새 페이지 객체 생성
            addPage(newPage); // 새 페이지를 리스트에 추가
            current = head; // current를 새로 추가된 head로 설정
        }
        return current; // 현재 페이지 노드 반환
    }

    public ShopPage getCurrentPage() {
        PageNode currentNode = getCurrentPageNode(); // 현재 페이지 노드 가져오기
        if (currentNode == null) { // currentNode가 null인지 확인
            ShopPage newPage = new ShopPage(); // 새 페이지 객체 생성
            addPage(newPage); // 새 페이지를 리스트에 추가
            current = head; // current를 새 head로 설정
            return newPage; // 새 페이지 반환
        }
        return currentNode.getPage(); // 현재 페이지의 ShopPage 객체 반환
    }

    public void nextPage() {
        if (current == null) return; // current가 null이면 종료, 리스트가 비어 있음
        current = current.getNext(); // current를 다음 노드로 이동, 페이지 탐색
    }

    public void prevPage() {
        if (current == null || current.getPrev() == null) return; // current 또는 prev가 null이면 종료
        current = current.getPrev(); // current를 이전 노드로 이동, 페이지 탐색
    }

    public void addNextPage() {
        if (current == null || head == null) { // current 또는 head가 null인지 확인
            ShopPage newPage = new ShopPage(); // 새 페이지 객체 생성
            addPage(newPage); // 새 페이지를 리스트에 추가
            current = head; // current를 새 head로 설정
            current.markAsProcessed(); // 새 페이지의 isNewlyAdded를 false로 설정
            System.out.println("[DEBUG] addNextPage: created initial page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
            return;
        }
        PageNode next = current.getNext(); // 현재 노드의 다음 노드 가져오기
        if (next != head) { // 다음 노드가 head가 아닌지 확인 (기존 페이지 존재)
            current = next; // current를 다음 노드로 이동
            System.out.println("[DEBUG] addNextPage: moved to existing page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
        } else {
            ShopPage newPage = new ShopPage(); // 새 페이지 객체 생성
            addPage(newPage); // 새 페이지를 리스트에 추가
            current = head.getPrev(); // current를 새로 추가된 페이지(tail)로 이동
            System.out.println("[DEBUG] addNextPage: added new page, currentIndex=" + current.getIndex() + ", totalPages=" + getPageCount());
        }
    }

    public void removeEmptyPages() {
        if (head == null) { // 리스트가 비어 있는지 확인
            System.out.println("[DEBUG] removeEmptyPages: no pages to remove");
            return;
        }
        PageNode node = head; // 순회 시작을 head로 설정
        int initialPageCount = getPageCount(); // 초기 페이지 수 계산
        System.out.println("[DEBUG] removeEmptyPages: starting with " + initialPageCount + " pages");
        do {
            PageNode nextNode = node.getNext(); // 다음 노드 미리 저장, 순회 중 제거 대비
            if (node.getPage().isEmpty()) { // 현재 페이지가 비어 있는지 확인
                if (getPageCount() <= 1) { // 페이지가 하나뿐인지 확인
                    System.out.println("[DEBUG] removeEmptyPages: keeping last page (index=" + node.getIndex() + ")");
                    break;
                }
                System.out.println("[DEBUG] removeEmptyPages: removing empty page (index=" + node.getIndex() + ")");
                if (node == head) { // 현재 노드가 head인지 확인
                    head = nextNode == head ? null : nextNode; // head 갱신, 단일 노드면 null
                }
                if (node.getPrev() != null) { // 이전 노드가 존재하는지 확인
                    node.getPrev().setNext(nextNode); // 이전 노드의 next를 다음 노드로 연결
                }
                if (nextNode != null) { // 다음 노드가 존재하는지 확인
                    nextNode.setPrev(node.getPrev()); // 다음 노드의 prev를 이전 노드로 연결
                }
                if (node == current) { // 현재 노드가 current인지 확인
                    current = nextNode != null ? nextNode : head; // current를 다음 노드 또는 head로 갱신
                }
            }
            node = nextNode; // 다음 노드로 이동, 순회 계속
        } while (node != null && node != head); // head로 돌아오거나 null일 때까지 순회
        if (head != null) { // 리스트가 비어 있지 않은지 확인
            PageNode currentNode = head; // 인덱스 재설정을 위해 head에서 시작
            int index = 0; // 새 인덱스 초기화
            do {
                currentNode.setIndex(index++); // 현재 노드의 인덱스 재설정
                System.out.println("[DEBUG] removeEmptyPages: set index=" + (index-1) + " for page");
                currentNode = currentNode.getNext(); // 다음 노드로 이동
            } while (currentNode != null && currentNode != head); // head로 돌아올 때까지 반복
            nextPageIndex = index; // 다음 페이지 인덱스 갱신
        } else {
            nextPageIndex = 0; // 리스트가 비었으므로 인덱스 초기화
            current = null; // current를 null로 설정
        }
        System.out.println("[DEBUG] removeEmptyPages: finished with " + getPageCount() + " pages, nextPageIndex=" + nextPageIndex);
    }

    public void resetToFirstPage() {
        current = head; // current를 head로 설정, 첫 페이지로 이동
    }

    public void clearAllPages() {
        head = null; // head 참조 제거, 리스트 초기화
        current = null; // current 참조 제거
        nextPageIndex = 0; // 다음 페이지 인덱스 초기화
    }

    public int getPageCount() {
        if (head == null) return 0; // 리스트가 비어 있으면 0 반환
        int count = 0; // 페이지 수를 세기 위한 카운터
        PageNode node = head; // 순회 시작을 head로 설정
        do {
            if (node == null) break; // 노드가 null이면 순회 중단
            count++; // 페이지 수 증가
            node = node.getNext(); // 다음 노드로 이동
        } while (node != head); // head로 돌아올 때까지 반복
        return count; // 총 페이지 수 반환
    }

    public PageNode getHead() {
        return head; // head 노드 참조 반환, 리스트 시작점 접근
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
}
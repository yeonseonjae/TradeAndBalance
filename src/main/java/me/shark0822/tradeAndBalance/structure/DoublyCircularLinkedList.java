package me.shark0822.tradeAndBalance.structure;

public class DoublyCircularLinkedList<T> {
    private Node<T> head; // 리스트의 첫 노드 참조
    private int size; // 리스트의 노드 수

    // 내부 노드 클래스
    private static class Node<T> {
        T data; // 노드가 저장하는 데이터
        Node<T> prev; // 이전 노드 참조
        Node<T> next; // 다음 노드 참조

        Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    public DoublyCircularLinkedList() {
        head = null;
        size = 0;
    }

    // 리스트에 노드 추가
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) { // 리스트가 비어 있는 경우
            head = newNode;
            head.next = head; // 원형 구조: 자기 자신을 가리킴
            head.prev = head;
        } else {
            Node<T> tail = head.prev; // 마지막 노드 (head의 이전 노드)
            tail.next = newNode; // tail의 다음을 새 노드로 연결
            newNode.prev = tail; // 새 노드의 이전을 tail로 설정
            newNode.next = head; // 새 노드의 다음을 head로 설정
            head.prev = newNode; // head의 이전을 새 노드로 설정
        }
        size++; // 리스트 크기 증가
    }

    // 리스트에서 노드 제거
    public void remove(T data) {
        if (head == null) return; // 빈 리스트면 종료
        Node<T> current = head;
        do {
            if (current.data.equals(data)) { // 데이터가 일치하는 노드 찾기
                // Java의 equals 메서드: 객체의 내용 비교
                if (current == head && size == 1) { // 단일 노드인 경우
                    head = null;
                } else {
                    current.prev.next = current.next; // 이전 노드의 다음을 현재의 다음으로 연결
                    current.next.prev = current.prev; // 다음 노드의 이전을 현재의 이전으로 연결
                    if (current == head) { // head 노드 제거 시
                        head = current.next;
                    }
                }
                size--; // 리스트 크기 감소
                return;
            }
            current = current.next;
        } while (current != head); // 원형 리스트이므로 head로 돌아올 때까지 순회
    }

    // 특정 데이터의 다음 데이터 반환
    public T getNext(T data) {
        if (head == null) return null;
        Node<T> current = head;
        do {
            if (current.data.equals(data)) {
                return current.next.data; // 다음 노드의 데이터 반환
            }
            current = current.next;
        } while (current != head);
        return null; // 데이터가 없으면 null 반환
    }

    // 특정 데이터의 이전 데이터 반환
    public T getPrev(T data) {
        if (head == null) return null;
        Node<T> current = head;
        do {
            if (current.data.equals(data)) {
                return current.prev.data; // 이전 노드의 데이터 반환
            }
            current = current.next;
        } while (current != head);
        return null;
    }

    // 리스트 크기 반환
    public int size() {
        return size;
    }

    // 리스트가 비어 있는지 확인
    public boolean isEmpty() {
        return size == 0; // size가 0이면 true 반환
    }

    // 리스트의 모든 데이터 반환
    public java.util.List<T> getAll() {
        java.util.List<T> result = new java.util.ArrayList<>();
        if (head == null) return result;
        Node<T> current = head;
        do {
            result.add(current.data); // ArrayList에 데이터 추가
            current = current.next;
        } while (current != head);
        return result;
    }

    // 특정 인덱스의 데이터 반환
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Invalid index");
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public boolean replace(T oldData, T newData) {
        if (head == null) return false;

        Node<T> current = head;
        do {
            if (current.data.equals(oldData)) {
                current.data = newData; // 데이터만 교체
                return true;
            }
            current = current.next;
        } while (current != head);

        return false; // 찾지 못한 경우
    }

    public boolean contains(T data) {
        if (head == null) return false;
        Node<T> current = head;
        do {
            if (current.data.equals(data)) {
                return true;
            }
            current = current.next;
        } while (current != head);
        return false;
    }
}

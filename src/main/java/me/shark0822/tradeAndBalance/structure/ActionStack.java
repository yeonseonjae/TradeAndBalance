package me.shark0822.tradeAndBalance.structure;

import java.util.ArrayList;

public class ActionStack<T> {
    private final ArrayList<T> items; // 동적 크기 조절을 위한 ArrayList
    private int top;

    public ActionStack() {
        items = new ArrayList<>(); // ArrayList 초기화: 동적 배열로 크기 자동 조절
        top = -1; // 스택이 비어 있을 때 top은 -1
    }

    // 요소 추가
    public void push(T item) {
        items.add(item); // ArrayList 끝에 요소 추가
        top++; // top 인덱스 증가
    }

    // 요소 제거 및 반환
    public T pop() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        T item = items.remove(top); // ArrayList의 마지막 요소 제거
        top--; // top 인덱스 감소
        return item;
    }

    // 최상위 요소 확인
    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        return items.get(top); // ArrayList의 top 인덱스 요소 반환
    }

    // 스택이 비어 있는지 확인
    public boolean isEmpty() {
        return top == -1; // top이 -1이면 빈 스택
    }

    // 스택 크기 반환
    public int size() {
        return top + 1; // top은 0-based 인덱스이므로 +1
    }

    // 스택 비우기
    public void clear() {
        items.clear(); // ArrayList의 모든 요소 제거
        top = -1; // top 초기화
    }
}

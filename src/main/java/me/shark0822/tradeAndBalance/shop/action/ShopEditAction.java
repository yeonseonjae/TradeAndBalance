package me.shark0822.tradeAndBalance.shop.action;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopPage;
import me.shark0822.tradeAndBalance.gui.EditorGUI;
import org.bukkit.Bukkit;

public class ShopEditAction implements EditorAction {
    private final Shop shop;
    private final int pageIndex; // 페이지 인덱스 저장
    private final ShopPage beforePage; // 변경 전 페이지 상태
    private final ShopPage afterPage; // 변경 후 페이지 상태
    private final EditorGUI editorGUI;

    public ShopEditAction(Shop shop, int pageIndex, ShopPage beforePage, ShopPage afterPage, EditorGUI editorGUI) {
        this.shop = shop;
        this.pageIndex = pageIndex;
        this.beforePage = beforePage.clone(); // 깊은 복사로 상태 보존
        this.afterPage = afterPage.clone(); // 깊은 복사로 상태 보존
        this.editorGUI = editorGUI;

        Bukkit.getLogger().info("[ShopEditAction] 액션 생성 - 페이지 인덱스: " + pageIndex +
                ", 변경 전 아이템 수: " + beforePage.getItems().size() +
                ", 변경 후 아이템 수: " + afterPage.getItems().size());
    }

    @Override
    public void undo() {
        Bukkit.getLogger().info("[ShopEditAction] Undo 실행 - 페이지 인덱스: " + pageIndex);

        // 페이지 전체를 변경 전 상태로 복원
        ShopPage targetPage = shop.getPages().get(pageIndex);
        if (targetPage != null) {
            // 페이지 내용 완전 교체
            targetPage.clear();
            for (var entry : beforePage.getItems().entrySet()) {
                targetPage.addItem(entry.getKey(), entry.getValue());
            }

            Bukkit.getLogger().info("[ShopEditAction] Undo 완료 - 복원된 아이템 수: " + beforePage.getItems().size());
        } else {
            Bukkit.getLogger().warning("[ShopEditAction] Undo 실패 - 페이지를 찾을 수 없음: " + pageIndex);
        }

        editorGUI.saveAndRearrange(); // 재정렬 수행
    }

    @Override
    public void redo() {
        Bukkit.getLogger().info("[ShopEditAction] Redo 실행 - 페이지 인덱스: " + pageIndex);

        // 페이지 전체를 변경 후 상태로 복원
        ShopPage targetPage = shop.getPages().get(pageIndex);
        if (targetPage != null) {
            // 페이지 내용 완전 교체
            targetPage.clear();
            for (var entry : afterPage.getItems().entrySet()) {
                targetPage.addItem(entry.getKey(), entry.getValue());
            }

            Bukkit.getLogger().info("[ShopEditAction] Redo 완료 - 복원된 아이템 수: " + afterPage.getItems().size());
        } else {
            Bukkit.getLogger().warning("[ShopEditAction] Redo 실패 - 페이지를 찾을 수 없음: " + pageIndex);
        }

        editorGUI.saveAndRearrange(); // 재정렬 수행
    }
}
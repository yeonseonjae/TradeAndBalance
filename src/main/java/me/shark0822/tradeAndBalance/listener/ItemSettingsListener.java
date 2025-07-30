package me.shark0822.tradeAndBalance.listener;

import me.shark0822.tradeAndBalance.TradeAndBalance;
import me.shark0822.tradeAndBalance.gui.ItemSettingsGUI;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemSettingsListener implements Listener {
    private final ShopManager shopManager;
    private final Map<UUID, ItemSettingsGUI> playerItemSettingsGUIs;
    private final Map<UUID, String> pendingSettings;

    public ItemSettingsListener(ShopManager shopManager) {
        this.shopManager = shopManager;
        this.playerItemSettingsGUIs = new HashMap<>();
        this.pendingSettings = new HashMap<>();

        // 디버그 메시지: 리스너 초기화
        Bukkit.getLogger().info("[ItemSettingsListener] 리스너가 초기화되었습니다.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemSettingsGUI itemSettingsGUI = playerItemSettingsGUIs.get(player.getUniqueId());
        if (itemSettingsGUI == null || !event.getInventory().equals(itemSettingsGUI.getInventory())) return;

        event.setCancelled(true);
        int slot = event.getSlot();

        // 디버그 메시지: 클릭된 슬롯 정보
        Bukkit.getLogger().info("[ItemSettingsListener] 플레이어 " + player.getName() + "가 슬롯 " + slot + "을 클릭했습니다.");

        switch (slot) {
            case 10 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 구매 가격 설정 시작 - " + player.getName());
                pendingSettings.put(player.getUniqueId(), "구매 가격");
                itemSettingsGUI.startSetting(player, "구매 가격");
                Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings에 추가됨: " + player.getUniqueId() + " -> 구매 가격");
            }
            case 12 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 판매 가격 설정 시작 - " + player.getName());
                pendingSettings.put(player.getUniqueId(), "판매 가격");
                itemSettingsGUI.startSetting(player, "판매 가격");
                Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings에 추가됨: " + player.getUniqueId() + " -> 판매 가격");
            }
            case 14 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 제한 타입 토글 - " + player.getName());
                itemSettingsGUI.toggleLimitType();
            }
            case 16 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 제한 수량 설정 시작 - " + player.getName());
                pendingSettings.put(player.getUniqueId(), "제한 수량");
                itemSettingsGUI.startSetting(player, "제한 수량");
                Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings에 추가됨: " + player.getUniqueId() + " -> 제한 수량");
            }
            case 18 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] Undo 실행 - " + player.getName());
                if (shopManager.canUndoItemSettings()) {
                    shopManager.undoItemSettings();
                    player.sendMessage(TextUtil.format("&a실행 취소가 완료되었습니다."));
                } else {
                    player.sendMessage(TextUtil.format("&c실행 취소할 작업이 없습니다."));
                }
            }
            case 19 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] Redo 실행 - " + player.getName());
                if (shopManager.canRedoItemSettings()) {
                    shopManager.redoItemSettings();
                    player.sendMessage(TextUtil.format("&a다시 실행이 완료되었습니다."));
                } else {
                    player.sendMessage(TextUtil.format("&c다시 실행할 작업이 없습니다."));
                }
            }
            case 25 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 취소 버튼 클릭 - " + player.getName());
                itemSettingsGUI.cancel(player);
            }
            case 26 -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 확인 버튼 클릭 - " + player.getName());
                itemSettingsGUI.confirm(player);
            }
            default -> {
                Bukkit.getLogger().info("[ItemSettingsListener] 처리되지 않은 슬롯 클릭: " + slot + " - " + player.getName());
            }
        }

        debugPendingSettings();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 디버그 메시지: 채팅 이벤트 발생
        Bukkit.getLogger().info("[ItemSettingsListener] 채팅 이벤트 발생 - " + player.getName() + ": " + event.getMessage());
        Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings에서 플레이어 확인 중: " + playerUUID);
        Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings 포함 여부: " + pendingSettings.containsKey(playerUUID));

        String setting = pendingSettings.get(playerUUID);
        if (setting != null) {
            Bukkit.getLogger().info("[ItemSettingsListener] 설정 대기 중인 항목 발견: " + setting);

            // 이벤트 취소 및 pendingSettings에서 제거
            event.setCancelled(true);
            pendingSettings.remove(playerUUID);

            Bukkit.getLogger().info("[ItemSettingsListener] 채팅 이벤트 취소됨, pendingSettings에서 제거됨");

            ItemSettingsGUI itemSettingsGUI = playerItemSettingsGUIs.get(playerUUID);
            if (itemSettingsGUI == null) {
                Bukkit.getLogger().warning("[ItemSettingsListener] ItemSettingsGUI를 찾을 수 없습니다: " + player.getName());
                return;
            }

            String inputMessage = event.getMessage().trim();
            Bukkit.getLogger().info("[ItemSettingsListener] 입력받은 메시지: '" + inputMessage + "'");

            // 메인 스레드에서 GUI 작업 실행
            Bukkit.getScheduler().runTask(TradeAndBalance.getPlugin(TradeAndBalance.class), () -> {
                try {
                    Bukkit.getLogger().info("[ItemSettingsListener] 설정 적용 시작: " + setting + " = " + inputMessage);
                    itemSettingsGUI.applySetting(setting, inputMessage);

                    if (!"cancel".equalsIgnoreCase(inputMessage)) {
                        player.sendMessage(TextUtil.format("&a" + setting + "이(가) 임시 설정되었습니다."));
                        player.sendMessage(TextUtil.format("&7확인 버튼을 눌러 저장하거나 계속 편집하세요."));
                        Bukkit.getLogger().info("[ItemSettingsListener] 설정 성공적으로 적용됨: " + setting);
                    } else {
                        Bukkit.getLogger().info("[ItemSettingsListener] 설정 취소됨: " + setting);
                    }

                    itemSettingsGUI.open(player);
                    Bukkit.getLogger().info("[ItemSettingsListener] GUI 다시 열림: " + player.getName());

                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[ItemSettingsListener] 설정 적용 실패: " + e.getMessage());
                    player.sendMessage(TextUtil.format("&c" + e.getMessage()));

                    // 다시 설정 입력 요청
                    itemSettingsGUI.startSetting(player, setting);
                    pendingSettings.put(playerUUID, setting);
                    Bukkit.getLogger().info("[ItemSettingsListener] 설정 재입력 요청: " + setting);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[ItemSettingsListener] 예상치 못한 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage(TextUtil.format("&c설정 중 오류가 발생했습니다."));
                    itemSettingsGUI.cancel(player);
                }
            });
        } else {
            // 디버그 메시지: 대기 중인 설정이 없음
            Bukkit.getLogger().info("[ItemSettingsListener] 대기 중인 설정이 없습니다 - " + player.getName());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ItemSettingsGUI itemSettingsGUI = playerItemSettingsGUIs.get(playerUUID);

        // 디버그 메시지: 인벤토리 닫기
        Bukkit.getLogger().info("[ItemSettingsListener] 인벤토리 닫기 이벤트 - " + player.getName());

        if (itemSettingsGUI == null || !event.getInventory().equals(itemSettingsGUI.getInventory())) {
            Bukkit.getLogger().info("[ItemSettingsListener] ItemSettingsGUI와 일치하지 않는 인벤토리 - " + player.getName());
            return;
        }

        // 채팅 입력 대기 중이 아닐 때만 자동 확인 처리
        if (!pendingSettings.containsKey(playerUUID)) {
            Bukkit.getLogger().info("[ItemSettingsListener] 자동 확인 처리 시작 - " + player.getName());
            playerItemSettingsGUIs.remove(playerUUID);

            // 메인 스레드에서 실행
            Bukkit.getScheduler().runTask(TradeAndBalance.getPlugin(TradeAndBalance.class), () -> {
                itemSettingsGUI.confirm(player);
                Bukkit.getLogger().info("[ItemSettingsListener] 자동 확인 완료 - " + player.getName());
            });
        } else {
            Bukkit.getLogger().info("[ItemSettingsListener] 채팅 입력 대기 중이므로 자동 확인 생략 - " + player.getName());
        }
    }

    public static void setItemSettingsGUI(Player player, ItemSettingsGUI gui) {
        TradeAndBalance plugin = TradeAndBalance.getPlugin(TradeAndBalance.class);
        ItemSettingsListener listener = plugin.getItemSettingsListener();

        // 디버그 메시지: GUI 설정
        Bukkit.getLogger().info("[ItemSettingsListener] ItemSettingsGUI 설정 - " + player.getName());

        listener.playerItemSettingsGUIs.put(player.getUniqueId(), gui);
    }

    public void removeItemSettingsGUI(Player player) {
        UUID playerUUID = player.getUniqueId();

        // 디버그 메시지: GUI 제거
        Bukkit.getLogger().info("[ItemSettingsListener] ItemSettingsGUI 제거 - " + player.getName());

        playerItemSettingsGUIs.remove(playerUUID);
        pendingSettings.remove(playerUUID);

        Bukkit.getLogger().info("[ItemSettingsListener] GUI 및 pendingSettings 제거 완료 - " + player.getName());
    }

    public boolean hasItemSettingsGUI(Player player) {
        return playerItemSettingsGUIs.containsKey(player.getUniqueId());
    }

    // 디버그용 메서드 추가
    public void debugPendingSettings() {
        Bukkit.getLogger().info("[ItemSettingsListener] === Pending Settings Debug ===");
        Bukkit.getLogger().info("[ItemSettingsListener] pendingSettings 크기: " + pendingSettings.size());
        for (Map.Entry<UUID, String> entry : pendingSettings.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            String playerName = player != null ? player.getName() : "Unknown";
            Bukkit.getLogger().info("[ItemSettingsListener] - " + playerName + " (" + entry.getKey() + ") -> " + entry.getValue());
        }

        Bukkit.getLogger().info("[ItemSettingsListener] playerItemSettingsGUIs 크기: " + playerItemSettingsGUIs.size());
        for (UUID uuid : playerItemSettingsGUIs.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            String playerName = player != null ? player.getName() : "Unknown";
            Bukkit.getLogger().info("[ItemSettingsListener] - " + playerName + " (" + uuid + ")");
        }
        Bukkit.getLogger().info("[ItemSettingsListener] ===========================");
    }
}
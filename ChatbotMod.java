package ru.jeremy.chatbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatbotMod implements ClientModInitializer {

    // ==== НАСТРОЙКИ — МЕНЯЙ ТУТ ====
    // триггер -> ответ
    private static final Map<String, String> TRIGGERS = new LinkedHashMap<>();
    static {
        TRIGGERS.put("привет", "Привет! Как дела?");
        TRIGGERS.put("#bot", "хочешь бота? Пиши jeremy890");
    }
    private static final long COOLDOWN_MS = 60_000; // кулдаун на игрока, 60 сек
    // ===============================

    private final Map<String, Long> lastReply = new LinkedHashMap<>();
    private long lastSent = 0;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleChat(message.getString());
        });
    }

    private void handleChat(String text) {
        String low = text.toLowerCase();

        for (Map.Entry<String, String> e : TRIGGERS.entrySet()) {
            if (!low.contains(e.getKey().toLowerCase())) continue;

            String nick = extractNick(text);
            if (nick == null) return;

            long now = System.currentTimeMillis();
            Long last = lastReply.get(nick);
            if (last != null && now - last < COOLDOWN_MS) return;
            lastReply.put(nick, now);

            reply(e.getValue());
            return;
        }
    }

    private String extractNick(String text) {
        int colon = text.indexOf('>');
        if (colon > 0 && colon < 25) {
            return text.substring(0, colon).trim();
        }
        if (text.startsWith("<")) {
            int end = text.indexOf('>');
            if (end > 0) return text.substring(1, end).trim();
        }
        return null;
    }

    private void reply(String answer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastSent < 3000) return;
        lastSent = now;
        client.player.sendChatMessage(answer);
    }
}

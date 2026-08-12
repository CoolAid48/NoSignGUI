package me.coolaid.nosigngui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
//? if <1.19 {
/*import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
*///?}
//? if <1.17 {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?} else {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//?}

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NoSignGUI {
    public static final String MOD_ID = "nosigngui";
    public static final Logger LOGGER =
            //? if <1.17 {
            /*LogManager.getLogger(MOD_ID);
            *///?} else {
            LoggerFactory.getLogger(MOD_ID);
            //?}
    private static volatile boolean signGuiEnabled = true;
    private static final long PLACED_SIGN_GUI_TIMEOUT_MILLIS = 5000L;
    private static final Map<BlockPos, Long> placedSignsAwaitingGui = new ConcurrentHashMap<>();

    private static final Component MESSAGE =
            //? if <1.19 {
            /*new TranslatableComponent("component.toggleGuiKey.message")
            *///?} else {
            Component.translatable("component.toggleGuiKey.message")
            //?}
            .withStyle(ChatFormatting.WHITE);
    private static final Component ENABLED =
            //? if <1.19 {
            /*new TranslatableComponent("component.toggleGuiKey.enabled")
            *///?} else {
            Component.translatable("component.toggleGuiKey.enabled")
            //?}
            .withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED =
            //? if <1.19 {
            /*new TranslatableComponent("component.toggleGuiKey.disabled")
            *///?} else {
            Component.translatable("component.toggleGuiKey.disabled")
            //?}
            .withStyle(ChatFormatting.RED);

    private static final Component ENABLED_MESSAGE =
            //? if <1.19 {
            /*new TextComponent("")
            *///?} else {
            Component.empty()
            //?}
            .append(MESSAGE.copy())
            .append(ENABLED.copy());
    private static final Component DISABLED_MESSAGE =
            //? if <1.19 {
            /*new TextComponent("")
            *///?} else {
            Component.empty()
            //?}
            .append(MESSAGE.copy())
            .append(DISABLED.copy());

    public static void init(Path configDirectory) {
        signGuiEnabled = NoSignGUIConfig.load(configDirectory);
        LOGGER.info("NoSignGUI initialized; sign GUI is {}", signGuiEnabled ? "enabled" : "disabled");
    }

    private NoSignGUI() {
    }

    public static boolean isSignGuiEnabled() {
        return signGuiEnabled;
    }

    public static void markPlacedSignForSkippedGui(BlockPos pos) {
        if (!signGuiEnabled) {
            placedSignsAwaitingGui.put(pos.immutable(), System.currentTimeMillis());
        }
    }

    public static boolean shouldSkipPlacedSignGui(BlockPos pos) {
        if (signGuiEnabled) {
            placedSignsAwaitingGui.remove(pos);
            return false;
        }

        long now = System.currentTimeMillis();
        placedSignsAwaitingGui.entrySet().removeIf(entry -> now - entry.getValue() > PLACED_SIGN_GUI_TIMEOUT_MILLIS);

        Long markedAt = placedSignsAwaitingGui.remove(pos);
        return markedAt != null && now - markedAt <= PLACED_SIGN_GUI_TIMEOUT_MILLIS;
    }

    public static synchronized Component toggleSignGuiMessage() {
        signGuiEnabled = !signGuiEnabled;
        NoSignGUIConfig.save(signGuiEnabled);
        if (signGuiEnabled) {
            placedSignsAwaitingGui.clear();
        }
        return signGuiEnabled ? DISABLED_MESSAGE : ENABLED_MESSAGE;
    }
}

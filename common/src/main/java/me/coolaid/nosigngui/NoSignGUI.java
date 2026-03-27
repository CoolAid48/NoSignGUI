package me.coolaid.nosigngui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NoSignGUI {
    public static final String MOD_ID = "nosigngui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.

    private static volatile boolean signGuiEnabled = true;

    private static final Component MESSAGE = Component
            .translatable("component.toggleGuiKey.message")
            .withStyle(ChatFormatting.WHITE);
    private static final Component ENABLED = Component
            .translatable("component.toggleGuiKey.enabled")
            .withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = Component
            .translatable("component.toggleGuiKey.disabled")
            .withStyle(ChatFormatting.RED);

    private static final Component ENABLED_MESSAGE = Component.empty()
            .append(MESSAGE.copy())
            .append(ENABLED.copy());
    private static final Component DISABLED_MESSAGE = Component.empty()
            .append(MESSAGE.copy())
            .append(DISABLED.copy());

    public static void init() {
        LOGGER.info("The sign is NOT GUI-ing... Check out my Hardcore World on Twitch");
    }

    private NoSignGUI() {
    }

    public static boolean isSignGuiEnabled() {
        return signGuiEnabled;
    }

    public static Component toggleSignGuiMessage() {
        signGuiEnabled = !signGuiEnabled;
        return signGuiEnabled ? DISABLED_MESSAGE : ENABLED_MESSAGE;
    }
}
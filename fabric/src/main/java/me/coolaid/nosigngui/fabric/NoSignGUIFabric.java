package me.coolaid.nosigngui.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.nosigngui.NoSignGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class NoSignGUIFabric implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse("nosigngui"));

    private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
            "key.nosigngui.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    @Override
    public void onInitializeClient() {
        NoSignGUI.init();
        KeyMappingHelper.registerKeyMapping(TOGGLE_GUI_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_GUI_KEY.consumeClick()) {
                if (client.player != null) {
                    client.player.sendOverlayMessage(NoSignGUI.toggleSignGuiMessage());
                }
            }
        });
    }
}
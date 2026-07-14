package me.coolaid.nosigngui.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.nosigngui.NoSignGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.minecraft.client.KeyMapping;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else if >=1.21.9 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.lwjgl.glfw.GLFW;

public final class NoSignGUIFabric implements ClientModInitializer {
    //? if >=1.21.11 {
    /*private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse("nosigngui"));
    *///?} else if >=1.21.9 {
    /*private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(ResourceLocation.parse("nosigngui"));
    *///?}

    private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
            "key.nosigngui.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            //? if >=1.21.9 {
            /*CATEGORY
            *///?} else {
            "key.categories.nosigngui"
            //?}
    );

    @Override
    public void onInitializeClient() {
        NoSignGUI.init();
        //? if >=26.1 {
        /*KeyMappingHelper.registerKeyMapping(TOGGLE_GUI_KEY);
        *///?} else {
        KeyBindingHelper.registerKeyBinding(TOGGLE_GUI_KEY);
        //?}

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_GUI_KEY.consumeClick()) {
                if (client.player != null) {
                    //? if >=26.1 {
                    /*client.player.sendOverlayMessage(NoSignGUI.toggleSignGuiMessage());
                    *///?} else {
                    client.player.displayClientMessage(NoSignGUI.toggleSignGuiMessage(), true);
                    //?}
                }
            }
        });
    }
}

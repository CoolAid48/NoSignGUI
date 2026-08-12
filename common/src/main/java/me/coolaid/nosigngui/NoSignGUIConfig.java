package me.coolaid.nosigngui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

final class NoSignGUIConfig {
    private static final String FILE_NAME = NoSignGUI.MOD_ID + ".properties";
    private static final String SIGN_GUI_ENABLED = "signGuiEnabled";
    private static final boolean DEFAULT_SIGN_GUI_ENABLED = true;

    private static Path configFile;

    private NoSignGUIConfig() {
    }

    static boolean load(Path configDirectory) {
        configFile = configDirectory.resolve(FILE_NAME);

        if (Files.notExists(configFile)) {
            save(DEFAULT_SIGN_GUI_ENABLED);
            return DEFAULT_SIGN_GUI_ENABLED;
        }

        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException exception) {
            NoSignGUI.LOGGER.warn("Could not read config {}; using the default toggle state", configFile, exception);
            return DEFAULT_SIGN_GUI_ENABLED;
        }

        String configuredValue = properties.getProperty(SIGN_GUI_ENABLED);
        if ("true".equalsIgnoreCase(configuredValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(configuredValue)) {
            return false;
        }

        NoSignGUI.LOGGER.warn("Invalid {} value in {}; using the default toggle state", SIGN_GUI_ENABLED, configFile);
        return DEFAULT_SIGN_GUI_ENABLED;
    }

    static void save(boolean signGuiEnabled) {
        if (configFile == null) {
            NoSignGUI.LOGGER.warn("Cannot save toggle state before the config has been initialized");
            return;
        }

        try {
            Files.createDirectories(configFile.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(
                    configFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                writer.write("# Whether the sign editing screen opens after placing a sign.");
                writer.newLine();
                writer.write(SIGN_GUI_ENABLED + "=" + signGuiEnabled);
                writer.newLine();
            }
        } catch (IOException exception) {
            NoSignGUI.LOGGER.warn("Could not save toggle state to {}", configFile, exception);
        }
    }
}

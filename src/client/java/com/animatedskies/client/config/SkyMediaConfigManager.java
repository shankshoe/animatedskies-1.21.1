package com.animatedskies.client.config;

import com.animatedskies.client.enum_and_class.*;
import com.animatedskies.client.utils.SkyMediaManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class SkyMediaConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_DIR = Path.of("skyassets", "configs");

    /*
     * Prevent re-reading unchanged files
     */
    private static final Map<Path, Long> LAST_MODIFIED = new HashMap<>();

    public static void loadAllConfigs() {

        try {

            if (!Files.exists(CONFIG_DIR)) {
                return;
            }

            Files.walk(CONFIG_DIR)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .endsWith(".json"))
                    .forEach(SkyMediaConfigManager::loadConfig);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(Path configPath) {

        try {

            long modified = Files.getLastModifiedTime(configPath).toMillis();


            LAST_MODIFIED.put(configPath, modified);

            String fileName = configPath.getFileName().toString();

            String mediaName = fileName.substring(0, fileName.lastIndexOf('.'));

            SkyMedia media = SkyMediaManager.get(mediaName);

            if (media == null) {return;}

            String json = Files.readString(configPath);

            SkyMediaConfig config = GSON.fromJson(json, SkyMediaConfig.class);

            applyConfig(media, config);

            System.out.println(
                    "[AnimatedSkies] Reloaded config: "
                            + mediaName
            );

        } catch (Exception e) {

            System.err.println(
                    "[AnimatedSkies] Failed loading config: "
                            + configPath
            );

            e.printStackTrace();
        }
    }

    private static void applyConfig(SkyMedia media, SkyMediaConfig config) {

        /*
         * Active state
         */
        if (config.enabled) {
            SkyMediaManager.activate(media.getName());
        } else {
            SkyMediaManager.deactivate(media.getName());
        }

        /*
         * Time visibility
         */
        media.setTimeVisibility(config.timeVisibility != null ? config.timeVisibility : TimeVisibility.BOTH);

        media.designatedDimension = config.designatedDimension != null ? config.designatedDimension : DesignatedDimension.OVERWORLD;
        
        /*
         * Position
         */
        media.QuadXpos = config.quadXpos;

        media.QuadYpos = config.quadYpos;

        /*
         * Scale
         */
        media.QuadXscale = config.quadXscale;

        media.QuadYscale = config.quadYscale;

        media.QuadXrot = config.quadXrot;
        media.QuadYrot = config.quadYrot;
        media.QuadZrot = config.quadZrot;
    }

    public static void saveConfig(SkyMedia media, boolean enabled) {
        try {

            Files.createDirectories(CONFIG_DIR);

            Path configPath = CONFIG_DIR.resolve(media.getName() + ".json");

            SkyMediaConfig config = new SkyMediaConfig();

            config.enabled = enabled;

            config.timeVisibility = media.timeVisibility;
            config.designatedDimension = media.designatedDimension;

            config.quadXpos = media.QuadXpos;
            config.quadYpos = media.QuadYpos;

            config.quadXscale = media.QuadXscale;
            config.quadYscale = media.QuadYscale;

            config.quadXrot = media.QuadXrot;
            config.quadYrot = media.QuadYrot;
            config.quadZrot = media.QuadZrot;

            Files.writeString(
                    configPath,
                    GSON.toJson(config),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            loadConfig(configPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.animatedskies.client.utils;


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.animatedskies.client.enum_and_class.MediaType;
import com.animatedskies.client.enum_and_class.SkyMedia;
import com.animatedskies.client.enum_and_class.TimeVisibility;

import net.fabricmc.loader.api.FabricLoader;

public class SkyMediaProcessor {

    private static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();

    /**
     * Project root (one level above run/)
     */
    private static final Path PROJECT_ROOT = GAME_DIR.getParent();

    /**
     * Original assets (dev folder)
     */
    private static final Path ORIGINAL_DIR = PROJECT_ROOT.resolve("src/main/resources/assets/animatedskies/skyassets/original");

    /**
     * Runtime folders
     */
    private static final Path SKY_DIR = GAME_DIR.resolve("skyassets");

    private static final Path ADDITIONS_DIR = SKY_DIR.resolve("additions");

    private static final Path PROCESSED_DIR = SKY_DIR.resolve("processed");

    private static final Path CONFIG_DIR = SKY_DIR.resolve("configs");

    public static void processAll() {
        try {
            Files.createDirectories(SKY_DIR);
            Files.createDirectories(ADDITIONS_DIR);
            Files.createDirectories(PROCESSED_DIR);
            Files.createDirectories(CONFIG_DIR);

            System.out.println("[SkyMediaProcessor] Original dir = " + ORIGINAL_DIR);
            System.out.println("[SkyMediaProcessor] Additions dir = " + ADDITIONS_DIR);

            // Process built-in assets
            processDirectory(ORIGINAL_DIR);

            // Process additions
            processDirectory(ADDITIONS_DIR);

            System.out.println("[SkyMediaProcessor] Finished processing.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            System.err.println("[SkyMediaProcessor] Missing directory: " + dir);
            return;
        }

        Files.walk(dir)
                .filter(Files::isRegularFile)
                .forEach(SkyMediaProcessor::processFile);
    }

    private static void processFile(Path file) {
        String lower = file.getFileName().toString().toLowerCase();

        System.out.println("[SkyMediaProcessor] Processing: " + file);

        try {
            if (lower.endsWith(".png")
                    || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg")) {

                copyImage(file);

            } else if (lower.endsWith(".gif")) {

                convertGifToVerticalSpriteSheet(file);

            } else if (lower.endsWith(".mp4")) {

                convertMp4ToAtlas(file);
            }

        } catch (Exception e) {
            System.err.println("[SkyMediaProcessor] Failed: " + file);
            e.printStackTrace();
        }
    }

    private static void copyImage(Path file) throws IOException {

        BufferedImage image = ImageIO.read(file.toFile());

        if (image == null) {
                throw new IOException(
                        "Unsupported image format: " + file
                );
        }

        String relative =
                relativePath(file);

        // Force png output
        relative = relative.replaceAll(
                "\\.(png|jpg|jpeg)$",
                ".png"
        );

        Path out =
                PROCESSED_DIR.resolve(relative);

        Files.createDirectories(
                out.getParent()
        );

        // Write as actual PNG bytes
        ImageIO.write(image, "png", out.toFile());

        registerMedia(
                file,
                out,
                MediaType.IMAGE,
                image.getWidth(),
                image.getHeight(),
                1
        );
    }

    /**
     * GIF -> Vertical sprite sheet
     */
    private static void convertGifToVerticalSpriteSheet(Path gifFile)
            throws IOException {

        try (ImageInputStream stream =
                     ImageIO.createImageInputStream(gifFile.toFile())) {

            Iterator<ImageReader> readers =
                    ImageIO.getImageReadersByFormatName("gif");

            if (!readers.hasNext()) return;

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int frameCount = reader.getNumImages(true);

            List<BufferedImage> frames = new ArrayList<>();

            for (int i = 0; i < frameCount; i++) {
                frames.add(reader.read(i));
            }

            int width = frames.get(0).getWidth();
            int height = frames.get(0).getHeight();

            BufferedImage sheet =
                    new BufferedImage(
                            width,
                            height * frameCount,
                            BufferedImage.TYPE_INT_ARGB
                    );

            Graphics2D g = sheet.createGraphics();

            for (int i = 0; i < frameCount; i++) {
                g.drawImage(frames.get(i), 0, i * height, null);
            }

            g.dispose();
            reader.dispose();

            Path out = PROCESSED_DIR.resolve(
                    relativePath(gifFile)
                            .replace(".gif", ".png")
            );

            Files.createDirectories(out.getParent());

            ImageIO.write(sheet, "png", out.toFile());
            registerMedia(
                    gifFile,
                    out,
                    MediaType.GIF,
                    width,
                    height,
                    frameCount
                    );
        }
    }

    /**
     * MP4 -> atlas (requires ffmpeg in PATH)
     */
    private static void convertMp4ToAtlas(Path mp4File) throws IOException, InterruptedException {

        Path out = PROCESSED_DIR.resolve(
                relativePath(mp4File).replace(".mp4", ".png")
        );

        Files.createDirectories(out.getParent());

        /*
        * Get original dimensions
        */
        Dimension originalSize = getVideoDimensions(mp4File);

        String fileName = getSafeName(mp4File);
                
        /*
        * Detect greenscreen-tagged files
        */
        boolean isGreenScreen = fileName.contains("gn");
                
        /*
        * Dynamically scale while preserving aspect ratio
        * inside a 256x256 tile
        */
        float scale = Math.min(
                256f / originalSize.width,
                256f / originalSize.height
        );

        int scaledWidth = Math.round(originalSize.width * scale);

        int scaledHeight = Math.round(originalSize.height * scale);

        /*
        * FFmpeg filter chain
        */
        String videoFilter;

        if (isGreenScreen) {

            videoFilter =
            "fps=12," +
            "chromakey=0x00FF00:0.30:0.10," +
            "despill=green," +
            "scale=256:256:force_original_aspect_ratio=decrease," +
            "pad=256:256:(ow-iw)/2:(oh-ih)/2:color=black@0," +
            "format=rgba," +
            "tile=10x10";

            System.out.println(
                    "[SkyMediaProcessor] Greenscreen detected: "
                            + mp4File
            );

        } else {

            videoFilter =
                    "fps=12," +
                    "scale=256:256:force_original_aspect_ratio=decrease," +
                    "pad=256:256:(ow-iw)/2:(oh-ih)/2:color=black@0," +
                    "format=rgba," +
                    "tile=10x10";
        }

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",

                /*
                * Loop input infinitely
                */
                "-stream_loop", "-1",

                "-i", mp4File.toString(),

                "-vf", videoFilter,

                /*
                * Exactly 100 frames
                */
                "-frames:v", "100",

                "-an",
                out.toString()
        );

        pb.inheritIO();

        Process process = pb.start();
        process.waitFor();

        int frameCount = 100;

        /*
        * Register ACTUAL rendered frame size
        */
        registerMedia(
                mp4File,
                out,
                MediaType.VIDEO,
                scaledWidth,
                scaledHeight,
                frameCount
        );
    }

    private static String relativePath(Path file) {
        if (file.startsWith(ORIGINAL_DIR)) {
            return ORIGINAL_DIR.relativize(file).toString();
        }

        if (file.startsWith(ADDITIONS_DIR)) {
            return ADDITIONS_DIR.relativize(file).toString();
        }

        return file.getFileName().toString();
    }

    private static void registerMedia( Path originalFile, Path processedFile, MediaType mediaType, int frameWidth, int frameHeight, int frameCount) {

        String safeName = getSafeName(originalFile);

        SkyMedia media = new SkyMedia(
                safeName,
                mediaType,
                TimeVisibility.BOTH,
                processedFile.toString(),
                frameWidth,
                frameHeight,
                frameCount,
                2
        );

        SkyMediaManager.register(media);

        createMediaConfig(media, processedFile);

        System.out.println("[SkyMediaProcessor] Registered media: " + media.getName());
}

    private static Dimension getVideoDimensions(Path videoFile) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=s=x:p=0",
                videoFile.toString()
        );

        Process process = pb.start();

        String output = new String(
                process.getInputStream().readAllBytes()
        ).trim();

        process.waitFor();

        String[] split = output.split("x");

        int width = Integer.parseInt(split[0]);
        int height = Integer.parseInt(split[1]);

        return new Dimension(width, height);
        }

    private static void createMediaConfig(SkyMedia media, Path processedFile) {

        try {


                String jsonName = media.getName() + ".json";

                Path configPath = CONFIG_DIR.resolve(jsonName);

                /*
                * Don't overwrite existing configs
                */
                if (Files.exists(configPath)) {return;}

                Files.createDirectories(configPath.getParent());

                String json =
                        """
                        {
                            "enabled": false,
                            "timeVisibility": "%s",
                            "designatedDimension": "%s",
                            "quadXpos": %s,
                            "quadYpos": %s,
                            "quadXscale": %s,
                            "quadYscale": %s,
                            "quadXrot": %s,
                            "quadYrot": %s,
                            "quadZrot": %s
                        }
                        """.formatted(
                                media.timeVisibility,
                                media.designatedDimension,
                                media.QuadXpos,
                                media.QuadYpos,
                                media.QuadXscale,
                                media.QuadYscale,
                                media.QuadXrot,
                                media.QuadYrot,
                                media.QuadZrot
                        );

                Files.writeString(configPath, json);

                System.out.println("[SkyMediaProcessor] Created config: " + configPath);

        } catch (Exception e) {

                System.err.println("[SkyMediaProcessor] Failed creating config for " + media.getName());

                e.printStackTrace();
        }
        }


    private static String getSafeName(Path file) {

        String fileName = file.getFileName().toString();

        String rawName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

        return rawName.toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
    }
}
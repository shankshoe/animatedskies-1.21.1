package com.animatedskies.client.utils;

import com.animatedskies.client.enum_and_class.SkyMedia;

import java.util.*;

public class SkyMediaManager {

    private static final Map<String, SkyMedia> ALL_MEDIA = new LinkedHashMap<>();
    private static final List<SkyMedia> ACTIVE_MEDIA = new ArrayList<>();

    
    public static void register(SkyMedia media) {
        ALL_MEDIA.put(media.getName(), media);
    }

    
    public static SkyMedia get(String name) {
        return ALL_MEDIA.get(name);
    }

    public static void unregister(String name) {
        SkyMedia removed = ALL_MEDIA.remove(name);
        ACTIVE_MEDIA.remove(removed);
    }

    public static void clear() {
        ALL_MEDIA.clear();
        ACTIVE_MEDIA.clear();
    }

    public static Collection<SkyMedia> getAllMedia() {
        return Collections.unmodifiableCollection(ALL_MEDIA.values());
    }
    
    public static List<SkyMedia> getActiveMedia() {
        return Collections.unmodifiableList(ACTIVE_MEDIA);
    }
 
    public static boolean isActive(SkyMedia media) {
        return ACTIVE_MEDIA.contains(media);
    }

    public static boolean isActive(String name) {
        SkyMedia media = ALL_MEDIA.get(name);
        return media != null && ACTIVE_MEDIA.contains(media);
    }

    public static void activate(String name) {
        SkyMedia media = ALL_MEDIA.get(name);

        if (media != null && !ACTIVE_MEDIA.contains(media)) {
            ACTIVE_MEDIA.add(media);
            System.out.println("Activated: " + media.getName());
        }
    }

    public static void deactivate(String name) {
        SkyMedia media = ALL_MEDIA.get(name);
        if (media == null) {return;}

        System.out.println("Deactivated: " + media.getName());

        ACTIVE_MEDIA.remove(media);
    }

    public static void clearActive() {
        ACTIVE_MEDIA.clear();
    }


    public static void activateFirst() {

        if (ALL_MEDIA.isEmpty()) {
            return;
        }

        SkyMedia first =
                ALL_MEDIA.values()
                        .iterator()
                        .next();

        activate(first.getName());
    }
    
    /**
     * Debug info
     */
    public static void printStatus() {
        System.out.println("=== SkySpriteManager ===");

        System.out.println("Registered: "
                + ALL_MEDIA.size());

        System.out.println("Active: "
                + ACTIVE_MEDIA.size());

        for (SkyMedia media : ACTIVE_MEDIA) {
            System.out.println(" - "
                    + media.getName());
        }
    }
}
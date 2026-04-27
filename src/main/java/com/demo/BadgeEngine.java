package com.demo;

import java.util.ArrayList;
import java.util.List;

public class BadgeEngine {

    public enum BadgeType {
        EARLY_BIRD("Early Bird", "Started a session before 9 AM", "🌅"),
        ON_FIRE("On Fire", "3+ day streak", "🔥"),
        ZEN_MASTER("Zen Master", "Average focus rating 4.0+", "🧘"),
        NO_MERCY("No Mercy", "4+ sessions in one day", "⚡");

        public final String name;
        public final String description;
        public final String emoji;

        BadgeType(String name, String desc, String emoji) {
            this.name = name;
            this.description = desc;
            this.emoji = emoji;
        }
    }

    public static class Badge {
        public BadgeType type;
        public boolean unlocked;
        public String unlockedDate;  // null if locked

        public Badge(BadgeType type, boolean unlocked, String date) {
            this.type = type;
            this.unlocked = unlocked;
            this.unlockedDate = date;
        }
    }

    // ── Evaluate all badges based on DataStore ─────────────────────────────
    public static List<Badge> evaluateBadges() {
        List<Badge> badges = new ArrayList<>();
        DataStore ds = DataStore.get();

        // Early Bird — unlocked if any session started before 9 AM
        boolean earlyBird = ds.hasEarlyBirdSession();
        badges.add(new Badge(BadgeType.EARLY_BIRD, earlyBird, 
            earlyBird ? java.time.LocalDate.now().toString() : null));

        // On Fire — 3+ day streak
        boolean onFire = ds.isOnFire();
        badges.add(new Badge(BadgeType.ON_FIRE, onFire,
            onFire ? java.time.LocalDate.now().toString() : null));

        // Zen Master — avg focus rating >= 4.0
        boolean zenMaster = ds.isZenMaster();
        badges.add(new Badge(BadgeType.ZEN_MASTER, zenMaster,
            zenMaster ? java.time.LocalDate.now().toString() : null));

        // No Mercy — any day with 4+ sessions
        boolean noMercy = ds.isNoMercy();
        badges.add(new Badge(BadgeType.NO_MERCY, noMercy,
            noMercy ? java.time.LocalDate.now().toString() : null));

        return badges;
    }

    // ── Get a single badge ─────────────────────────────────────────────────
    public static Badge getBadge(BadgeType type) {
        return evaluateBadges().stream()
            .filter(b -> b.type == type)
            .findFirst()
            .orElse(new Badge(type, false, null));
    }

    // ── Count unlocked badges ──────────────────────────────────────────────
    public static int getUnlockedCount() {
        return (int) evaluateBadges().stream()
            .filter(b -> b.unlocked)
            .count();
    }
}
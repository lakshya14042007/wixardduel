package com.wizardduel.spell;

import java.util.*;

/**
 * Central registry for all available spells.
 * Call {@link #get(String)} to obtain a fresh Spell instance by name (case-insensitive).
 */
public final class SpellRegistry {

    // Ordered list used for the help menu
    private static final List<Spell> TEMPLATES = List.of(
            new AttackSpell(),
            new FireballSpell(),
            new ShieldSpell(),
            new ReflectSpell(),
            new HealSpell()
    );

    private static final Map<String, Spell> BY_NAME;

    static {
        Map<String, Spell> m = new LinkedHashMap<>();
        for (Spell s : TEMPLATES) {
            m.put(s.getName().toLowerCase(), s);
        }
        BY_NAME = Collections.unmodifiableMap(m);
    }

    private SpellRegistry() {}

    /**
     * @param name case-insensitive spell name
     * @return a Spell instance, or {@code null} if not found
     */
    public static Spell get(String name) {
        if (name == null) return null;
        // Return a fresh instance to avoid shared mutable state
        String key = name.trim().toLowerCase();
        return switch (key) {
            case "attack"   -> new AttackSpell();
            case "fireball" -> new FireballSpell();
            case "shield"   -> new ShieldSpell();
            case "reflect"  -> new ReflectSpell();
            case "heal"     -> new HealSpell();
            default         -> null;
        };
    }

    /** @return all spell descriptions in display order */
    public static List<Spell> all() {
        return TEMPLATES;
    }
}

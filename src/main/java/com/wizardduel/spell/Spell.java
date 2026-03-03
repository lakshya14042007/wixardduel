package com.wizardduel.spell;

import com.wizardduel.model.Player;

/**
 * Base class for all spells in Wizard Duel.
 *
 * <p>Each spell has a name, a base cooldown (in turns), and a SpellEffect that
 * describes any defensive modifier it grants to its caster. The {@link #apply}
 * method performs the spell's primary action (damage, healing, etc.) BEFORE
 * defensive interactions are evaluated by {@link com.wizardduel.game.GameSession}.
 */
public abstract class Spell {

    protected final String name;
    protected final int baseCooldown;
    protected final String emoji;
    protected final SpellEffect effect;

    protected Spell(String name, String emoji, int baseCooldown, SpellEffect effect) {
        this.name = name;
        this.emoji = emoji;
        this.baseCooldown = baseCooldown;
        this.effect = effect;
    }

    /** @return display name */
    public String getName() { return name; }

    /** @return emoji for chat messages */
    public String getEmoji() { return emoji; }

    /** @return number of turns the caster must wait before using this spell again */
    public int getBaseCooldown() { return baseCooldown; }

    /** @return the defensive modifier this spell grants to its caster */
    public SpellEffect getEffect() { return effect; }

    /**
     * How much raw outgoing damage this spell deals (0 for non-attack spells).
     * Used by the battle engine to calculate Shield / Reflect interactions.
     */
    public int getRawDamage() { return 0; }

    /**
     * Apply the spell's primary action.
     *
     * @param caster  the player casting the spell
     * @param target  the opposing player
     */
    public abstract void apply(Player caster, Player target);

    /** One-line description shown in the help menu. */
    public abstract String getDescription();
}

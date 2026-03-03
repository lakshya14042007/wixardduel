package com.wizardduel.spell;

/**
 * Describes any special modifier a spell applies to its caster for the current round.
 */
public enum SpellEffect {
    NONE,
    SHIELD,    // Reduces incoming damage by 50%
    REFLECT    // Reflects 50% of incoming damage back to attacker
}

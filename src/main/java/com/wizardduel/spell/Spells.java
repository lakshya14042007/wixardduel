package com.wizardduel.spell;

import com.wizardduel.model.Player;

// ---------------------------------------------------------------------------
// Attack – 20 damage, no cooldown
// ---------------------------------------------------------------------------
class AttackSpell extends Spell {
    public AttackSpell() { super("Attack", "⚡", 0, SpellEffect.NONE); }

    @Override public int getRawDamage() { return 20; }

    @Override
    public void apply(Player caster, Player target) {
        // Damage resolution is handled by GameSession (needs to check target's effect first).
    }

    @Override public String getDescription() { return "⚡ Attack – deals 20 damage (no cooldown)"; }
}

// ---------------------------------------------------------------------------
// Fireball – 30 damage, 2-turn cooldown
// ---------------------------------------------------------------------------
class FireballSpell extends Spell {
    public FireballSpell() { super("Fireball", "🔥", 2, SpellEffect.NONE); }

    @Override public int getRawDamage() { return 30; }

    @Override
    public void apply(Player caster, Player target) {
        // Damage resolution is handled by GameSession.
    }

    @Override public String getDescription() { return "🔥 Fireball – deals 30 damage (2-turn cooldown)"; }
}

// ---------------------------------------------------------------------------
// Shield – absorbs 50 % of incoming damage this round, 1-turn cooldown
// ---------------------------------------------------------------------------
class ShieldSpell extends Spell {
    public ShieldSpell() { super("Shield", "🛡️", 1, SpellEffect.SHIELD); }

    @Override
    public void apply(Player caster, Player target) {
        // Shield is a purely defensive spell; no direct action on target.
    }

    @Override public String getDescription() { return "🛡️ Shield – blocks 50 % of incoming damage (1-turn cooldown)"; }
}

// ---------------------------------------------------------------------------
// Reflect – reflects 50 % of incoming damage back at the attacker, 1-turn cooldown
// ---------------------------------------------------------------------------
class ReflectSpell extends Spell {
    public ReflectSpell() { super("Reflect", "🔮", 1, SpellEffect.REFLECT); }

    @Override
    public void apply(Player caster, Player target) {
        // Reflect is handled by GameSession (attacker receives reflected damage).
    }

    @Override public String getDescription() { return "🔮 Reflect – reflects 50 % of damage back at attacker (1-turn cooldown)"; }
}

// ---------------------------------------------------------------------------
// Heal – restores 20 HP to caster, no cooldown
// ---------------------------------------------------------------------------
class HealSpell extends Spell {
    public HealSpell() { super("Heal", "💚", 0, SpellEffect.NONE); }

    @Override
    public void apply(Player caster, Player target) {
        caster.heal(20);
    }

    @Override public String getDescription() { return "💚 Heal – restore 20 HP (no cooldown)"; }
}

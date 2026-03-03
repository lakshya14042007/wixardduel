package com.wizardduel.game;

/**
 * Captures the outcome of one resolved round, ready to be formatted into a chat message.
 */
public class TurnResult {

    public final int turn;

    public final String p1Name;
    public final String p1SpellName;
    public final String p1SpellEmoji;
    public final int p1HpBefore;
    public final int p1HpAfter;
    public final int p1DamageDealt;
    public final int p1DamageTaken;
    public final int p1Healed;
    public final int p1ReflectedDamage;   // damage reflected BACK at p1 by p2's Reflect

    public final String p2Name;
    public final String p2SpellName;
    public final String p2SpellEmoji;
    public final int p2HpBefore;
    public final int p2HpAfter;
    public final int p2DamageDealt;
    public final int p2DamageTaken;
    public final int p2Healed;
    public final int p2ReflectedDamage;   // damage reflected BACK at p2 by p1's Reflect

    /** null = game still going; non-null = winner's name (or "draw") */
    public final String winner;

    // p1 cooldown after this turn (0 = ready)
    public final int p1FireballCooldown;
    public final int p1ShieldCooldown;
    public final int p1ReflectCooldown;
    public final int p2FireballCooldown;
    public final int p2ShieldCooldown;
    public final int p2ReflectCooldown;

    private TurnResult(Builder b) {
        this.turn = b.turn;
        this.p1Name = b.p1Name; this.p1SpellName = b.p1SpellName; this.p1SpellEmoji = b.p1SpellEmoji;
        this.p1HpBefore = b.p1HpBefore; this.p1HpAfter = b.p1HpAfter;
        this.p1DamageDealt = b.p1DamageDealt; this.p1DamageTaken = b.p1DamageTaken;
        this.p1Healed = b.p1Healed; this.p1ReflectedDamage = b.p1ReflectedDamage;
        this.p2Name = b.p2Name; this.p2SpellName = b.p2SpellName; this.p2SpellEmoji = b.p2SpellEmoji;
        this.p2HpBefore = b.p2HpBefore; this.p2HpAfter = b.p2HpAfter;
        this.p2DamageDealt = b.p2DamageDealt; this.p2DamageTaken = b.p2DamageTaken;
        this.p2Healed = b.p2Healed; this.p2ReflectedDamage = b.p2ReflectedDamage;
        this.winner = b.winner;
        this.p1FireballCooldown = b.p1FireballCooldown; this.p1ShieldCooldown = b.p1ShieldCooldown;
        this.p1ReflectCooldown = b.p1ReflectCooldown;
        this.p2FireballCooldown = b.p2FireballCooldown; this.p2ShieldCooldown = b.p2ShieldCooldown;
        this.p2ReflectCooldown = b.p2ReflectCooldown;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        int turn;
        String p1Name, p1SpellName, p1SpellEmoji;
        int p1HpBefore, p1HpAfter, p1DamageDealt, p1DamageTaken, p1Healed, p1ReflectedDamage;
        String p2Name, p2SpellName, p2SpellEmoji;
        int p2HpBefore, p2HpAfter, p2DamageDealt, p2DamageTaken, p2Healed, p2ReflectedDamage;
        String winner;
        int p1FireballCooldown, p1ShieldCooldown, p1ReflectCooldown;
        int p2FireballCooldown, p2ShieldCooldown, p2ReflectCooldown;

        public Builder turn(int v)              { this.turn = v; return this; }
        public Builder p1Name(String v)         { this.p1Name = v; return this; }
        public Builder p1SpellName(String v)    { this.p1SpellName = v; return this; }
        public Builder p1SpellEmoji(String v)   { this.p1SpellEmoji = v; return this; }
        public Builder p1HpBefore(int v)        { this.p1HpBefore = v; return this; }
        public Builder p1HpAfter(int v)         { this.p1HpAfter = v; return this; }
        public Builder p1DamageDealt(int v)     { this.p1DamageDealt = v; return this; }
        public Builder p1DamageTaken(int v)     { this.p1DamageTaken = v; return this; }
        public Builder p1Healed(int v)          { this.p1Healed = v; return this; }
        public Builder p1ReflectedDamage(int v) { this.p1ReflectedDamage = v; return this; }
        public Builder p2Name(String v)         { this.p2Name = v; return this; }
        public Builder p2SpellName(String v)    { this.p2SpellName = v; return this; }
        public Builder p2SpellEmoji(String v)   { this.p2SpellEmoji = v; return this; }
        public Builder p2HpBefore(int v)        { this.p2HpBefore = v; return this; }
        public Builder p2HpAfter(int v)         { this.p2HpAfter = v; return this; }
        public Builder p2DamageDealt(int v)     { this.p2DamageDealt = v; return this; }
        public Builder p2DamageTaken(int v)     { this.p2DamageTaken = v; return this; }
        public Builder p2Healed(int v)          { this.p2Healed = v; return this; }
        public Builder p2ReflectedDamage(int v) { this.p2ReflectedDamage = v; return this; }
        public Builder winner(String v)         { this.winner = v; return this; }
        public Builder p1FireballCooldown(int v){ this.p1FireballCooldown = v; return this; }
        public Builder p1ShieldCooldown(int v)  { this.p1ShieldCooldown = v; return this; }
        public Builder p1ReflectCooldown(int v) { this.p1ReflectCooldown = v; return this; }
        public Builder p2FireballCooldown(int v){ this.p2FireballCooldown = v; return this; }
        public Builder p2ShieldCooldown(int v)  { this.p2ShieldCooldown = v; return this; }
        public Builder p2ReflectCooldown(int v) { this.p2ReflectCooldown = v; return this; }

        public TurnResult build() { return new TurnResult(this); }
    }
}

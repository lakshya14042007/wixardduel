package com.wizardduel.model;

public class Player {

    private static final int MAX_HP = 100;

    private final String name;
    private final long userId;
    private int hp;

    public Player(String name, long userId) {
        this.name = name;
        this.userId = userId;
        this.hp = MAX_HP;
    }

    public String getName() { return name; }
    public long getUserId() { return userId; }
    public int getHp() { return hp; }
    public int getMaxHp() { return MAX_HP; }

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
    }

    public void heal(int amount) {
        hp = Math.min(MAX_HP, hp + amount);
    }

    public boolean isAlive() { return hp > 0; }

    public String getHpBar() {
        int filled = (int) Math.round((hp / (double) MAX_HP) * 10);
        return "█".repeat(filled) + "░".repeat(10 - filled) + " " + hp + "/" + MAX_HP;
    }
}

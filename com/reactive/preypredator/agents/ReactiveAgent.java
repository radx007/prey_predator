package com.reactive.preypredator.agents;

import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Position;
import com.reactive.preypredator.environment.ReactiveEnvironment;

/**
 * Abstract base class for all reactive agents
 */
public abstract class ReactiveAgent {
    protected String id;
    protected Position position;
    protected int energy;
    protected Gender gender;
    protected int reproductionCooldown;
    protected int ticksWithoutFood;
    protected boolean alive;

    public ReactiveAgent(String id, Position position, int energy) {
        this.id = id;
        this.position = position;
        this.energy = energy;
        this.gender = Gender.random();
        this.reproductionCooldown = 0;
        this.ticksWithoutFood = 0;
        this.alive = true;
    }

    /**
     * REACTIVE BEHAVIOR: Perceive environment and react immediately
     */
    public abstract void react(ReactiveEnvironment env);

    /**
     * Consume energy from movement or actions
     */
    protected void consumeEnergy(double amount) {
        energy -= (int) amount;
        if (energy < 0) energy = 0;
    }

    /**
     * Gain energy from food
     */
    protected void gainEnergy(int amount, int maxEnergy) {
        energy = Math.min(energy + amount, maxEnergy);
    }

    /**
     * Check if agent can reproduce - PUBLIC for environment access
     */
    public boolean canReproduce(int minEnergy) {
        return energy >= minEnergy && reproductionCooldown == 0 && alive;
    }

    /**
     * Decrement reproduction cooldown timer
     */
    protected void decrementCooldown() {
        if (reproductionCooldown > 0) reproductionCooldown--;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public int getEnergy() {
        return energy;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isAlive() {
        return alive;
    }

    public void die() {
        alive = false;
    }

    // Setters for reproduction
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void setReproductionCooldown(int cooldown) {
        this.reproductionCooldown = cooldown;
    }
}
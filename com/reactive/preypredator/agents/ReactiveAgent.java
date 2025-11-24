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
        if (energy <= 0) {
            alive = false;
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, energy);
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getReproductionCooldown() {
        return reproductionCooldown;
    }

    public void setReproductionCooldown(int cooldown) {
        this.reproductionCooldown = cooldown;
    }

    public void decrementReproductionCooldown() {
        if (reproductionCooldown > 0) {
            reproductionCooldown--;
        }
    }

    public int getTicksWithoutFood() {
        return ticksWithoutFood;
    }

    public void incrementTicksWithoutFood() {
        ticksWithoutFood++;
    }

    public void resetTicksWithoutFood() {
        ticksWithoutFood = 0;
    }
}

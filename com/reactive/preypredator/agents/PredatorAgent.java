package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Position;
import jade.core.Agent;

/**
 * Predator agent with eating cooldown to prevent rapid kills
 */
public class PredatorAgent extends Agent {
    private ReactiveEnvironment environment;
    private Position position;
    private int energy;
    private Gender gender;
    private int reproductionCooldown;
    private int ticksWithoutFood;
    private int ticksSinceLastMeal;
    private int eatingCooldown;  // NEW: prevent kill spam
    private boolean alive;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            this.environment = (ReactiveEnvironment) args[0];
            this.position = (Position) args[1];
            this.gender = (Gender) args[2];
            String name = (String) args[3];

            this.energy = Config.PREDATOR_ENERGY_START;
            this.reproductionCooldown = 0;
            this.ticksWithoutFood = 0;
            this.ticksSinceLastMeal = Integer.MAX_VALUE;
            this.eatingCooldown = 0;  // NEW
            this.alive = true;

            environment.registerPredatorAgent(this);
            addBehaviour(new PredatorBehavior(this, environment));
        }
    }

    // Getters and setters
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
        this.energy = Math.min(energy, Config.PREDATOR_ENERGY_MAX);
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

    public void setTicksWithoutFood(int ticks) {
        this.ticksWithoutFood = ticks;
    }

    public void incrementTicksWithoutFood() {
        ticksWithoutFood++;
    }

    public void resetTicksWithoutFood() {
        ticksWithoutFood = 0;
    }

    public int getTicksSinceLastMeal() {
        return ticksSinceLastMeal;
    }

    public void setTicksSinceLastMeal(int ticks) {
        this.ticksSinceLastMeal = ticks;
    }

    // NEW: Eating cooldown methods
    public int getEatingCooldown() {
        return eatingCooldown;
    }

    public void setEatingCooldown(int cooldown) {
        this.eatingCooldown = cooldown;
    }

    public void decrementEatingCooldown() {
        if (eatingCooldown > 0) {
            eatingCooldown--;
        }
    }

    public void consumeEnergy(double amount) {
        energy -= (int) amount;
        if (energy <= 0) {
            alive = false;
        }
    }
}

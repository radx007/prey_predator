package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Position;
import jade.core.Agent;

/**
 * Prey agent in the reactive system
 */
public class PreyAgent extends Agent {
    private ReactiveEnvironment environment;
    private Position position;
    private int energy;
    private Gender gender;
    private int reproductionCooldown;
    private int ticksWithoutFood;
    private boolean alive;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            this.environment = (ReactiveEnvironment) args[0];
            this.position = (Position) args[1];
            this.gender = (Gender) args[2];
            String name = (String) args[3];
        }

        this.energy = Config.PREY_ENERGY_START;
        this.reproductionCooldown = 0;
        this.ticksWithoutFood = 0;
        this.alive = true;

        // Register with environment
        environment.registerPreyAgent(getLocalName(), this);

        // Add cyclic behavior
        addBehaviour(new PreyBehavior(this, environment));
    }

    @Override
    protected void takeDown() {
        alive = false;
    }

    // Getters and setters
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public void setEnvironment(ReactiveEnvironment env) { this.environment = env; }
    public void setGender(Gender g) { this.gender = g; }
    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = Math.min(energy, Config.PREY_ENERGY_MAX); }
    public Gender getGender() { return gender; }
    public int getReproductionCooldown() { return reproductionCooldown; }
    public void setReproductionCooldown(int cooldown) { this.reproductionCooldown = cooldown; }
    public int getTicksWithoutFood() { return ticksWithoutFood; }
    public void setTicksWithoutFood(int ticks) { this.ticksWithoutFood = ticks; }
    public boolean isAlive() { return alive; }

    public void consumeEnergy(int amount) {
        energy = Math.max(0, energy - amount);
    }

    public void gainEnergy(int amount) {
        energy = Math.min(Config.PREY_ENERGY_MAX, energy + amount);
        ticksWithoutFood = 0;
    }

    public boolean canReproduce() {
        return energy >= Config.PREY_REPRODUCTION_THRESHOLD && reproductionCooldown == 0;
    }

    public boolean isDead() {
        return energy <= 0 || ticksWithoutFood >= Config.PREY_STARVATION_LIMIT;
    }
}
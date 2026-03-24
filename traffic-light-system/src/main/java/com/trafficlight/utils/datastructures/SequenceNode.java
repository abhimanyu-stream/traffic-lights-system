package com.trafficlight.utils.datastructures;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;

import java.util.Objects;

/**
 * Custom Node class for implementing light sequence linked list.
 * Represents a single step in a traffic light sequence.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class SequenceNode {
    
    private final Direction direction;
    private final LightState state;
    private final int durationSeconds;
    private final int order;
    private volatile SequenceNode next;
    private volatile SequenceNode previous;
    
    public SequenceNode(Direction direction, LightState state, int durationSeconds, int order) {
        this.direction = Objects.requireNonNull(direction, "Direction cannot be null");
        this.state = Objects.requireNonNull(state, "State cannot be null");
        this.durationSeconds = validateDuration(durationSeconds);
        this.order = order;
    }
    
    private int validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive, got: " + duration);
        }
        if (duration > 300) {
            throw new IllegalArgumentException("Duration cannot exceed 300 seconds, got: " + duration);
        }
        return duration;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public LightState getState() {
        return state;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    public int getOrder() {
        return order;
    }
    
    public SequenceNode getNext() {
        return next;
    }
    
    public void setNext(SequenceNode next) {
        this.next = next;
    }
    
    public SequenceNode getPrevious() {
        return previous;
    }
    
    public void setPrevious(SequenceNode previous) {
        this.previous = previous;
    }
    
    public boolean hasNext() {
        return next != null;
    }
    
    public boolean hasPrevious() {
        return previous != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceNode that = (SequenceNode) o;
        return durationSeconds == that.durationSeconds &&
               order == that.order &&
               direction == that.direction &&
               state == that.state;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(direction, state, durationSeconds, order);
    }
    
    @Override
    public String toString() {
        return String.format("SequenceNode{order=%d, direction=%s, state=%s, duration=%ds}", 
            order, direction, state, durationSeconds);
    }
}
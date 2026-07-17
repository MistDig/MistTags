package com.mistdig.misttags;

import java.util.List;

public class AnimationData {

    private final List<String> frames;
    private final int updateTicks;
    private int tickCounter = 0;
    private int currentFrameIndex = 0;

    public AnimationData(List<String> frames, int updateTicks) {
        this.frames = frames;
        this.updateTicks = updateTicks;
    }

    /**
     * Advances the animation clock by one server tick.
     * @return true if the visible frame changed on this call (used so the display
     *         manager only bothers refreshing scoreboard teams when something actually moved).
     */
    public boolean tick() {
        if (frames.size() <= 1) return false;
        tickCounter++;
        if (tickCounter >= updateTicks) {
            tickCounter = 0;
            currentFrameIndex = (currentFrameIndex + 1) % frames.size();
            return true;
        }
        return false;
    }

    public String getCurrentFrame() {
        if (frames.isEmpty()) return "";
        return frames.get(currentFrameIndex);
    }

    public int getFrameCount() {
        return frames.size();
    }
}

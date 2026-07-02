package com.ryane.vehiclealternative;

import java.util.UUID;

public final class VehicleConstants {

    private VehicleConstants() {}

    public static final UUID SPEED_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("vehiclealternative.speed".getBytes());
    public static final UUID JUMP_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("vehiclealternative.jump".getBytes());

    public static final String SPEED_MODIFIER_NAME = "vehiclealternative_speed";
    public static final String JUMP_MODIFIER_NAME  = "vehiclealternative_jump";

    /**
     * Approximate vanilla boat top-speed on water (blocks/tick).
     * Used as the reference baseline when scaling boat velocity.
     */
    public static final double VANILLA_BOAT_WATER_SPEED = 0.35;
}

package com.ryane.vehiclealternative;

import java.util.UUID;

public final class VehicleConstants {

    private VehicleConstants() {}

    /** Applied on mount — scales entity movement speed by configured multiplier. */
    public static final UUID SPEED_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("vehiclealternative.speed".getBytes());

    /** Applied on mount — scales horse jump strength. */
    public static final UUID JUMP_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("vehiclealternative.jump".getBytes());

    /**
     * Applied by the block-speed-boost system when an entity is standing on
     * a listed surface block. Separate from SPEED_MODIFIER so the two
     * multipliers stack independently and can be removed independently.
     */
    public static final UUID BLOCK_SPEED_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("vehiclealternative.blockspeed".getBytes());

    public static final String SPEED_MODIFIER_NAME       = "vehiclealternative_speed";
    public static final String JUMP_MODIFIER_NAME        = "vehiclealternative_jump";
    public static final String BLOCK_SPEED_MODIFIER_NAME = "vehiclealternative_blockspeed";

    /**
     * Approximate vanilla boat top-speed on water (blocks/tick).
     * Used as the reference baseline when scaling boat velocity.
     */
    public static final double VANILLA_BOAT_WATER_SPEED = 0.35;
}

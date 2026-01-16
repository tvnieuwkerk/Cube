package nl.tvn.cube.model;

import java.util.Objects;

public final class Move {
    public enum Kind {
        FACE,
        SLICE,
        WIDE,
        ROTATION
    }

    private final Kind kind;
    private final Face face;
    private final Axis axis;
    private final TurnDirection direction;
    private final int turns;

    private Move(Kind kind, Face face, Axis axis, TurnDirection direction, int turns) {
        this.kind = kind;
        this.face = face;
        this.axis = axis;
        this.direction = direction;
        this.turns = turns;
    }

    public static Move face(Face face, TurnDirection direction, int turns) {
        return new Move(Kind.FACE, face, null, direction, turns);
    }

    public static Move slice(Axis axis, TurnDirection direction, int turns) {
        return new Move(Kind.SLICE, null, axis, direction, turns);
    }

    public static Move wide(Face face, TurnDirection direction, int turns) {
        return new Move(Kind.WIDE, face, null, direction, turns);
    }

    public static Move rotation(Axis axis, TurnDirection direction, int turns) {
        return new Move(Kind.ROTATION, null, axis, direction, turns);
    }

    public Kind kind() {
        return kind;
    }

    public Face face() {
        return face;
    }

    public Axis axis() {
        return axis;
    }

    public TurnDirection direction() {
        return direction;
    }

    public int turns() {
        return turns;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move move)) {
            return false;
        }
        return turns == move.turns
            && kind == move.kind
            && face == move.face
            && axis == move.axis
            && direction == move.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, face, axis, direction, turns);
    }
}

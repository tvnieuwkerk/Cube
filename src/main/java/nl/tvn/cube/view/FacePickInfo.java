package nl.tvn.cube.view;

import nl.tvn.cube.model.RotationAxis;

public record FacePickInfo(RotationAxis axis, int layer, int x, int y, int z) {
}

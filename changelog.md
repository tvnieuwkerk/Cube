# Changelog

## 2026-01-17 13:03:06 +0100 - Scale cube with window size
- Summary of change request: Make the cube grow or shrink when the app window is resized.
- Summary of change request implementation: Bound the 3D subscene to the container size and updated the cube group's scale based on the current scene dimensions.

## 2026-01-17 13:08:26 +0100 - Keep cube fully visible on resize
- Summary of change request: Ensure the cube remains fully visible when resizing, and it shrinks again as the window gets smaller.
- Summary of change request implementation: Recomputed cube scaling from the container size on each resize and clamped it to the camera frustum so the cube never exceeds the view.

## 2026-01-17 13:12:35 +0100 - Clamp cube scale and preserve help visibility
- Summary of change request: Ensure the cube starts at the correct size, shrinks with the window, and keep the help section visible.
- Summary of change request implementation: Bound cube scaling directly to subscene size changes, allowed the 3D container to shrink, and fixed the help area to its preferred height.

## 2026-01-17 13:17:15 +0100 - Fix cube scale calculation compile error
- Summary of change request: Fix compile errors in MainView.
- Summary of change request implementation: Used the subscene width/height for aspect ratio calculation and guarded against zero dimensions.

## 2026-01-17 13:20:24 +0100 - Reduce initial cube size
- Summary of change request: Make the initial cube 25% smaller.
- Summary of change request implementation: Lowered the target scale ratio so the cube renders at 75% of the previous size and still scales with the window.

## 2026-01-17 13:32:02 +0100 - Slow cube growth for larger windows
- Summary of change request: Make cube growth less aggressive so it remains within the visible area on resize.
- Summary of change request implementation: Based scaling on the cube's diagonal to keep it within view even as it grows and rotates.

## 2026-01-17 13:38:58 +0100 - Align front-left edge with vertical axis
- Summary of change request: Align the edge between the front and left faces with the window's vertical axis.
- Summary of change request implementation: Flipped the camera's yaw so the view centers the front-left edge along the vertical axis.

## 2026-01-17 13:47:51 +0100 - Add world axes
- Summary of change request: Draw x, y, and z axes and extend them beyond the cube by half a cube size.
- Summary of change request implementation: Added thin colored axis boxes centered at the origin and extending one cube edge length so they protrude past the cube.

## 2026-01-17 13:54:51 +0100 - Animate turns
- Summary of change request: Animate cube turns over 0.25 seconds.
- Summary of change request implementation: Grouped affected cubies into a temporary slice, rotated it with a timeline, then applied the final model and view transforms.

## 2026-01-17 13:58:37 +0100 - Add module descriptor
- Summary of change request: Add a Java module file to the project.
- Summary of change request implementation: Created `module-info.java` with JavaFX requirements and exported the app packages.

## 2026-01-17 14:31:25 +0100 - Add camera yaw controls
- Summary of change request: Allow rotating the camera around the Y axis with the left and right arrow keys and update the help text.
- Summary of change request implementation: Added a yaw rotate transform that responds to arrow key input and documented the new controls in the help section.

## 2026-01-17 14:38:48 +0100 - Add camera pitch and roll controls
- Summary of change request: Add camera rotation around the X and Z axes using the arrow and page keys.
- Summary of change request implementation: Added pitch and roll transforms with key bindings and updated the help text to document the controls.

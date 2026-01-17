# Changelog

## 2026-01-17 18:47:03 +0000 - Improve help window visibility
- Summary of change request: Analyze the HelpWindow UI and ensure the window always shows all of its content.
- Summary of change request implementation: Added a scrollable container for the help content and clamped the window size to the available screen bounds.

## 2026-01-17 16:44:05 +0000 - Refine help window layout and labels
- Summary of change request: Clarify inverse move keys in labels, switch to three turn cards per row, shrink previews slightly, and auto-size the help window to its content.
- Summary of change request implementation: Updated turn labels to mention Shift+Key inverse input, adjusted the grid to three columns, reduced preview sizing, and let the help window size to its scene on show.

## 2026-01-17 16:22:40 +0000 - Expand help window content and labels
- Summary of change request: Enlarge help previews, add M/E/S turns, extend labels with keyboard keys, and show camera key hints while keeping all content visible.
- Summary of change request implementation: Increased help preview sizing and window dimensions, added slice moves and keyboard labels to the turn grid, and appended camera control hints below the turns.

## 2026-01-17 15:56:05 +0000 - Add help window with animated turn previews
- Summary of change request: Add a help button in the top row that opens a help window to the right of the main cube window showing all turn animations in a two-column layout.
- Summary of change request implementation: Added a dedicated help window with mini cube previews that loop each face move and its inverse, wired a top-row help button to open and position the window beside the main stage, and removed the inline help section.

## 2026-01-17 15:40:09 +0000 - Focus root on startup and cube clicks
- Summary of change request: Ensure the cube area/root gets focus on app start and when clicking the cube view so keyboard controls remain active.
- Summary of change request implementation: Requested focus on the root after showing the stage and added mouse-click focus requests on the cube subscene/container.

## 2026-01-17 15:26:32 +0000 - Restore keyboard focus after running algorithms
- Summary of change request: Ensure cube keyboard controls work after running an algorithm from the text field by restoring focus.
- Summary of change request implementation: Made the root focusable and return focus to it after successfully running a parsed algorithm.

## 2026-01-17 14:52:19 +0000 - Add algorithm input parsing and execution
- Summary of change request: Provide a text input for algorithm strings, validate allowed notation, show errors, and execute parsed moves on the cube.
- Summary of change request implementation: Added parser/parse result utilities, wired a new algorithm input with error messaging to execute parsed move sequences, exposed a public move-sequence entry point, and added unit tests for parsing behavior.

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

## 2026-01-17 14:45:12 +0100 - Add reset/randomize controls
- Summary of change request: Add Reset/Randomize buttons, restore the cube, and scramble with fast animations.
- Summary of change request implementation: Added top controls wired to new reset and randomize actions, reset model/view state, and played a rapid random move sequence.

## 2026-01-17 14:49:39 +0100 - Restore original camera key mapping
- Summary of change request: Revert the camera rotation keys to the original left/right arrow mapping.
- Summary of change request implementation: Removed pitch/roll key handlers and updated the help text to only mention left/right camera rotation.

## 2026-01-17 15:04:19 +0100 - Disable button focus
- Summary of change request: Prevent the Reset and Randomize buttons from stealing focus.
- Summary of change request implementation: Disabled focus traversal on both buttons so arrow key handling stays on the scene.

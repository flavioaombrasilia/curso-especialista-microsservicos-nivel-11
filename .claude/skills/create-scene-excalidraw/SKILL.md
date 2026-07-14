---
name: create-scene-excalidraw
description: >-
  Create or edit Excalidraw+ diagrams for the AlgaWorks course instructor.
  Invoke whenever the user asks to create/edit Excalidraw scenes, diagrams, or
  course lesson boards. Encodes the instructor's hard-won conventions: dark-mode
  color inversion, hand-drawn-but-straight style, restrained semantic palette,
  one-scene-per-module layout, and the MCP tool workflow.
---

# Creating & Editing Excalidraw Scenes for the AlgaWorks Course

This skill codifies how the AlgaWorks course instructor wants Excalidraw+ boards
built. These are **free-form compositions** (stacked lesson diagrams on one
continuous canvas), NOT node/edge flowcharts and NOT slide decks. Follow every
rule below — each one comes from explicit, repeated instructor feedback.

## Workspace & scene lookup

The instructor works in the `highworks` workspace and the `AlgaWorks` collection.
Do **not** hard-code any workspace/collection/scene IDs — resolve them at runtime:

- `get_workspace` / `list_collections` / `list_collection_scenes` to find the
  right workspace, collection, and scene.
- Scene URL format: `https://app.excalidraw.com/s/<workspaceId>/<sceneId>`.

Do not invent IDs, scene names, or collection data. If a request depends on
existing scene state, `get_scene` / `list_scenes` first.

---

## 1. Tooling / MCP workflow

Use the real Excalidraw+ MCP server tools (`mcp__excalidraw__*`):

- `create_scene`, `edit_scene_content`, `get_scene`, `get_scene_content`,
  `search_scene_content`, `take_screenshot`, `read_freeform_format`.

**MANDATORY first step:** Before the FIRST scene write in a session you MUST call
`read_freeform_format` (the free-form guide). These boards are free-form
compositions — do NOT use `create_diagram` (node/edge) or presentation tools.

### `edit_scene_content` — one call does delete → update → add (in that order)

- **`add`** = a JSON **array string** of new element skeletons.
  - NO `id`.
  - Use `tempId` for same-request references (`frameId`, `startBinding.elementId`,
    `endBinding.elementId`) so newly-added elements can reference each other.
- **`update`** = a JSON **array string** of patches. Each patch needs a real,
  persisted `id`.
  - To change a label, send `label` as an **OBJECT** `{"text":"..."}`, NEVER a
    bare string.
- **`delete`** = an array of real persisted ids.

### Arrow bindings (critical — geometry alone does NOT persist)

Any arrow that points at a shape MUST carry explicit `startBinding` /
`endBinding` with `fixedPoint` and `mode:"inside"`. Without these the binding is
dropped on save even if the arrow visually touches the shape.

Common `fixedPoint` anchors:

| Side | fixedPoint |
|---|---|
| top | `[0.5, 0]` |
| right | `[1, 0.5]` |
| bottom | `[0.5, 1]` |
| left | `[0, 0.5]` |

```json
{
  "type": "arrow",
  "tempId": "arr1",
  "startBinding": { "elementId": "boxA", "fixedPoint": [0.5, 1], "mode": "inside" },
  "endBinding":   { "elementId": "boxB", "fixedPoint": [0.5, 0], "mode": "inside" }
}
```

---

## 2. THE MOST IMPORTANT RULE — dark-mode color inversion

The instructor **views the board in Excalidraw dark mode**, which applies an
invert filter: **lightness flips (light ↔ dark) but hue is preserved** — blue
stays blue, red stays red, only light/dark swaps. The scene itself is
stored/rendered on a **white background**; that is exactly what `take_screenshot`
returns (LIGHT mode).

**Therefore: ALWAYS design in normal light-mode conventions** — dark text on
light pastel fills, saturated/dark strokes. Dark mode then inverts this into the
look the instructor wants: dark boxes + white text + bright accents.

Because you design in light mode, `take_screenshot` (a light-mode render) IS a
valid verification of BOTH layout AND readability. What reads well in the
screenshot will read well inverted.

### Color decision table

| Role | Set this color | Renders (for instructor) as | Notes |
|---|---|---|---|
| Text inside boxes / shape labels | `strokeColor: "#1e1e1e"` | WHITE | Instructor demanded: *"dentro das caixas, deixe sempre a fonte branca"* — inside boxes ALWAYS white. |
| Standalone accent text / section titles | saturated DARK hue (below) | BRIGHT accent | |
| Secondary captions / subtitles | `#495057` (dark gray) | light gray, readable | |
| Neutral / structural box fill | `#f1f3f5` (light gray) + `#868e96` stroke | near-black base + subtle border | Avoid pure `#ffffff` fills. |

### Saturated-dark accents for standalone text (invert to BRIGHT)

| Meaning | Use (dark) | NOT the light variant |
|---|---|---|
| blue | `#1971c2` | ~~`#4dabf7`~~ |
| green | `#2f9e44` | ~~`#69db7c`~~ |
| violet | `#7048e8` | ~~`#b197fc`~~ |
| red | `#e03131` | — |
| amber | `#f08c00` | — |

Using the LIGHT variants for standalone text makes it dim/gray after inversion.

### NEVER do this (real regression the instructor rejected)

- Do NOT set text to light colors: `#ffffff`, `#ced4da`, `#e9ecef`, or light
  pastels. In dark mode they invert to DARK and become invisible on the dark
  canvas. The instructor's exact rejection: *"ainda tem coisas cinza... não dá
  pra enxergar."*

> Historical note: an older iteration of these notes told authors to design for a
> dark canvas directly (light text, transparent fills). That was WRONG and caused
> the regression above. The scene is white; the invert filter is what produces
> dark mode. **Design light, let inversion do the work.**

---

## 3. Visual style (the instructor's board pattern)

- **Hand-drawn but perfectly straight**: `roughness: 1` (sketchy stroke) but
  **NEVER any `angle`/tilt** — the instructor hates crooked things (*"estilo a mão
  mas não quero as coisas tortas"*). Omit `angle` entirely. Boxes on a clean grid;
  arrows only horizontal / vertical / elbowed — **NO diagonals**.
- **No frames / molduras**: one continuous canvas; lessons stacked vertically,
  separated by whitespace only.
- **No decorative lines / dividers**: no wavy scribbled separators, no wavy
  underlines. Not their pattern.
- **Restrained semantic palette** (~5–6 colors, each with meaning, not
  decoration). Pastel = `backgroundColor`; saturated = `strokeColor` and
  standalone accent text:

  | Meaning | Pastel fill (`backgroundColor`) | Saturated stroke / accent (`strokeColor`) |
  |---|---|---|
  | workload / apps / instances | `#a5d8ff` | `#1971c2` |
  | Bins/Libs | `#ffec99` | `#f08c00` |
  | weight / negative / cons | `#ffc9c9` | `#e03131` |
  | virtualization / engine layer | `#d0bfff` | `#7048e8` |
  | positive / highlight / conclusion / pros | `#b2f2bb` | `#2f9e44` |
  | base / structure (neutral) | `#f1f3f5` | `#868e96` |

- **Font: Excalifont everywhere** → `fontFamily: 5`.
- **Rounded corners** on shapes → `roundness: {type: 3}`.
- **Alignment & spacing is a quality bar** (explicitly enforced):
  - Peer boxes share identical width, height, and y.
  - Columns share equal gaps (~20px).
  - Vertical gaps between layers are even and generous (~18px).
  - The instructor rejected cramped/uneven spacing and unbalanced box heights and
    asked for a clean, balanced grid. Keep peer items the same size.

### Canonical box skeleton (light-mode design)

```json
{
  "type": "rectangle",
  "tempId": "hostBox",
  "x": 100, "y": 100, "width": 220, "height": 90,
  "roughness": 1,
  "roundness": { "type": 3 },
  "backgroundColor": "#f1f3f5",
  "strokeColor": "#868e96",
  "label": { "text": "Host", "strokeColor": "#1e1e1e", "fontFamily": 5 }
}
```

The label's `strokeColor: "#1e1e1e"` renders WHITE for the instructor in dark
mode. The `#f1f3f5` fill inverts to a near-black box.

---

## 4. Scale / performance gotchas

Large scenes (hundreds+ elements) will make `get_scene_content` and
`take_screenshot` **time out or exceed token limits** (often surfacing as
"session expired" / timeout).

- Prefer `search_scene_content` when you only need to locate specific
  shapes/labels.
- When `get_scene_content` returns a **saved-to-file** result, do NOT read the
  whole file into context. Run `jq` / a Python script over the file to extract a
  compact element table (`id, type, x, y, w, h, strokeColor, backgroundColor,
  text`) and to **generate bulk `update` patches programmatically** rather than
  hand-authoring dozens of patches.
- **Frames cascade-delete their children** — do NOT use frames as removable
  scaffolds around real content.
- **Focused-screenshot trick** (safe on tall scenes): a frame created via MCP
  over EXISTING elements does NOT capture them. So:
  1. `edit_scene_content` add a temporary `frame` over the region of interest.
  2. `take_screenshot(frameId)` — clips to the frame bounds.
  3. Delete the frame — only the frame is removed (`deleted: 1`), children stay.

---

## 5. Content workflow for this course

- **Deliverable pattern:** ONE scene per module (e.g. *"Aulas Módulo 5 - Nível
  11"*), containing ONE key diagram per lesson (NOT full slide decks), stacked
  vertically.
- Each lesson is introduced by a section header `5.0X · Título` in **saturated
  blue `#1971c2`** (standalone accent text — inverts to bright blue).
- **Lesson titles come from `EMENTA-NIVEL-11-AWS.md`** (source of truth in the
  course repo). Confirm numbers/titles there.
- The reference "Nível 9" board is likewise vertical and frameless — same pattern.
- **Always include the scene URL** in your summary:
  `https://app.excalidraw.com/s/<workspaceId>/<sceneId>`.

---

## Quick checklist before writing a scene

- [ ] Called `read_freeform_format` this session (first write only).
- [ ] Designing in LIGHT mode: `#1e1e1e` labels inside boxes, pastel fills,
      saturated-dark accents/titles. No light-colored text anywhere.
- [ ] `roughness: 1`, `roundness: {type: 3}`, `fontFamily: 5`, NO `angle`.
- [ ] No frames, no decorative dividers, no diagonal arrows.
- [ ] Arrows to shapes have `startBinding`/`endBinding` with `fixedPoint` +
      `mode:"inside"`.
- [ ] Peer boxes same width/height/y; even ~18–20px gaps; balanced grid.
- [ ] `label` in updates sent as `{"text":"..."}` object, not a string.
- [ ] Section headers `5.0X · Título` in `#1971c2`, titles from the ementa.
- [ ] Verified layout+readability with `take_screenshot` (light render is valid).
- [ ] Reported the scene URL.

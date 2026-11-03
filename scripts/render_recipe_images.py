#!/usr/bin/env python3
"""Render Minecraft-style recipe images for the README.

Reads this mod's recipe JSONs, models and textures, borrows the vanilla GUI,
font and item textures from a Minecraft client jar, and writes:

  docs/images/recipes/<recipe>.png  the crafting table / furnace / stonecutter /
                                    brewing stand screen for one recipe
  docs/images/tree_<name>.png       an overview of what crafts into what

Usage:
  pip install pillow
  python scripts/render_recipe_images.py [--jar path/to/minecraft-client.jar]

Re-run it whenever a recipe or texture changes.
"""
import argparse
import json
import os
import re
import sys
import zipfile
from collections import Counter
from dataclasses import dataclass
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
MOD_ID = "tacos-mod"
RECIPE_DIR = ROOT / "src/main/resources/data" / MOD_ID / "recipe"
TAG_DIR = ROOT / "src/main/generated/data" / MOD_ID / "tags/item"
MOD_TEXTURES = ROOT / "src/main/resources/assets" / MOD_ID / "textures"
MOD_MODELS = ROOT / "src/main/generated/assets" / MOD_ID
LANG_FILE = ROOT / "src/main/resources/assets" / MOD_ID / "lang/en_us.json"
OUT_DIR = ROOT / "docs/images"

SCALE = 3  # output pixels per GUI pixel, like GUI Scale 3 in game
GUI_WIDTH = 176
GUI_HEIGHT = 166
SCREEN_HEIGHT = 77  # GUI texture rows kept above the player inventory
GUI_BG = (198, 198, 198, 255)
TEXT_COLOR = (64, 64, 64, 255)
COUNT_COLOR = (255, 255, 255, 255)
COUNT_SHADOW = (63, 63, 63, 255)
WATER_COLOR = 0x385DC6
FURNACE_FUEL = "minecraft:coal"
BREWING_FUEL = "minecraft:blaze_powder"
BOTTLES_PER_BREW = 3

STATION_NAMES = {
    "crafting": "Crafting Table",
    "smelting": "Furnace",
    "stonecutting": "Stonecutter",
    "brewing": "Brewing Stand",
}

# Each tree is a list of (section title, recipe names), in crafting order
TREES = {
    "tacos": [
        ("Step 1: Tortillas", ["flour", "dough", "raw_tortilla", "tortilla"]),
        ("Step 2: Fillings", ["ground_beef", "shredded_chicken", "pulled_pork"]),
        ("Step 3: Tacos", ["beef_taco", "crunchwrap", "chicken_taco", "pork_taco", "fish_taco", "suspicious_taco"]),
    ],
    "sodas": [
        ("Step 4: Sugar Cubes & Syrups", ["sugar_block", "baja_blast_syrup", "mountain_dew_syrup", "dr_pepper_syrup"]),
        ("Step 5: Brewing", ["baja_blast_potion", "mountain_dew_potion", "dr_pepper_potion"]),
    ],
}


@dataclass(frozen=True)
class ItemRef:
    id: str  # "minecraft:wheat", or "#tacos-mod:cooked_fish" for an item tag
    potion: str | None = None
    color: int | None = None


@dataclass(frozen=True)
class Stack:
    item: ItemRef
    count: int = 1


@dataclass(frozen=True)
class Recipe:
    name: str
    station: str
    inputs: tuple[Stack, ...]
    output: Stack
    grid: tuple[ItemRef | None, ...] = ()  # 9 crafting slots, row by row


# --- Recipe parsing ---------------------------------------------------------

def parse_ingredient(value) -> ItemRef:
    if isinstance(value, str):
        return ItemRef(value)
    potion = value.get("potion_contents", {}).get("potion")
    color = WATER_COLOR if potion == "minecraft:water" else None
    return ItemRef(value["item"], potion, color)


def parse_result(value) -> Stack:
    contents = value.get("components", {}).get("minecraft:potion_contents")
    if contents:
        item = ItemRef(value["id"], contents["potion"], contents.get("custom_color"))
    else:
        item = ItemRef(value["id"])
    return Stack(item, value.get("count", 1))


def count_stacks(refs) -> tuple[Stack, ...]:
    counts = Counter(refs)  # keeps first-seen order
    return tuple(Stack(ref, n) for ref, n in counts.items())


def shaped_grid(data) -> list[ItemRef | None]:
    pattern = data["pattern"]
    width = max(len(row) for row in pattern)
    # Centre 1-wide and 1-tall patterns so they read naturally in a 3x3 grid
    col_offset = 1 if width == 1 else 0
    row_offset = 1 if len(pattern) == 1 else 0
    grid = [None] * 9
    for r, row in enumerate(pattern):
        for c, symbol in enumerate(row):
            if symbol != " ":
                grid[(r + row_offset) * 3 + c + col_offset] = parse_ingredient(data["key"][symbol])
    return grid


def load_recipe(name: str) -> Recipe:
    data = json.loads((RECIPE_DIR / f"{name}.json").read_text(encoding="utf-8"))
    kind = data["type"].removeprefix("minecraft:")

    if kind in ("crafting_shaped", "crafting_shapeless"):
        if kind == "crafting_shaped":
            grid = shaped_grid(data)
        else:
            refs = [parse_ingredient(i) for i in data["ingredients"]]
            grid = refs + [None] * (9 - len(refs))
        inputs = count_stacks(ref for ref in grid if ref)
        return Recipe(name, "crafting", inputs, parse_result(data["result"]), tuple(grid))
    if kind in ("smelting", "stonecutting"):
        inputs = (Stack(parse_ingredient(data["ingredient"])),)
        return Recipe(name, kind, inputs, parse_result(data["result"]))
    if kind == "brewing":
        inputs = (
            Stack(parse_ingredient(data["reagent"])),
            Stack(parse_ingredient(data["input"]), BOTTLES_PER_BREW),
        )
        output = parse_result(data["output"])
        return Recipe(name, kind, inputs, Stack(output.item, BOTTLES_PER_BREW))
    raise ValueError(f"Unsupported recipe type '{data['type']}' in {name}.json")


# --- Image helpers ----------------------------------------------------------

def upscale(image: Image.Image, factor: int = SCALE) -> Image.Image:
    return image.resize((image.width * factor, image.height * factor), Image.NEAREST)


def flat_icon(texture: Image.Image) -> Image.Image:
    """An item texture at slot size; oversized textures are squeezed in like the game does."""
    if texture.size == (16, 16):
        return upscale(texture)
    return texture.resize((16 * SCALE, 16 * SCALE), Image.LANCZOS)


def missing_texture() -> Image.Image:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    magenta = Image.new("RGBA", (8, 8), (248, 0, 248, 255))
    image.paste(magenta, (0, 0))
    image.paste(magenta, (8, 8))
    return image


def tint(image: Image.Image, rgb: int) -> Image.Image:
    multipliers = ((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255)
    red, green, blue, alpha = image.split()
    tinted = [band.point(lambda v, m=m: v * m // 255) for band, m in zip((red, green, blue), multipliers)]
    return Image.merge("RGBA", (*tinted, alpha))


def shade(image: Image.Image, brightness: float) -> Image.Image:
    level = int(255 * brightness)
    return tint(image, (level << 16) | (level << 8) | level)


def face(texture: Image.Image, origin, u, v, size: int) -> Image.Image:
    """Map a texture onto the parallelogram origin + s*u + t*v (s, t in 0..1)."""
    detail = 8
    source = texture.resize((texture.width * detail, texture.height * detail), Image.NEAREST)
    det = u[0] * v[1] - v[0] * u[1]
    a, b = v[1] / det * source.width, -v[0] / det * source.width
    d, e = -u[1] / det * source.height, u[0] / det * source.height
    c = -(a * origin[0] + b * origin[1])
    f = -(d * origin[0] + e * origin[1])
    return source.transform((size, size), Image.AFFINE, (a, b, c, d, e, f), resample=Image.NEAREST)


def iso_block(top: Image.Image, left: Image.Image, right: Image.Image, height: int = 16) -> Image.Image:
    """Draw a block the way the inventory shows it, at output resolution."""
    size = 16 * SCALE
    half_width, rise = 6.9 * SCALE, 3.98 * SCALE  # isometric top face
    side = 7.97 * SCALE * height / 16
    centre_x = 8 * SCALE
    top_y = 15.95 * SCALE - side - 2 * rise
    peak, east = (centre_x, top_y), (centre_x + half_width, top_y + rise)
    front, west = (centre_x, top_y + 2 * rise), (centre_x - half_width, top_y + rise)

    def towards(p, q):
        return (q[0] - p[0], q[1] - p[1])

    block = Image.new("RGBA", (size, size))
    block.alpha_composite(shade(face(left, west, towards(west, front), (0, side), size), 0.8))
    block.alpha_composite(shade(face(right, front, towards(front, east), (0, side), size), 0.6))
    block.alpha_composite(face(top, west, towards(west, peak), towards(west, front), size))
    return block


# --- Asset loading ----------------------------------------------------------

class Assets:
    def __init__(self, jar_path: Path):
        self.jar = zipfile.ZipFile(jar_path)
        self.lang = json.loads(LANG_FILE.read_text(encoding="utf-8"))
        self.missing: set[str] = set()
        self._icons: dict[ItemRef, Image.Image] = {}

    def vanilla(self, path: str) -> Image.Image:
        with self.jar.open(f"assets/minecraft/textures/{path}.png") as file:
            return Image.open(file).convert("RGBA")

    def texture(self, ref: str) -> Image.Image:
        namespace, path = ref.split(":")
        if namespace == "minecraft":
            return self.vanilla(path)
        file = MOD_TEXTURES / f"{path}.png"
        if not file.exists():
            self.missing.add(ref)
            return missing_texture()
        return Image.open(file).convert("RGBA")

    def name(self, item: ItemRef) -> str:
        if item.potion:
            if item.potion == "minecraft:water":
                return "Water Bottle"
            return self.lang[f"item.minecraft.potion.effect.{item.potion.split(':')[1]}"]
        namespace, path = item.id.lstrip("#").split(":")
        if item.id.startswith("#"):
            return self.lang[f"tag.item.{namespace}.{path}"]
        if namespace == MOD_ID:
            return self.lang.get(f"item.{namespace}.{path}") or self.lang[f"block.{namespace}.{path}"]
        return path.replace("_", " ").title()

    def icon(self, item: ItemRef) -> Image.Image:
        if item not in self._icons:
            self._icons[item] = self._render_icon(item)
        return self._icons[item]

    def _render_icon(self, item: ItemRef) -> Image.Image:
        if item.potion:
            bottle = tint(self.vanilla("item/potion_overlay"), item.color or WATER_COLOR)
            bottle.alpha_composite(self.vanilla("item/potion"))
            return upscale(bottle)
        namespace, path = item.id.lstrip("#").split(":")
        if item.id.startswith("#"):
            tag = json.loads((TAG_DIR / f"{path}.json").read_text(encoding="utf-8"))
            return self.icon(ItemRef(tag["values"][0]))
        if namespace == MOD_ID:
            return self._mod_icon(path)
        return flat_icon(self.vanilla(f"item/{path}"))

    def _mod_icon(self, path: str) -> Image.Image:
        item_def = json.loads((MOD_MODELS / "items" / f"{path}.json").read_text(encoding="utf-8"))
        model_path = item_def["model"]["model"].split(":")[1]
        model = json.loads((MOD_MODELS / "models" / f"{model_path}.json").read_text(encoding="utf-8"))
        textures = model["textures"]
        if model.get("parent", "").endswith("cube_all"):
            texture = self.texture(textures["all"])
            return iso_block(texture, texture, texture)
        return flat_icon(self.texture(textures["layer0"]))

    def station_icon(self, station: str) -> Image.Image:
        if station == "crafting":
            return iso_block(*(self.vanilla(f"block/crafting_table_{side}") for side in ("top", "front", "side")))
        if station == "smelting":
            return iso_block(*(self.vanilla(f"block/furnace_{side}") for side in ("top", "front", "side")))
        if station == "stonecutting":
            side = self.vanilla("block/stonecutter_side").crop((0, 7, 16, 16))
            return iso_block(self.vanilla("block/stonecutter_top"), side, side, height=9)
        return upscale(self.vanilla("item/brewing_stand"))

    def gui(self, name: str) -> Image.Image:
        return self.vanilla(f"gui/container/{name}")

    def sprite(self, name: str) -> Image.Image:
        return self.vanilla(f"gui/sprites/container/{name}")


class McFont:
    """The vanilla bitmap font from font/ascii.png."""

    def __init__(self, sheet: Image.Image):
        self.sheet = sheet
        self.cell = sheet.width // 16

    def _glyph(self, char: str) -> Image.Image:
        code = ord(char) if ord(char) < 256 else ord("?")
        x, y = (code % 16) * self.cell, (code // 16) * self.cell
        return self.sheet.crop((x, y, x + self.cell, y + self.cell))

    def advance(self, char: str) -> int:
        if char == " ":
            return 4
        bbox = self._glyph(char).getbbox()
        return (bbox[2] if bbox else 0) + 1

    def width(self, text: str, bold: bool = False) -> int:
        return sum(self.advance(c) + (1 if bold else 0) for c in text)

    def draw(self, canvas: "Canvas", text: str, x: int, y: int, color, shadow=None, bold: bool = False):
        passes = ([(1, 1, shadow)] if shadow else []) + [(0, 0, color)] + ([(1, 0, color)] if bold else [])
        for char in text:
            mask = self._glyph(char).getchannel("A")
            for dx, dy, rgba in passes:
                glyph = Image.new("RGBA", mask.size, rgba)
                glyph.putalpha(mask)
                canvas.draw_gui(glyph, x + dx, y + dy)
            x += self.advance(char) + (1 if bold else 0)


class Canvas:
    """An output image addressed in GUI pixels."""

    def __init__(self, width: int, height: int):
        self.width, self.height = width, height
        self.image = Image.new("RGBA", (width * SCALE, height * SCALE))

    def draw(self, image: Image.Image, x: int, y: int):
        self.image.alpha_composite(image, (x * SCALE, y * SCALE))

    def draw_gui(self, image: Image.Image, x: int, y: int):
        self.draw(upscale(image), x, y)

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.image.save(path, optimize=True)


# --- Shared drawing ---------------------------------------------------------

class Painter:
    def __init__(self, assets: Assets):
        self.assets = assets
        self.font = McFont(assets.vanilla("font/ascii"))
        self.window = assets.gui("crafting_table")
        self.slot = self.window.crop((29, 16, 47, 34))
        self.arrow = self.window.crop((89, 34, 113, 51))

    def stack(self, canvas: Canvas, stack: Stack, x: int, y: int, show_count: bool = True):
        canvas.draw(self.assets.icon(stack.item), x, y)
        if show_count and stack.count > 1:
            text = str(stack.count)
            self.font.draw(canvas, text, x + 17 - self.font.width(text), y + 9, COUNT_COLOR, COUNT_SHADOW)

    def panel(self, width: int, height: int) -> Image.Image:
        """Stretch the vanilla window border to any size."""
        edge = 4
        right, bottom = GUI_WIDTH - edge, GUI_HEIGHT - edge
        panel = Image.new("RGBA", (width, height), GUI_BG)
        edges = [
            ((edge, 0, edge + 1, edge), (edge, 0), (width - 2 * edge, edge)),
            ((edge, bottom, edge + 1, GUI_HEIGHT), (edge, height - edge), (width - 2 * edge, edge)),
            ((0, edge, edge, edge + 1), (0, edge), (edge, height - 2 * edge)),
            ((right, edge, GUI_WIDTH, edge + 1), (width - edge, edge), (edge, height - 2 * edge)),
        ]
        for box, at, size in edges:
            panel.paste(self.window.crop(box).resize(size, Image.NEAREST), at)
        corners = [((0, 0), (0, 0)), ((right, 0), (width - edge, 0)),
                   ((0, bottom), (0, height - edge)), ((right, bottom), (width - edge, height - edge))]
        for (sx, sy), at in corners:
            panel.paste(self.window.crop((sx, sy, sx + edge, sy + edge)), at)
        return panel

    def screen(self, gui_name: str) -> Canvas:
        """A container screen with the player inventory cut off."""
        texture = self.assets.gui(gui_name)
        canvas = Canvas(GUI_WIDTH, SCREEN_HEIGHT + 6)
        canvas.draw_gui(texture.crop((0, 0, GUI_WIDTH, SCREEN_HEIGHT)), 0, 0)
        canvas.draw_gui(texture.crop((0, GUI_HEIGHT - 6, GUI_WIDTH, GUI_HEIGHT)), 0, SCREEN_HEIGHT)
        return canvas

    def title(self, canvas: Canvas, text: str, x: int | None = None):
        if x is None:
            x = (canvas.width - self.font.width(text)) // 2
        self.font.draw(canvas, text, x, 6, TEXT_COLOR)


# --- Recipe screens ---------------------------------------------------------

def render_crafting(p: Painter, recipe: Recipe) -> Canvas:
    canvas = p.screen("crafting_table")
    p.title(canvas, "Crafting", 29)
    for index, item in enumerate(recipe.grid):
        if item:
            p.stack(canvas, Stack(item), 30 + (index % 3) * 18, 17 + (index // 3) * 18)
    p.stack(canvas, recipe.output, 124, 35)
    return canvas


def render_smelting(p: Painter, recipe: Recipe) -> Canvas:
    canvas = p.screen("furnace")
    p.title(canvas, "Furnace")
    canvas.draw_gui(p.assets.sprite("furnace/lit_progress"), 56, 36)
    canvas.draw_gui(p.assets.sprite("furnace/burn_progress"), 79, 34)
    p.stack(canvas, recipe.inputs[0], 56, 17)
    p.stack(canvas, Stack(ItemRef(FURNACE_FUEL)), 56, 53)
    p.stack(canvas, recipe.output, 116, 35)
    return canvas


def render_stonecutting(p: Painter, recipe: Recipe) -> Canvas:
    canvas = p.screen("stonecutter")
    p.title(canvas, "Stonecutter", 8)
    canvas.draw_gui(p.assets.sprite("stonecutter/recipe_selected"), 52, 15)
    p.stack(canvas, recipe.output, 52, 16, show_count=False)
    canvas.draw_gui(p.assets.sprite("stonecutter/scroller_disabled"), 119, 15)
    p.stack(canvas, recipe.inputs[0], 20, 33)
    p.stack(canvas, recipe.output, 143, 33)
    return canvas


def render_brewing(p: Painter, recipe: Recipe) -> Canvas:
    """The brewing stand before brewing, next to the potions it makes."""
    reagent, base = recipe.inputs
    stand = p.screen("brewing_stand")
    p.title(stand, "Brewing Stand")
    for sprite, x, y in (("fuel_length", 60, 44), ("brew_progress", 97, 16), ("bubbles", 63, 14)):
        stand.draw_gui(p.assets.sprite(f"brewing_stand/{sprite}"), x, y)
    p.stack(stand, reagent, 79, 17)
    p.stack(stand, Stack(ItemRef(BREWING_FUEL)), 17, 17)
    for x, y in ((56, 51), (79, 58), (102, 51)):
        p.stack(stand, Stack(base.item), x, y)

    gap = 4
    result_width = 8 + p.arrow.width + 4 + BOTTLES_PER_BREW * SLOT + 7
    canvas = Canvas(GUI_WIDTH + gap + result_width, stand.height)
    canvas.image.paste(stand.image, (0, 0))
    left = GUI_WIDTH + gap
    canvas.draw_gui(p.panel(result_width, stand.height), left, 0)
    p.font.draw(canvas, "Result", left + (result_width - p.font.width("Result")) // 2, 6, TEXT_COLOR)
    canvas.draw_gui(p.arrow, left + 7, 33)
    for i in range(BOTTLES_PER_BREW):
        x = left + 8 + p.arrow.width + 4 + i * SLOT
        canvas.draw_gui(p.slot, x, 33)
        p.stack(canvas, Stack(recipe.output.item), x + 1, 34)
    return canvas


SCREEN_RENDERERS = {
    "crafting": render_crafting,
    "smelting": render_smelting,
    "stonecutting": render_stonecutting,
    "brewing": render_brewing,
}


# --- Crafting trees ---------------------------------------------------------

PAD = 8
SLOT = 18
LINE_HEIGHT = 9
ROW_HEIGHT = SLOT + 2 + 2 * LINE_HEIGHT
ROW_GAP = 4
HEADER_HEIGHT = 14
GAP = 6
PLUS_WIDTH = 9
MAX_LABEL_WIDTH = 44


def wrap_label(font: McFont, text: str) -> list[str]:
    """Split a long name over two lines, as evenly as possible."""
    words = text.split()
    if font.width(text) <= MAX_LABEL_WIDTH or len(words) == 1:
        return [text]
    splits = [(" ".join(words[:i]), " ".join(words[i:])) for i in range(1, len(words))]
    return list(min(splits, key=lambda pair: max(font.width(pair[0]), font.width(pair[1]))))


class TreeRenderer:
    """Rows of 'ingredients + ... -> station -> result', grouped into steps."""

    def __init__(self, painter: Painter):
        self.p = painter
        self.font = painter.font

    def labelled_width(self, text: str) -> int:
        return max(SLOT, *(self.font.width(line) for line in wrap_label(self.font, text)))

    def inputs_width(self, recipe: Recipe) -> int:
        widths = [self.labelled_width(self.p.assets.name(s.item)) for s in recipe.inputs]
        return sum(widths) + (len(widths) - 1) * (GAP + PLUS_WIDTH + GAP)

    def station_width(self, recipe: Recipe) -> int:
        return max(16 + 2 + self.p.arrow.width, self.labelled_width(STATION_NAMES[recipe.station]))

    def render(self, sections: list[tuple[str, list[Recipe]]]) -> Canvas:
        recipes = [r for _, rs in sections for r in rs]
        inputs_w = max(self.inputs_width(r) for r in recipes)
        station_w = max(self.station_width(r) for r in recipes)
        output_w = max(self.labelled_width(self.p.assets.name(r.output.item)) for r in recipes)
        station_x = PAD + inputs_w + 2 * GAP
        output_x = station_x + station_w + 2 * GAP
        width = output_x + output_w + PAD
        height = PAD + sum(HEADER_HEIGHT + len(rs) * (ROW_HEIGHT + ROW_GAP) for _, rs in sections) + PAD

        canvas = Canvas(width, height)
        canvas.draw_gui(self.p.panel(width, height), 0, 0)
        y = PAD
        for title, section in sections:
            self.font.draw(canvas, title, PAD, y + 2, TEXT_COLOR, bold=True)
            y += HEADER_HEIGHT
            for recipe in section:
                self.row(canvas, recipe, y, station_x, station_w, output_x)
                y += ROW_HEIGHT + ROW_GAP
        return canvas

    def row(self, canvas: Canvas, recipe: Recipe, y: int, station_x: int, station_w: int, output_x: int):
        x = PAD
        for i, stack in enumerate(recipe.inputs):
            if i:
                self.font.draw(canvas, "+", x + GAP + 2, y + 5, TEXT_COLOR)
                x += GAP + PLUS_WIDTH + GAP
            x += self.labelled_slot(canvas, stack, x, y)

        icon_x = station_x + (station_w - (16 + 2 + self.p.arrow.width)) // 2
        canvas.draw(self.p.assets.station_icon(recipe.station), icon_x, y + 1)
        canvas.draw_gui(self.p.arrow, icon_x + 18, y + 1)
        self.label(canvas, STATION_NAMES[recipe.station], station_x, station_w, y + SLOT + 2)

        self.labelled_slot(canvas, recipe.output, output_x, y)

    def labelled_slot(self, canvas: Canvas, stack: Stack, x: int, y: int) -> int:
        name = self.p.assets.name(stack.item)
        width = self.labelled_width(name)
        slot_x = x + (width - SLOT) // 2
        canvas.draw_gui(self.p.slot, slot_x, y)
        self.p.stack(canvas, stack, slot_x + 1, y + 1)
        self.label(canvas, name, x, width, y + SLOT + 2)
        return width

    def label(self, canvas: Canvas, text: str, x: int, width: int, y: int):
        for i, line in enumerate(wrap_label(self.font, text)):
            self.font.draw(canvas, line, x + (width - self.font.width(line)) // 2, y + i * LINE_HEIGHT, TEXT_COLOR)


# --- Entry point ------------------------------------------------------------

def default_jar() -> Path:
    """The launcher's vanilla jar for the Minecraft version this mod targets."""
    properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    version = re.search(r"^minecraft_version=(.+)$", properties, re.MULTILINE).group(1).strip()
    versions = Path(os.environ.get("APPDATA", Path.home())) / ".minecraft" / "versions"
    return versions / version / f"{version}.jar"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jar", type=Path, default=None,
                        help="vanilla Minecraft client jar to take GUI and item textures from "
                             "(default: the launcher's jar for minecraft_version in gradle.properties)")
    args = parser.parse_args()

    jar = args.jar or default_jar()
    if not jar.exists():
        print(f"Could not find {jar}. Launch that version once, or pass a jar with --jar.", file=sys.stderr)
        return 1
    print(f"Using textures from {jar}")

    painter = Painter(Assets(jar))
    covered = {name for sections in TREES.values() for _, names in sections for name in names}
    uncovered = sorted({f.stem for f in RECIPE_DIR.glob("*.json")} - covered)
    if uncovered:
        print(f"warning: recipes not in any tree: {', '.join(uncovered)}", file=sys.stderr)

    trees = TreeRenderer(painter)
    for tree_name, sections in TREES.items():
        loaded = [(title, [load_recipe(n) for n in names]) for title, names in sections]
        for _, recipes in loaded:
            for recipe in recipes:
                SCREEN_RENDERERS[recipe.station](painter, recipe).save(OUT_DIR / "recipes" / f"{recipe.name}.png")
        trees.render(loaded).save(OUT_DIR / f"tree_{tree_name}.png")

    for ref in sorted(painter.assets.missing):
        print(f"warning: missing texture {ref}, drawn as the purple/black missing texture", file=sys.stderr)
    print(f"Wrote images to {OUT_DIR}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

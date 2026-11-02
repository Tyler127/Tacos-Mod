"""Generate the README graphics in docs/images from the mod's textures.

Usage (from the repo root):  python3 docs/generate_images.py
Requires Pillow (pip install pillow).
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src/main/resources/assets/tacos-mod/textures"
OUT = ROOT / "docs/images"
ICONS = OUT / "icons"
ICON_SIZE = 64

FONT_DIRS = [Path("/usr/share/fonts"), Path("/Library/Fonts"), Path("C:/Windows/Fonts")]

# Drink colors match the custom_color values in the brewing recipes
DRINKS = {
    "baja_blast_potion": 0x69FFCB,
    "mountain_dew_potion": 0x9AE61E,
    "dr_pepper_potion": 0x6E0F1E,
}

# 16x16 potion bottle (original pixel art): o = outline, c = cork, g = glass, l = liquid, h = highlight
BOTTLE = [
    "................",
    "......oooo......",
    "......occo......",
    "......occo......",
    ".....oooooo.....",
    "......oggo......",
    "......oggo......",
    ".....oglggo.....",
    "....ogllllgo....",
    "...ohllllllgo...",
    "...ohllllllgo...",
    "...ollllllllo...",
    "...ollllllllo...",
    "....ollllllo....",
    ".....oooooo.....",
    "................",
]


def find_font(*names):
    for name in names:
        for base in FONT_DIRS:
            hits = list(base.rglob(name)) if base.exists() else []
            if hits:
                return str(hits[0])
    return None


def load_texture(path: Path) -> Image.Image:
    img = Image.open(path).convert("RGBA")
    if max(img.size) > 32:
        # High-res source texture: shrink smoothly onto a square canvas
        img.thumbnail((ICON_SIZE, ICON_SIZE), Image.LANCZOS)
        canvas = Image.new("RGBA", (ICON_SIZE, ICON_SIZE))
        canvas.alpha_composite(img, ((ICON_SIZE - img.width) // 2, (ICON_SIZE - img.height) // 2))
        return canvas
    # Pixel art: scale up with nearest neighbor so it stays crisp
    return img.resize((ICON_SIZE, ICON_SIZE), Image.NEAREST)


def shade(rgb, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in rgb)


def make_bottle(color: int) -> Image.Image:
    liquid = ((color >> 16) & 255, (color >> 8) & 255, color & 255)
    palette = {
        "o": (38, 30, 42, 255),
        "c": (150, 103, 64, 255),
        "g": (200, 225, 235, 170),
        "l": liquid + (255,),
        "h": (255, 255, 255, 210),
    }
    img = Image.new("RGBA", (16, 16))
    for y, row in enumerate(BOTTLE):
        for x, ch in enumerate(row):
            if ch in palette:
                img.putpixel((x, y), palette[ch])
    # Darken the bottom of the liquid a little for depth
    for y in range(12, 14):
        for x in range(16):
            if BOTTLE[y][x] == "l":
                img.putpixel((x, y), shade(liquid, 0.78) + (255,))
    return img.resize((ICON_SIZE, ICON_SIZE), Image.NEAREST)


def build_icons() -> dict:
    ICONS.mkdir(parents=True, exist_ok=True)
    icons = {}
    for path in sorted((TEXTURES / "item").glob("*.png")):
        icons[path.stem] = load_texture(path)
    icons["sugar_block"] = load_texture(TEXTURES / "block/sugar_block.png")
    icons["flight"] = load_texture(TEXTURES / "mob_effect/flight.png")
    icons["zombie_disguise"] = load_texture(TEXTURES / "mob_effect/zombie_disguise.png")
    for name, color in DRINKS.items():
        icons[name] = make_bottle(color)
    for name, img in icons.items():
        img.save(ICONS / f"{name}.png", optimize=True)
    return icons


def build_banner(icons: dict) -> None:
    w, h = 1280, 360
    banner = Image.new("RGBA", (w, h))
    draw = ImageDraw.Draw(banner)

    # Warm sunset gradient
    top, bottom = (255, 186, 73), (214, 64, 46)
    for y in range(h):
        t = y / (h - 1)
        draw.line([(0, y), (w, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bottom)) + (255,))

    # Subtle pixel-grid texture
    grid = Image.new("RGBA", (w, h))
    gdraw = ImageDraw.Draw(grid)
    for x in range(0, w, 16):
        gdraw.line([(x, 0), (x, h)], fill=(255, 255, 255, 14))
    for y in range(0, h, 16):
        gdraw.line([(0, y), (w, y)], fill=(255, 255, 255, 14))
    banner.alpha_composite(grid)

    title_font_path = find_font("Inter-Black.otf", "DejaVuSans-Bold.ttf", "arialbd.ttf")
    body_font_path = find_font("Inter-SemiBold.otf", "DejaVuSans.ttf", "arial.ttf")
    title_font = ImageFont.truetype(title_font_path, 104) if title_font_path else ImageFont.load_default()
    body_font = ImageFont.truetype(body_font_path, 30) if body_font_path else ImageFont.load_default()

    def shadowed_text(xy, text, font, fill, anchor):
        layer = Image.new("RGBA", (w, h))
        ImageDraw.Draw(layer).text((xy[0] + 5, xy[1] + 6), text, font=font, fill=(90, 20, 10, 150), anchor=anchor)
        banner.alpha_composite(layer.filter(ImageFilter.GaussianBlur(3)))
        ImageDraw.Draw(banner).text(xy, text, font=font, fill=fill, anchor=anchor)

    shadowed_text((w // 2, 92), "Tacos Mod", title_font, (255, 251, 240, 255), "mm")
    shadowed_text((w // 2, 168), "Tacos, Crunchwraps & questionable sodas for Minecraft", body_font,
                  (255, 240, 220, 255), "mm")

    lineup = ["beef_taco", "chicken_taco", "baja_blast_potion", "crunchwrap", "dr_pepper_potion",
              "fish_taco", "mountain_dew_potion", "pork_taco", "suspicious_taco"]
    size, gap = 96, 22
    total = len(lineup) * size + (len(lineup) - 1) * gap
    x0, y0 = (w - total) // 2, 220
    for i, name in enumerate(lineup):
        img = icons[name].resize((size, size), Image.NEAREST)
        x = x0 + i * (size + gap)
        y = y0 + (8 if i % 2 else 0)
        shadow = Image.new("RGBA", (w, h))
        silhouette = Image.new("RGBA", img.size, (80, 15, 5, 120))
        shadow.paste(silhouette, (x + 4, y + 8), img)
        banner.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(4)))
        banner.alpha_composite(img, (x, y))

    banner.convert("RGB").save(OUT / "banner.png", optimize=True)


if __name__ == "__main__":
    icons = build_icons()
    build_banner(icons)
    print(f"Wrote {len(icons)} icons and a banner to {OUT.relative_to(ROOT)}")

import subprocess

# We'll render at 1024x1024 for supersampling anti-aliasing, then resize to 512x512
# Scale: 2x the 512 canvas coordinates

# Coordinates on 1024x1024:
# Cover top: y = 200
# Cover bottom: y = 740
# Cover left spine separator: x = 330
# Cover right edge: x = 800
# Center of front cover: x = 565, y = 470
# Spine left line: x = 220
# Rings: x from 155 to 285. Height: 44. Y centers: 336, 488, 640.

BLUE = "#0084FF"
WHITE = "#FFFFFF"
BLACK = "#000000"

draw_ops = [
    # 1. Background
    f"fill {BLACK} rectangle 0,0 1024,1024",

    # 2. Main Blue Line Work
    f"stroke {BLUE} fill none stroke-width 36 stroke-linecap round stroke-linejoin round",

    # Front cover outline: top line, top-right rounded corner, right edge, bottom-right rounded corner, bottom cover line
    # M 330,200 L 740,200 A 60,60 0 0,1 800,260 L 800,680 A 60,60 0 0,1 740,740 L 330,740
    "path 'M 330,200 L 740,200 A 60,60 0 0,1 800,260 L 800,680 A 60,60 0 0,1 740,740 L 330,740'",

    # Vertical line separating spine from front cover
    "line 330,200 330,740",

    # Spine left vertical line connecting between rings
    "line 220,240 220,720",

    # Bottom book spine curl under the white pages:
    # Curves from (220,720) down around to (250,850) -> horizontal bottom cover to (800,850)
    "path 'M 220,720 A 50,50 0 0,0 270,770 L 270,800 A 50,50 0 0,1 220,850 L 800,850 A 20,20 0 0,0 820,830'",

    # 3. White Pages Block at bottom
    f"stroke none fill {WHITE}",
    # Horizontal page block from x=250 to x=800, y=770 to y=824 with left curve
    "path 'M 260,770 L 800,770 L 800,824 L 260,824 A 27,27 0 0,1 235,797 A 27,27 0 0,1 260,770 Z'",

    # 4. White Rings on left spine (Capsules)
    f"stroke none fill {WHITE}",
    # Ring 1
    "roundrectangle 155,314 285,358 22,22",
    # Ring 2
    "roundrectangle 155,466 285,510 22,22",
    # Ring 3
    "roundrectangle 155,618 285,662 22,22",

    # 5. Dumbbell on front cover (Center: x=565, y=470)
    f"stroke none fill {BLUE}",

    # Horizontal connecting bar
    "roundrectangle 475,452 655,488 14,14",

    # Left inner plate (tall)
    "roundrectangle 465,340 505,600 20,20",

    # Left outer plate (medium)
    "roundrectangle 415,380 453,560 19,19",

    # Left tip nub
    "roundrectangle 375,454 403,486 12,12",

    # Right inner plate (tall)
    "roundrectangle 625,340 665,600 20,20",

    # Right outer plate (medium)
    "roundrectangle 677,380 715,560 19,19",

    # Right tip nub
    "roundrectangle 727,454 755,486 12,12",
]

cmd = ["convert", "-size", "1024x1024", "xc:#000000", "-type", "truecolor"]
for op in draw_ops:
    cmd.extend(["-draw", op])

cmd.extend(["-resize", "512x512", "brand_new_logo.png"])

subprocess.run(cmd, check=True)
print("brand_new_logo.png created successfully!")

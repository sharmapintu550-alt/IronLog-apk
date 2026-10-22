import subprocess
import os

# 1024x1024 base canvas for high definition anti-aliasing
# Coordinates scaled x2 (from 512x512):
# Center of canvas: (512, 512)
# Book cover width: from x=320 to x=820, height: from y=190 to y=880
# Front cover center: X ≈ 570, Y ≈ 470

BLUE = "#0084FF"
WHITE = "#FFFFFF"
BLACK = "#000000"

draw_commands = [
    # Background
    f"fill {BLACK} rectangle 0,0 1024,1024",

    # Settings for blue line art
    f"stroke {BLUE} fill none stroke-width 36 stroke-linecap round stroke-linejoin round",

    # Front cover main border (top, right, bottom of front cover)
    # Starts at top spine (x=330, y=210) -> top-right corner (x=800, y=210) -> right edge -> bottom-right corner (x=800, y=740) -> bottom edge to spine (x=240, y=740)
    "path 'M 330,210 L 750,210 A 60,60 0 0,1 810,270 L 810,690 A 60,60 0 0,1 750,750 L 240,750'",

    # Inner spine vertical line separating front cover and spine
    "line 330,210 330,730",

    # Spine left vertical line connecting the rings and curling at the bottom
    "line 210,240 210,680",

    # Spine bottom curl (curves down, around, and under the white pages)
    # Curves from (210, 680) down to (210, 770) -> curve right under pages -> (240, 860) to (800, 860) -> curve up
    "path 'M 210,680 L 210,750 A 60,60 0 0,0 270,810 L 270,830 A 50,50 0 0,1 220,880 L 780,880 A 40,40 0 0,0 820,840'",

    # White pages block at the bottom
    # Solid white block fill
    f"stroke none fill {WHITE}",
    "path 'M 250,776 L 790,776 A 10,10 0 0,1 800,786 L 800,820 A 10,10 0 0,1 790,830 L 280,830 A 40,40 0 0,1 250,796 Z'",

    # Three white spiral rings on left
    # Ring 1 (Top)
    f"roundrectangle 150,300 280,344 22,22",
    # Ring 2 (Middle)
    f"roundrectangle 150,460 280,504 22,22",
    # Ring 3 (Bottom)
    f"roundrectangle 150,620 280,664 22,22",

    # Dumbbell in center of front cover:
    # Center is approximately (570, 480)
    # Barbell central bar (horizontal)
    f"fill {BLUE} stroke none",
    "roundrectangle 470,460 670,500 12,12",

    # Left inner plate (tall)
    "roundrectangle 460,345 502,615 21,21",

    # Left outer plate (medium)
    "roundrectangle 410,380 448,580 19,19",

    # Left tip nub
    "roundrectangle 368,462 396,498 12,12",

    # Right inner plate (tall)
    "roundrectangle 638,345 680,615 21,21",

    # Right outer plate (medium)
    "roundrectangle 692,380 730,580 19,19",

    # Right tip nub
    "roundrectangle 744,462 772,498 12,12",
]

# Write to temp file for ImageMagick
draw_script = " ".join([f"-draw \"{cmd}\"" for cmd in draw_commands])

full_cmd = f"convert -size 1024x1024 xc:black {draw_script} -resize 512x512 logo_gen_test.png"
subprocess.run(full_cmd, shell=True, check=True)
print("Generated logo_gen_test.png successfully")

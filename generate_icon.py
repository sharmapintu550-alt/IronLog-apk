import subprocess

# We will create a high-res 1024x1024 image using ImageMagick
# Colors:
# Background: #000000
# Blue: #0084FF
# White: #FFFFFF

# Coordinates scaled to 1024x1024
# Book bounds:
# left spine: x ~ 230
# right edge: x ~ 800
# top edge: y ~ 210
# bottom of cover: y ~ 720
# bottom pages block: y ~ 750 to 800
# rings: x ~ 160 to 280

cmd = [
    "convert", "-size", "1024x1024", "xc:black",
    "-stroke", "#0084FF", "-fill", "none",
    "-strokewidth", "36",
    "-linejoin", "round", "-linecap", "round",
    # Main outer book frame
    "-draw", "path 'M 320,210 L 760,210 A 60,60 0 0,1 820,270 L 820,700 A 40,40 0 0,1 780,740 L 250,740'",
    # Left spine vertical
    "-draw", "line 230,240 230,730",
    # Bottom spine curl
    "-draw", "path 'M 230,730 A 50,50 0 0,0 280,780 L 280,840 A 50,50 0 0,1 230,890 L 800,890 A 20,20 0 0,0 820,870'",
    "test_preview.png"
]
subprocess.run(cmd, check=True)

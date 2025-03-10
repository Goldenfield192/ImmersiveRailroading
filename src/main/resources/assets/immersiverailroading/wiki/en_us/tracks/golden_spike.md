# Overview
The Golden Spike allows you to plan, move and shape the track with the [Track Blueprint](immersiverailroading:wiki/en_us/track_blueprint.md)'s blueprint mode.

# Usage
After placing a blueprint, you can right-click on it with a Golden Spike to bind the item to it. The actual function varies from type to type:
## Straight & Slope
Use the bound Golden Spike on the ground will set the track length to the larger absolute difference between the x or z coordinates of the Golden Rail Spike and the track placement position, rounded to the nearest whole number and then increased by 1.

## Turn
Use the bound Golden Spike on the ground will set the radius of the turn to the result of following algorithm:
* Take the average of the x-coordinate difference and z-coordinate difference between the Golden Rail Spike and the track placement position;
* Then increased by 1
* Then multiplied by 90 and devide by the turn's degree
* Then rounded to the nearest whole number as the result.

## Custom Curve
[OBJECT PROMISE]

## Switch
The straight section will just like the Straight type[TODO WRONG], and turning section will be like the Custom Curve part.

## Turntable
[OBJECT PROMISE]
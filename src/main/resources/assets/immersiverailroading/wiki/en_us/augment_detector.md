The Detector Augment is displayed as red block under rail and is used to check the locomotive/stock overhead's properties.

Right-click on the augment redstone torch/redstone dust/piston in your main hand will switch it between a few modes:
* Stock: Outputs a signal of 15 if there's a stock on the detector, otherwise outputs no signal.
* Speed: Outputs a signal equal to the minimum value of (speed(kph) divided by 10) rounded down and 15.
* Number of passengers: Outputs a signal with strength equal to the minimum value of the number of passengers on the stock and 15.
* Freight Cargo Fullness: Outputs a signal with strength proportional to the ratio of non-empty item slots to total item slots of the stock on the detector.
* Liquid Cargo Fullness: Outputs a signal with strength proportional to the ratio of current liquid amount to the max amount of liquid the stock on the detector can hold.

## Examples:
A plain detector:

![Plain](immersiverailroading:wiki/images/detector1.png)

A liquid detector:

![Liquid](immersiverailroading:wiki/images/detector2.png)
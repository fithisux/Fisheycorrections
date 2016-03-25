# Fisheycorrections

Co-developed with Sotiris. Thank you :-)


Experiment with equisolid or equidistant corrections.

I created this java classes to experiment with possible distortion corrections on Go Pro hero 4 cameras.

You can find an example in testCorrection. There, you just give a bmp to the software, it attempts to remove fishey distortion
(equisolid or equidistant currently). 

As a test case, I give some possible parameters for Go Pro cameras, found through experimentation and googgling (not in 35mm format but in straight mm).

The distortion corrected image and its accompanying mask are the result and a cropped image having the maximum rectangle of the center of the corrected image, since as you can see the image is not rectangular.

Tested on Java 8 and Netbeans 8+.

Based on Altera's **A Flexible Architecture for Fisheye Correction in Automotive
Rear-View Cameras**.

Enjoy.


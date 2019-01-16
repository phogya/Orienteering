# Orienteering

[Orienteering on wikipedia](https://en.wikipedia.org/wiki/Orienteering)

A java program written for Intro to Intelligent Systems to find the shortest path along some given course using an A* search algorithm. Takes several input files detailing terrain, elevation, and course requirements as well as a season argument and generates a path through the course. The course file contains the checkpoints the path must go through. The terrain file is an image and the color of each pixel represents the type of terrain, swamp, road, dense forest, etc. The given season argument will effect some types of terrain, for example in winter the edges of bodies of water become walkable because they freeze. The elevation file is an array of values for the elevation at each pixel.

The program can be run by navigating to the src folder and compiling with:

	javac Orienteering.java
	
and running with:

	java Orienteering terrain.png mpp.txt course.txt season
	
The season argument should be replaced with a lower case season, summer, fall, winter, or spring. The other arguments are files containing information about the map and may be changed if other similar files are available.

This will output two files, path.png and directions.txt. The path file is an image of a path along the terrain.png image, the path is a red line. The directions file is text directions corresponding to that same path.

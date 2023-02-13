# Gravity/Elastic Collision Simulation Engine

This is a small personnal project I started to get a handle on collision and gravity simulations in 3D spaces.

The inspiration was taken from [The Coding Train](https://www.youtube.com/@TheCodingTrain)'s episode on [Mutual Attraction](https://www.youtube.com/watch?v=GjbKsOkN1Oc) though I chose to increase the complexity by bringing the project into the third dimension.

> Note: This sketch was made using Processing 4, I cannot guarantee that it will work in other versions.

## What is Processing?

Processing is an open-source project created by the Processing Foundation allowing for the creation of simple 2D and 3D visual applications, as well as many other features. The default programming language is Java, but processing also exists in JavaScript, R as well as Python.

More information [here](https://processing.org/overview).

## Installation

You can download Processing 4 from [the official website](https://processing.org/) as either a standalone or complete installation (all major OS supported).

## How to use

To start the sketch, simply press the play button in the processing window. You don't have to control anything, the disposition of the spheres is randomly generated. If you want to customize the amount of generated spheres, you can edit the SPHERE_COUNT with any value you wish to try (that is above one).

Move the camera using left-click, rotate using right-click, zoom using mouse wheel and reset camera with any double-click.

Further customization will have to wait until I get around to implement them.

Commands:
- Click the tickboxes to enable/disable features.
- Press 'r' to restart the simulation.
- Press 'p' to pause/unpause the simulation.
- Press 'h' to toggle hide the interface.

## Detail

#### Explanation

The simulation creates a number of spheres (stored in the SPHERE_COUNT variable), which all have a random position, velocity, weight, radius and color.

Spheres get attracted to other spheres according to their respective masses, and collisions are both elastic and (supposedly) realistic.

Due to the limitations of Processing, I decided to sacrifice some efficiency by dampening gravitational movements when objects get attracted too close to one another.

#### Notes about uses
- The sketch uses concurrency to be able to deal with large amounts of particles.
- The UI elements help visualizing how the collisions happen, but don't have any bearing on the physics (except for the gravity button).
- Processing has notorious issues with displaying fonts, which is why I have an imported font in the data folder. If the font is blurry despite my fix, try restarting the sketch.
- Thread management is done using the native Java implementation, which procludes the use of a mutex for managing collision indexes (hence the clumsy attempt at exclusive implementation).

## How I made it

#### Sources
Math for the collision was adapted from: 
- [Momentum conservation in angle collisions between two spherical bodies](https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html)
- [Elastic collision and exchange of momentum between two bodies with different masses](https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution)

Math for the gravitational pull was taken from [Wikipedia](https://en.wikipedia.org/wiki/Gravitational_acceleration).

> Notes: I am notoriously terrible at math, and this (among other things) is an attempt to get a grasp on notions I never managed to properly understand in my youth. It is very possible that mistakes linger in the computational part of the code, and I would be thankful for any feedback about it.
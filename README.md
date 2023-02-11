# Gravity/Elastic Collision Simulation engine

This is a personnal project to get a handle on collision and gravity simulations in 3D space. The inspiration was taken from [The Coding Train](https://www.youtube.com/@TheCodingTrain)'s episode on [Mutual Attraction](https://www.youtube.com/watch?v=GjbKsOkN1Oc).

Made using Processing 4.

## What is Processing?

Processing is an open-source project created by the Processing Foundation allowing for the creation of simple 2D and 3D visual applications, as well as many other features. The default programming language is Java, but processing also exists in JavaScript, R as well as Python. More information [here](https://processing.org/overview).

## Installation

If you don't already have processing on your system, start by [downloading it on the website](https://processing.org/).

## How to use

To start the sketch, simply press the play button in the processing window. You don't have to control anything, the disposition of the balls is randomly generated.

Commands:
- Click the tickboxes to enable/disable features.
- Press 'r' to restart the simulation.
- Press 'p' to pause/unpause the simulation.
- Press 'h' to toggle hide the interface.

## Explanation

The simulation creates a number of spheres (stored in the SPHERE_COUNT variable), which all have a random position, velocity, weight, radius and color.

Spheres get attracted to other spheres according to their respective masses, and collisions are both elastic and (supposedly) realistic.

Do note that by default, a large amount of spheres will clump together due to natural gravitational pull, which results in some basic safety features to trigger to prevent them from clipping into one another.

Due to the limitations of Processing, I decided to sacrifice some efficiency by dampening gravitational movements when objects get attracted too close to one another.

Notes:
- The sketch uses concurrency to be able to deal with large amounts of particles.
- The UI elements help visualizing how the collisions happen, but don't have any bearing on the physics (except for the gravity button).
- Processing has notorious issues with displaying fonts, which is why I have an imported font in the data folder. If the font is blurry despite my fix, try restarting the sketch.

## How I made it

Math for the collision was adapted from: 
- [Momentum conservation in angle collisions between two spherical bodies](https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html)
- [Elastic collision and exchange of momentum between two bodies with different masses](https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution)

Math for the gravitational pull was taken from [Wikipedia](https://en.wikipedia.org/wiki/Gravitational_acceleration).

Thread management is done using the native Java implementation, which procludes the use of a mutex for managing collision indexes (hence the clumsy attempt at exclusive implementation).

## Upcoming features 

I will try to add more control over the simulation, including but not limited to the following:
- Control of sphere generation
- Sphere creation (with ajustable sliders for customization)
- Support for massless spheres (photons, basically)
- Global physics tweaking (gravity constant, speed)
- Cleaner interface

I will also refactor the code to manage the interface in a cleaner way as well as separate the classes that don't need to be inside of the main file into other files.
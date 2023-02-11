# Gravity/Elastic Collision Simulation Engine

This is a small personnal project I started to get a handle on collision and gravity simulations in 3D spaces.

The inspiration was taken from [The Coding Train](https://www.youtube.com/@TheCodingTrain)'s episode on [Mutual Attraction](https://www.youtube.com/watch?v=GjbKsOkN1Oc).

> Note: This sketch was made using Processing 4, I cannot guarantee that it will work in other versions.

## What is Processing?

Processing is an open-source project created by the Processing Foundation allowing for the creation of simple 2D and 3D visual applications, as well as many other features. The default programming language is Java, but processing also exists in JavaScript, R as well as Python.

More information [here](https://processing.org/overview).

## Installation

You can download Processing 4 from [the official website](https://processing.org/) as either a standalone or complete installation (all major OS supported).

## How to use

To start the sketch, simply press the play button in the processing window. You don't have to control anything, the disposition of the spheres is randomly generated. If you want to customize the amount of generated spheres, you can edit the SPHERE_COUNT with any value you wish to try (that is above one).
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

## How I made it

#### Sources
Math for the collision was adapted from: 
- [Momentum conservation in angle collisions between two spherical bodies](https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html)
- [Elastic collision and exchange of momentum between two bodies with different masses](https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution)

Math for the gravitational pull was taken from [Wikipedia](https://en.wikipedia.org/wiki/Gravitational_acceleration).

> Notes: I am notoriously terrible at math, and this (among other things) is an attempt to get a grasp on notions I never managed to properly understand in my youth. It is very possible that mistakes linger in the computational part of the code, and I would be thankful for any feedback about it.

#### Notes
Thread management is done using the native Java implementation, which procludes the use of a mutex for managing collision indexes (hence the clumsy attempt at exclusive implementation).

## Bugs

- Simulation stops: 
- Sphere agglutination: I am aware that of the "clumping effect" the spheres go through when the simulation runs for a while. I do not have an adequate way to fix this yet, and I believe the behavior might be linked to some loss of kinetic energy due to incorrect distribution of mass. Feel free to reach out if you spot the source of this issue.
    - Tangently, spheres bouncing off of these clumps will get a part of their velocity cancelled because of the way I implemented the collision part of the algorithm. I am aware of the issue and I believe I may have a solution, but I am not entirely sure how I will go about it yet.
- Clipping: As much as I tried to correct the issue, some clipping still occurs in various settings
- Unexpectedly fast speeds: spheres will rarely gain a wrong amount of velocity after a collision with another due to the accumulation of the anti-clipping algorithm and the distributed velocity, this can snowball and send the balls flinging in several circumstances. I could limit the maximum speed, but I would like to try and keep the possibility for spheres to get slung fast in drastic weight differences.
- Spheres "stuck together": spheres will sometimes stick to another and they will start building speed really fast until they hit something. This is not really something I want to delve into, as it involve minute adjustment on the math parts and only happens when spheres collide at *very specific* angles.
- Spheres phase through walls: haven't seen that one in quite some time, so I believe the issue is fixed for the moment.


## Upcoming features 

I will try to add more control over the simulation, including but not limited to the following:
- Control of sphere generation
- Sphere creation (with ajustable sliders for customization)
- Support for massless spheres (photons, basically)
- Global physics tweaking (gravity constant, speed)
- Cleaner interface

I will also refactor the code to manage the interface in a cleaner way as well as separate the classes that don't need to be inside of the main file into other files.

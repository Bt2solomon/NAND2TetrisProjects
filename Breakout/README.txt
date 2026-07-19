NAND2Breakout!
by Brady Solomon
Created for Project 9 of NAND2Tetris Part 2 on Coursera
The course, textbook, Jack language and compiler, and web IDE are courtesy of Shimon Schocken and Noam Nisan.

This project is my implementation of the classic arcade game Breakout,
in which you bounce a ball off a paddle into rows of blocks to destroy them.



Functionality: 
(note: The basic info necessary to play is displayed in the program. 
This is just a more in depth explanation of the game's mechanics.)

The game starts automatically when running the program. 
To play, move the paddle with the left and right arrow keys. 
When you're ready, press the up arrow key to release the ball from the paddle.

The ball will bounce off the walls and ceiling of the play area, 
as well as off of the blocks and paddle.
Bouncing the ball off a block destroys it and gives you 5 points.
The ball will bounce simply off of walls and blocks, only changing
the direction of its horizontal or vertical movement.
When bouncing off the paddle, the ball will move at a faster horizontal 
speed the closer it is to the edges of the paddle when it bounces.
It also always bounces left when hitting the left side of the paddle
and right when hitting the right side, rather than maintaining its 
horizontal speed.
You lose a point every time you bounce the ball off the paddle, to 
incentivise accurate aiming and efficient block destruction.

When you start the game, you have 3 lives. If the ball touches 
the bottom of the play area (below the paddle) then you lose 
a life and 10 points. If you lose all your lives, the game ends.
If you manage to destroy all the blocks before running out of lives,
you win!
The game can be paused at any time by pressing 'p' or quit by pressing 'q'.
You can also restart after winning or losing a game by pressing 'r'.



Behind the scenes:
The program is broken into six files, each containing one Jack class: 
Main: starts the game and then checks if the user wants to restart unless they quit.
Breakout: runs the game, handling a lot of the interactions between other classes and their objects.
Paddle: creates and controls the paddle.
Ball: creates a ball and handles its movement and collisions.
Block: represents a single block to be broken by the ball.
BlockGroup: creates a grid of Blocks and helps handle collisions with said blocks.

Each of the Jack files is thoroughly documented with both JavaDoc-style documentation of
each class and method, and frequent inline comments to describe the functionality within each method.

This program is entirely my own creation (excluding the Jack standard library APIs, of course). 
It may share similarities with the Pong project included in NAND2Tetris as an example; 
while the functionality of the two games is similar, I DID NOT base any of my implementation on its contents. 

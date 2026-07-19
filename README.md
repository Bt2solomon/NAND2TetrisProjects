# NAND2TetrisProjects
A collection of projects completed for the NAND2Tetris online course by Noam Nisan and Shimon Schocken.<br>
https://www.nand2tetris.org/<br>
https://www.coursera.org/learn/nand2tetris<br>
https://www.coursera.org/learn/nand2tetris2<br>
<h1>Overview</h1>
  "Build a Modern Computer from First Principles: Nand To Tetris" (NAND2Tetris for short) is an online course created by
Noam Nisan and Shimon Schocken. The course guides learners how computer hardware is designed from simple logic gates to 
having a CPU and memory, then shows how a high-level programming language can be created and how it translates back to 
machine-readable binary. The course provides a web IDE (https://nand2tetris.github.io/web-ide/) that includes a hardware 
simulator, CPU emulator, assembler, VM emulator, and Jack compiler. For more information on how to operate each feature 
of the web IDE, click the 'Guide' button (the book icon) on that page of the IDE.
<br><br>
Important terms used in the projects in this directory include:
  <ul>
    <li>Hack computer: The computer designed in the hardware simulator.</li>
    <li>Hack (language): The binary language that runs on the Hack computer. Uses the file extension .hack and can be 
run in the CPU Emulator.</li>
    <li>Assembly (language): An assembly language that translates into Hack. Uses the file extension .asm and can be 
loaded in the CPU Emulator. Detailed information on the assembly language can 
be found in <a href="https://b1391bd6-da3d-477d-8c01-38cdf774495a.filesusr.com/ugd/44046b_7ef1c00a714c46768f08c459a6cab45a.pdf">Section 4.2</a> 
of the textbook. </li>
    <li>VM code (language): A mid-level stack-based language, conceptually equivalent to Java's JVM bytecode. Uses the 
file extension .vm and can be loaded in the VM Emulator. Textbook chapters relevant to Project 7 and beyond are not 
freely available, but details on the VM language can be found in the 
<a href="https://drive.google.com/file/d/1BPmhMLu_4QTcte0I5bK4QBHI8SACnQSt/view">Chapter 7</a> and 
<a href="https://drive.google.com/file/d/1BexrNmdqYhKPkqD_Y81qNAUeyfzl-ZtO/view">Chapter 8</a> slides. Most useful are 
slides 32 and 37 in Chapter 7, and slides 7, 8, 10, 15, 16, and 17 in Chapter 8.</li>
    <li>Jack (language): A simple high-level object-oriented language based on Java that compiles into VM language. 
Uses the file extension .jack and can be loaded in the Jack Compiler. Details on the Jack specification can be found in 
the <a href="https://drive.google.com/file/d/1CAGF8d3pDIOgqX8NZGzU34PPEzvfTYrk/view">Chapter 9</a> slides, especially slides 102-123.</li>
    <li>Jack OS: A collection of Jack files containing the standard Jack library and code necessary for running Jack 
programs. The API for the Jack OS can be found 
<a href="https://drive.google.com/file/d/1MGofsorKBabyFo1Ppn3-fYpO3g3otE4q/view">here</a>.</li>
  </ul>
This repository contains a selection of projects I completed for the course, each detailed below and found in their 
respective directories. 

<h1>Projects</h1>
Project details, lecture slides, and some textbook chapters (from <i>The Elements of Computing Principles</i> by Noam 
Nisan and Shimon Schocken) mentioned in the following project descriptions can be found at https://www.nand2tetris.org/course 

<h2>Hack Assembler (Project 6)</h2>
<p>
Project 6, detailed <a href="https://drive.google.com/file/d/1CITliwTJzq19ibBF5EeuNBZ3MJ01dKoI/view">here</a>, was to 
build a program that could translate assembly code into Hack binary. I wrote the program in Java. After compiling, the 
program can be run in several ways. By running the program with no arguments, you will be prompted to enter the name of 
a single .asm file, which the program will attempt to find and translate. You may also run the program with one or more 
.asm files or directories as arguments. The program will attempt to translate the .asm files and all .asm files within 
the given directories.
</p>
<h2>VM Translator (Projects 7 and 8)</h2>
<p>
Projects 7 and 8, detailed <a href="https://drive.google.com/file/d/1DN5Gpjw6uJZuSvGBdXzwm-SHcBEn0PE-/view">here</a> 
and <a href="https://drive.google.com/file/d/1F2cYb2cIPFG0B_GybMcnNUPtc5mq8mHY/view">here</a>, were a two-part project 
to develop a program that could translate VM code into assembly. The program is written in Java and can be run in the 
same way as the Hack Assembler, except it requires arguments (.vm files or directories containing them) to be passed when running.
</p>
<h2>Jack Program: Breakout (Project 9)</h2>
<p>
Project 9, detailed <a href="https://drive.google.com/file/d/1O0lZ3oXHhcMrKJJ_byCfz-6Wjgtf7n6q/view">here</a>, was to 
write a program in Jack to gain familiarity with the language before building a compiler for it. For my project, I built 
a version of the computer game Breakthrough. Details on the implementation can be found in a separate README in the 
project's directory. The project was written in Jack and can be run on the web IDE.
</p>
<h2>Jack Compiler (Projects 10 and 11)</h2>
<p>
Projects 10 and 11, detailed <a href="https://drive.google.com/file/d/1O1nTS24VM2kp_ilTZCrBZOryhTK1e0qN/view">here</a> 
and <a href="https://drive.google.com/file/d/1O-129lGOVNQ8XU7J4z0SGgbp7gPUv0sj/view">here</a>, were a two-part project 
to develop a Jack Compiler capable of parsing Jack code and translating it into VM code. My Jack Compiler is written in 
Java and run similarly to the VM Translator, except with .jack files.
</p>
<h2>Jack OS (Project 12)</h2>
<p>
Project 12, detailed <a href="https://drive.google.com/file/d/1Qeuor0zqUAR0Q6xGPCuwdfYDAQILjbEm/view">here</a>, was to 
write my own alternative to the built-in Jack OS. The project is written in Jack and does nothing on its own, but can be 
included when running other Jack programs to override the default Jack OS.
</p>

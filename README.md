# evoUG2 Repository ReadMe

Use config.csv to assign values to parameters.

Some columns in config.csv act as parameter groups. 
Some parameters require additional values in order 
to function. For example, if you are using the 
square grid space, you must also provide the length 
and width of the grid.

Configuration parameters / parameter groups:
- ``game``: what game will be played **(required)**: accepted values:
  - ``UG``
  - ``DG``
  - ``PD``
- ``runs``: number of times the experiment will be run (integer number) **(required)**
- ``gens``: number of generations to occur per experiment run (integer number) **(required)**
- ``space``: what topology will be used **(required)**: accepted values:
  - ``grid``: if so, provide values for:
    1. ``length``: length of grid (integer number)
    2. ``width``: width of grid (integer number)
- ``neigh``: how neighbourhood will be initialised **(required)** : broken down into:
  - ``neighType``: what type of neighbourhood will be enforced: accepted values:
    - ``VN``: von Neumann
    - ``M``: Moore
    - ``dia``: diamond
    - If ``VN`` or ``M`` or ``dia``, provide value for:
      - ``neighRadius``: radius of neighbourhood
    - ``random``: if so, provide value for:
      - ``neighSize``: number of neighbours in a neighbourhood
- ``EM``: evolution mechanism **(required)**: accepted values:
  - ``ER``: evolution rate: if so, provide value for:
    - ``ER``: number of iterations of play and EWL to occur before evolution (integer number)
  - ``MC``: monte carlo method: if so, provide value for:
    - ``NIS``: number of inner steps per generation / monte carlo step: accepted values:
      - ``N``
      - integer number
- ``EW``: edge weight **(required)**: broken down into:
  - ``EWT``: edge weight type: accepted values:
    - ``proposalProb``
    - ``payoffPercent``
    - ``rewire``: if so, provide values for:
      1. ``RP``: rewire probability (rational number)
      2. ``RA``: rewire away: accepted values:
         - ``0Single``
         - ``0Many``
         - ``linear``
         - ``FD``
         - ``exponential``
         - ``smoothstep``
      - ``RT``: rewire to: accepted values:
        - ``local``
        - ``pop``
- ``EWL``: edge weight learning: broken down into:
  - ``EWLF``: edge weight learning formula: accepted values:
    - ``ROC``: if so, provide value for:
      - ``ROC``: rate of change (rational number)
  - ``EWLP``: edge weight learning probability: accepted values:
    - ``always``
    - ``UFD``: utility Fermi-Dirac
    - ``PFD``: proposal value Fermi-Dirac
- ``sel``: selection **(required)**: accepted values:
  - ``RW``: roulette wheel: if so, provide value for:
    - ``RWT``: roulette wheel type: accepted values:
      - ``normal``
      - ``exponential``
  - ``fittest``
  - ``intensity``
  - ``crossover``
  - ``randomNeigh``
  - ``randomPop``
- ``evo``: evolution **(required)**: accepted values:
  - ``copy``
  - ``approach``
- ``mut``: mutation: accepted values:
  - ``local``: if so, provide values for:
    1. ``mutRate``: mutation rate (rational number)
    2. ``mutBound``: mutation bound (rational number)
  - ``global``: if so, provide value for:
    - ``mutRate``: mutation rate
- ``UF``: utility formula **(required)**: accepted values:
  - ``MNI``: minimum number of interactions
  - ``cumulative``
  - ``normalised``
- ``dataRate``: rate at which data is recorded
- ``series``: broken down into:
  - ``varying``: name of parameter to be varied
  - ``variation``: amount by which parameter will be varied
  - ``numExp``: number of experiments with varied parameter
- ``inj``: broken down into:
  - ``injIter``
  - ``injP``
  - ``injSize``
- ``desc``: description of configuration



---

TESTING MARKDOWN

First Term
: This is the definition of the first term.

Second Term
: This is one definition of the second term.
: This is another definition of the second term.

game
: what game will be played
: required
: accepted values:
- ``UG``
- ``DG``
- ``PD``

Second Term
: This is one definition of the second term.
: This is another definition of the second term.

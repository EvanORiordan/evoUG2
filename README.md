# evoUG2 Repository ReadMe

Application for dictator game environment simulator. To run application, run Env.java. To configure parameters, 
edit config.csv. 

Simulator can run for generations, runs, experiments or series.
- ``a`` generations per run.
- ``b`` runs per experiment.
- ``c`` experiments per series.

Compute \(f(x) = x^2 + 2\) if \(x=2\).

$$
x = \frac { - b \pm \sqrt { b ^ { 2 } - 4 a c } } { 2 a }
$$

\begin{equation*}
l ( \theta ) = \sum _ { i = 1 } ^ { m } \log p ( x , \theta )
\end{equation*}

Parameters:
- ``runs``
  1. number of runs (integer)
- ``space``
  1. length of pop grid (integer)
- ``EM`` 
  1. evolution mechanism (``oldER``, ``newER``, ``MC``)
  2. evolution rate (integer)
  3. number of generations (integer)
- ``EWT`` 
  1. edge weight type (``proposalProb``, ``rewire``)
    - if ``rewire``
      1. rewire probability parameter ($x \in [0.0,1.0]$)
      2. rewire-away function
      3. rewire-to function
- ``EWL``
  1. edge weight learning function
    - if ``PROC`` or ``UROC``
      1. rate of change parameter
- ``sel``
  1. selection function
    - if ``RW``
      1. roulette wheel type
         - if ``exponential``
           1. noise
- ``mut``
  1. mutation function
  - if not null
    1. mutation rate
       - if ``local``
         1. mutation bound
- ``UF``
  1. utility function
- ``writing``
  1. writing booleans (format: abcdef (each char is a boolean))
     1. write p gen stats
     2. write u gen stats
     3. write deg gen stats
     4. write p run stats
     5. write u run stats
     6. write deg run stats
  2. writing rate (write stats every x gens)
- ``series``
  1. varying parameter
  2. values of varying parameter in subsequent experiments (format for e.g. 4 additional experiments: a b c d)

# evoUG2 Repository ReadMe


Use config.csv to customise the values of the environmental parameters.


Parameters that must be initialised:
- `game`
- `runs`
- `iters`
- `width`
- ``length``
- `EWT`
- `ER`
- `neigh`
- `sel`
- `ASD`
- `evo`


[//]: # (Accepted `neigh` parameter values:)

[//]: # (- `VN x`)

[//]: # (  - x denotes Manhattan distance of von Neumann neighbourhood)

[//]: # (- `Moore x`)

[//]: # (  - x denotes Chebyshev distance of Moore neighbourhood)

[//]: # (- `random x y`)

[//]: # (  - x = `uni` or `bi` denotes whether edges are uni or bi-directional)

[//]: # (  - y denotes neighbourhood size)

[//]: # (- `all`)

[//]: # (- ``dia x``)

[//]: # (  - x denotes distance of diamond neighbourhood)


Accepted ``neigh`` parameter values:
- ``VN a``
- ``Moore a`` where a denotes range
- ``dia a`` where a denotes range
- ``random a`` where a denote size




Accepted `varying` parameter values:
- `runs`
- `iters`
- `rows`
- `ER`
- `ROC`
- `leeway1`
- `leeway2`
- `leeway3`
- `leeway4`
- `leeway5`
- `leeway6`
- `leeway7`
- `selnoise`
- `evonoise`
- `mutrate`
- `mutamount`


Accepted `EWLC` parameter values:
- `p`
  - compare proposal values
- `avgscore`
  - compare average scores
- `AB`
  - compare alpha-beta rating
- `q`
  - compare acceptance thresholds


Accepted `EWLF` parameter values:
- `ROC`
  - apply constant learning
- `pAD`
  - absolute difference between ``p`` of players ``x`` and ``y``
    - |px - py|
- `pEAD`
  - exponential absolute diff between p
    - e^|px-py|
- `avgscoreAD`
  - diff between avg score
    - |scorex - scorey|
- `avgscoreEAD`
  - exponential diff between avg score
    - e^|scorex - scorey|
- `pAD2`
  - quadratic diff between p
    - |px - py|^2
- `pAD3`
  - cubic diff between p
    - |px - py|^3
- `AB`
  - alpha-beta rating
    - |(apx + bscorex / a + b) - (apy + bscorey / a + b)|


Accepted `sel` parameter values:
- ``RW``
  - roulette wheel
- ``elitist``
  - select fittest neighbour
- ``rand``
  - selection based on _Rand et al., 2013_
- ``crossover``


Accepted `evo` parameter values:
- ``copy``
- ``approach``
- ``crossover``


Accepted 'EWT' parameter values:
- ``proposalProb``
- ``payoffPercent``
- ``rewire``

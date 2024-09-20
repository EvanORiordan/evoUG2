# evoUG2 Repository ReadMe


Use config.csv to customise the values of the environmental parameters.


Parameters that must be initialised:
- _game_
- _runs_
- _iters_
- _rows_
- _EWT_
- _ER_
- _neigh_
- _sel_
- _ASD_
- _evo_


Acceptable _neigh_ parameter values:
- "VN" (von neumann)
- "M" (moore)
- "random" (4 random neighbours)
- "all" (all players are neighbours)
- "VN2"
- "VN3"


Acceptable _varying_ parameter values:
- "runs"
- "iters"
- "rows"
- "ER"
- "ROC"
- "leeway1"
- "leeway2"
- "leeway3"
- "leeway4"
- "leeway5"
- "leeway6"
- "leeway7"
- "selnoise"
- "evonoise"
- "mutrate"
- "mutamount"


Acceptable _EWLC_ parameter values:
- "p" (compare proposal values)
- "avgscore" (compare average scores)
- "AB" (compare alpha-beta rating)
- "q" (compare acceptance thresholds)


Acceptable _EWLF_ parameter values:
- "ROC" (apply constant learning)
- "pAD" (difference between p (|px - py|))
- "pEAD" (exponential diff between p (e^|px-py|))
- "avgscoreAD" (diff between avg score (|scorex - scorey|))
- "avgscoreEAD" (exponential diff between avg score (e^|scorex - scorey|))
- "pAD2" (quadratic diff between p (|px - py|^2))
- "pAD3" (cubic diff between p (|px - py|^3))
- "AB" (alpha-beta rating (|(apx + bscorex / a + b) - (apy + bscorey / a + b)|))


Acceptable _sel_ parameter values:
- "RW" (roulette wheel)
- "best" (select best)
- "rand" (selection based on Rand et al., 2013)
- "crossover"


Acceptable _evo_ parameter values:
- "copy"
- "approach"
- "crossover"



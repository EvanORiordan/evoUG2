# Dictator Game Environment Simulator

To run application, execute Env.java.
Environment is defined by parameters.
Select a configuration of parameters by typing the number of the configuration in the table into the console.

Simulator can execute generations of a population, as well as multiple runs, experiments and series.
- $\alpha$ generations per run.
    - Population evolves as the generations of the run pass.
    - Measure $\overline{p}$.
- $\beta$ runs per experiment.
    - Unique population for every run of the experiment.
    - Measure $\overline{\overline{p}}$.
- $\gamma$ experiments per series.
    - Vary the value of one parameter across the series of experiments.
        - Then you can compare $\overline{\overline{p}}$ of the different experiments.

config.csv stores configurations of parameters.
Edit the file to change the values of parameters.
Some parameters do not have an effect unless another parameter has a certain value.
E.g. ``RP`` depends on ``EWT``: it has no effect unless ``EWT`` is set to ``rewire``.
If an unexpected value is passed to a parameter, once the configuration is selected from the table, the application will terminate.

List of parameters and acceptable values:
1. ``runs``: number of runs (acceptable values: $x \in \{1, 2, 3, ..., \infty)$)
2. ``length``: length (and width) of pop (square) grid ($x \in \{3, 4, 5, ..., \infty)$)
3. ``ER``: evolution rate ($x \in \{1, 2, 3, ..., gens)$)
4. ``gens``: number of generations ($x \in \{1, 2, 3, ..., \infty)$)
5. ``EWT``: edge weight type (``proposalProb``, ``rewire``)
    - if ``rewire``
        1. ``RP``: rewire probability parameter ($x \in [0.0,1.0]$)
        2. ``RA``: rewire-away function (``smoothstep``,``smootherstep``,``0Many``, ``linear``)
        3. ``RT``: rewire-to function (``local``, ``pop``)
6. ``EWLF``: edge weight learning formula (``PROC``, ``PD``, ``UROC``, ``UD``)
    - if ``PROC`` or ``UROC``
        1. ``ROC``: rate of change ($x \in [0.0,1.0]$)
7. ``sel``: selection function (``RW``, ``fittest``, ``randomNeigh``, ``randomPop``)
    - if ``RW``
        1. ``RWT``: roulette wheel type (``normal``, ``exponential``)
            - if ``exponential``
                1. ``selNoise``: selection noise ($x \in [0.0, \infty)$)
8. ``mut``: mutation function (null, ``global``, ``local``)
    - if ``global`` or ``local``
        1. ``mutRate``: mutation rate ($x \in [0.0, \infty)$)
        - if ``local``
            1. ``mutBound``: mutation bound ($x \in [0.0, \infty)$)
        2. ``selfMut``: allow self mutation (``0`` for false, ``1`` for true)
9. ``UF``: utility function (``cumulative``, ``normalised``)
10. ``WPGS``: write p gen stats (``0`` for false, ``1`` for true)
11. ``WUGS``: write u gen stats (``0`` for false, ``1`` for true)
12. ``WDGS``: write deg gen stats (``0`` for false, ``1`` for true)
13. ``WPRS``: write p gen stats (``0`` for false, ``1`` for true)
14. ``WURS``: write u gen stats (``0`` for false, ``1`` for true)
15. ``WDRS``: write deg gen stats (``0`` for false, ``1`` for true)
    - if any write $x$ parameter enabled:
        1. ``writeRate``: write stats every $x$ gens ($x \in [1, gens]$)
16. ``varying``: varying parameter (null, ``ER``, ``ROC``, ``length``, ``RP``, ``gens``, ``EWLF``, ``EWT``, ``RA``, ``RT``, ``sel``, ``selNoise``, ``mutRate``, ``mutBound``, ``UF``)
    - if not null:
        1. ``variations``: sequence of variations to varying parameter, separated by semicolons (;)
            - e.g. ``ER`` set to ``1``, ``varying`` set to ``ER``, variations set to ``2;3;4;5``
            - e.g. ``RA`` set to ``smoothstep``, ``varying`` set to ``RA``, variations set to ``smootherstep;linear``

Enable statistic writing parameters to record data.
- gen stats: record data of individual players
  1. $p$
  2. $\overline{p}_\Omega$
  3. $u$
  4. $deg$
- run stats: record aggregate data of population
  1. $\overline{p}$
  2. $\overline{u}$
  3. $\overline{deg}$
  4. $\sigma_p$
  6. $\sigma_{deg}}$
  7. $p_{max}$
  8. $\overline{\overline{p}}$
  9. $\sigma_{\overline{p}}$
  10. $\overline{\overline{u}}$
  11. $\overline{\sigma_{deg}}$
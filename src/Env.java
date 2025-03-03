import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * School of Computer Science<br>
 * University of Galway<br>
 */
public class Env extends Thread{ // environment simulator
    static String game; // indicates what game is being played
    static double M = 1.0; // default prize amount during a UG/DG
    static String space; // indicates what kind of space the population will reside within
    static int length; // length of space; in 2D grid, len = num rows
    static int width; // width of space; in 2D grid, wid = num cols i.e. num players per row
    static String neighType; // indicates type of neighbourhood players will have.
    static int neighRadius; // radius of neighbourhood
    static int neighSize; // size of neighbours with random neighbourhood type.
    static int N; // population size
//    static int run_num; // current run
    static int run; // current run
    static int runs; // number of experiment runs to occur
    Player[] pop; // array of players; assumes 2D space
    double mean_p; // mean proposal value; informally referred to as the performance of an experiment
    double mean_q; // mean acceptance threshold
    double mean_u; // mean utility
    double mean_degree; // mean degree of population
    double sigma_p; // standard deviation of p
    double sigma_q; // standard deviation of q
    double sigma_u; // standard deviation of utility
    double sigma_degree; // standard deviation of degree
    double p_max; // highest p in population at a time
    int gen = 1; // current generation
    static int gens; // number of generations to occur per experiment run
    static int iters; // temp var: number of iterations of play and EWL
    static String UF; // utility formula: indicates how utility is calculated
    static double T; // PD: temptation to defect
    static double R; // PD: reward for mutual coopeation
    static double P; // PD: punishment for mutual defection
    static double S; // PD: sucker's payoff for cooperating with a defector
    static double l; // loner's payoff
    static String varying = ""; // indicates which parameter will be varied in experiment series
    static ArrayList<String> str_variations = new ArrayList<>();
    static int expNum; // indicates how far along we are through the experiment series
    static double mean_mean_p; // mean of the mean p of the runs of an experiment
    static double mean_mean_q;
    static double mean_mean_u;
    static double mean_mean_degree;
    static double sigma_mean_p; // standard deviation of the mean p of the runs of an experiment
    static double sigma_mean_q;
    static double sigma_mean_u;
    static double sigma_mean_degree;
    static double mean_sigma_degree; // mean of the standard deviations of degree of the runs of an experiment
    static double[] mean_p_values; // mean p values of the runs of an experiment
    static double[] mean_q_values;
    static double[] mean_u_values;
    static double[] mean_degree_values;
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static LocalDateTime old_timestamp; // timestamp of end of previous experiment in series
    static String project_path = Paths.get("").toAbsolutePath().toString();
    static String general_path = project_path + "\\csv_data"; // address where all data is recorded
    static String this_path; // address where results of current experimentation is recorded
    static String experiment_path; // address where results of current experiment are stored
    static boolean writePPop;
    static boolean writeUPop;
    static boolean writeDegPop;
    static boolean writePStats;
    static boolean writeUStats;
    static boolean writeDegStats;
    static String pos_data_filename;
    static String EWT; // EW type
    static String EWLF; // EWL formula
    static double ROC = 0; // rate of change: fixed learning amount to EW
    static double alpha = 0; // used in alpha-beta rating
    static double beta = 0; // used in alpha-beta rating
    static String EWLP; // EWL probability
    static String evo; // indicates which evolution function to call
    static double evoNoise = 0; // noise affecting evolution
    static String sel; // indicates which selection function to call
    static double selNoise = 0.0; // noise affecting selection
    static String mut; // indicates which mutation function to call
    static double mutRate = 0.0; // probability of mutation
    static double mutBound = 0.0; // denotes max mutation possible
    static String EM; // evolution mechanism: the mechanism by which evolution occurs.
    static int ER = 0; // evolution rate: used in various ways to denote how often generations occur
    static int NIS = 0; // num inner steps: number of inner steps per generation using the monte carlo method; usually is set to value of N
    static String RWT; // roulette wheel type
    static String RA = ""; // rewire away
    static String RT = ""; // rewire to
    static double RP = 0.0; // rewire probability
    static int injIter; // injection iteration: indicates when strategy injection will occur. 0 ==> no injection.
    static double injP = 0.0; // injection p: indicates p value to be injected
    static int injSize = 0; // injection cluster size: indicates size of cluster to be injected




    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        configureEnvironment();
        LocalDateTime start_timestamp = LocalDateTime.now(); // timestamp of start of experimentation
        old_timestamp = start_timestamp;
        String start_timestamp_string = start_timestamp.getYear()
                +"-"+start_timestamp.getMonthValue()
                +"-"+start_timestamp.getDayOfMonth()
                +"_"+start_timestamp.getHour()
                +"-"+start_timestamp.getMinute()
                +"-"+start_timestamp.getSecond();
//        this_path = general_path+"\\"+start_timestamp_string+" "+desc;
        this_path = general_path+"\\"+start_timestamp_string;
        try {
            Files.createDirectories(Paths.get(this_path)); // create results storage folder
        }catch(IOException e){
            e.printStackTrace();
        }
        printPath();
        System.out.println("Starting timestamp: "+start_timestamp);
        experimentSeries();
        LocalDateTime finish_timestamp = LocalDateTime.now(); // marks the end of the main algorithm's runtime
        System.out.println("Finishing timestamp: "+finish_timestamp);
        Duration duration = Duration.between(start_timestamp, finish_timestamp);
        long secondsElapsed = duration.toSeconds();
        long minutesElapsed = duration.toMinutes();
        long hoursElapsed = duration.toHours();
        System.out.println("Time elapsed: "+hoursElapsed+" hours, "+minutesElapsed%60+" minutes, "+secondsElapsed%60+" seconds");
        printPath();
    }



    /**
     * Runs an experiment series. If varying parameter defined, vary it after each
     * subsequent experiment in the series.
     */
    public static void experimentSeries(){
        for(expNum = 1; expNum <= str_variations.size() + 1; expNum++){
            System.out.println("\nstart experiment " + expNum);
            experiment_path = this_path + "\\exp" + expNum;
//            if(dataRate != 0)
//                createDataFolders();
            createDataFolders();
            experiment(); // run an experiment of the series
            if(expNum <= str_variations.size()){ // do not try to vary after the last experiment has ended
                switch(varying){
                    case "EWLF" -> EWLF = str_variations.get(expNum - 1);
                    case "RA" -> RA = str_variations.get(expNum - 1);
                    case "RT" -> RT = str_variations.get(expNum - 1);
                    case "sel" -> sel = str_variations.get(expNum - 1);
                    case "evo" -> evo = str_variations.get(expNum - 1);
                    case "EWT" -> EWT = str_variations.get(expNum - 1);
                    case "gens" -> gens = Integer.parseInt(str_variations.get(expNum - 1));
                    case "length" -> length = Integer.parseInt(str_variations.get(expNum - 1));
                    case "width" -> width = Integer.parseInt(str_variations.get(expNum - 1));
                    case "ER" -> ER = Integer.parseInt(str_variations.get(expNum - 1));
//                    case "newER", "oldER" -> ER = Integer.parseInt(str_variations.get(expNum - 1));
                    case "NIS" -> NIS = Integer.parseInt(str_variations.get(expNum - 1));
                    case "ROC" -> ROC = Double.parseDouble(str_variations.get(expNum - 1));
                    case "RP" -> RP = Double.parseDouble(str_variations.get(expNum - 1));
                    case "M" -> M = Double.parseDouble(str_variations.get(expNum - 1));
                    case "selNoise" -> selNoise = Double.parseDouble(str_variations.get(expNum - 1));
                }
            }
        }
    }



    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){
        switch(game){
            case "UG" -> {
                mean_mean_p = 0;
                mean_p_values = new double[runs];
                sigma_mean_p = 0;
                mean_mean_q = 0;
                mean_q_values = new double[runs];
                sigma_mean_q = 0;
            }
            case "DG" -> {
                mean_mean_p = 0;
                mean_p_values = new double[runs];
                sigma_mean_p = 0;
            }
            case "PD" -> {}
        }
        mean_mean_u = 0.0;
        mean_u_values = new double[runs];
        sigma_mean_u = 0.0;
        mean_mean_degree = 0.0;
        mean_degree_values = new double[runs];
        sigma_mean_degree = 0.0;
        mean_sigma_degree = 0.0;
        for(run = 1; run <= runs; run++){
            Env pop = new Env();
            pop.start();


//            if(writeP){
//                writePRun();
//            }


            String output = "experiment "+expNum+" run "+run;
            switch(game){
                case "UG" -> {
                    mean_mean_p += pop.mean_p;
                    mean_p_values[run - 1] = pop.mean_p;
                    mean_mean_q += pop.mean_q;
                    mean_q_values[run - 1] = pop.mean_q;
                    output += " mean p=" + DF4.format(pop.mean_p);
                    output += " mean q="+DF4.format(pop.mean_q);
                }
                case "DG" -> {
                    mean_mean_p += pop.mean_p;
                    mean_p_values[run - 1] = pop.mean_p;
                    output += " mean p=" + DF4.format(pop.mean_p);
                }
                case "PD" -> {} // IDEA: if game is PD, then print number of cooperators, defectors and abstainers.
            }
            mean_mean_u += pop.mean_u;
            mean_u_values[run - 1] = pop.mean_u;
            mean_mean_degree += pop.mean_degree;
            mean_degree_values[run - 1] = pop.mean_degree;
            mean_sigma_degree += pop.sigma_degree;
            output += " mean u=" + DF4.format(pop.mean_u);
            System.out.println(output);
        }
        String output = "experiment " + expNum + ":";
        switch(game){
            case "UG" -> {
                mean_mean_p /= runs;
                mean_mean_q /= runs;
                for(int i = 0; i < runs; i++){
                    sigma_mean_p += Math.pow(mean_p_values[i] - mean_mean_p, 2);
                    sigma_mean_q += Math.pow(mean_q_values[i] - mean_mean_q, 2);
                }
                sigma_mean_p = Math.pow(sigma_mean_p / runs, 0.5);
                sigma_mean_q = Math.pow(sigma_mean_q / runs, 0.5);
                output += "\nmean mean p=" + DF4.format(mean_mean_p);
                output += "\nsigma mean p=" + DF4.format(sigma_mean_p);
                output += "\nmean mean q=" + DF4.format(mean_mean_q);
                output += "\nsigma mean q=" + DF4.format(sigma_mean_q);
            }
            case "DG" -> {
                mean_mean_p /= runs;
                for(int i = 0; i < runs; i++){
                    sigma_mean_p += Math.pow(mean_p_values[i] - mean_mean_p, 2);
                }
                sigma_mean_p = Math.pow(sigma_mean_p / runs, 0.5);
                output += "\nmean mean p=" + DF4.format(mean_mean_p);
                output += "\nsigma mean p=" + DF4.format(sigma_mean_p);
            }
            case "PD" -> {}
        }
        mean_mean_u /= runs;
        mean_mean_degree /= runs;
        for(int i = 0; i < runs; i++){
            sigma_mean_u += Math.pow(mean_u_values[i] - mean_mean_u, 2);
            sigma_mean_degree += Math.pow(mean_degree_values[i] - mean_mean_degree, 2);
        }
        sigma_mean_u = Math.pow(sigma_mean_u / runs, 0.5);
        sigma_mean_degree = Math.pow(sigma_mean_degree / runs, 0.5);
        mean_sigma_degree /= runs;
        output += "\nmean mean u=" + DF4.format(mean_mean_u);
        output += "\nsigma mean u=" + DF4.format(sigma_mean_u);
        output += "\nmean mean degree=" + DF4.format(mean_mean_degree);
        output += "\nsigma mean degree=" + DF4.format(sigma_mean_degree);
        output += "\nmean sigma degree=" + DF4.format(mean_sigma_degree);
        System.out.println(output);
        writeSettings();
        writeResults();
//        expNum++; // move on to the next experiment in the series
    }



    /**
     * Core algorithm of the program.
     * Initialise population.
     * Initialise neighbourhoods and weights.
     * Execute generations.
     */
    @Override
    public void start(){
        initRandomPop();

        for(int i=0;i<N;i++){
            switch(neighType){
                case"VN","Moore","dia"->assignAdjacentNeighbours(pop[i]);
                case"random"->assignRandomNeighbours(pop[i], neighSize);
                case"all"->assignAllNeighbours(pop[i]);
                default -> {
                    System.out.println("[ERROR] Invalid neighbourhood type configured. Exiting...");
                    Runtime.getRuntime().exit(0);
                }
            }
        }

//        if(space.equals("grid") && run == 1){
//            writePosData();
//        }

        for(int i=0;i<N;i++){
            initialiseEdgeWeights(pop[i]);
        }

        while(gen <= gens){
            switch(EM){
//                case "ER" -> {
                case "newER" -> {

                    // iterations of playing and edge weight learning
                    for(int j = 0; j < ER; j++){
                        for(int i = 0; i < N; i++){
                            play(pop[i]);
                        }
                        for(int i=0;i<N;i++){
                            updateUtility(pop[i]);
                        }
                        for(int i=0;i<N;i++){
                            EWL(pop[i]);
                        }
                    }

                    // rewire if applicable
                    if(EWT.equals("rewire")){
                        for(int i=0;i<N;i++){
                            rewire(pop[i]);
                        }
                    }

                    // evolution
                    for(int i=0;i<N;i++) {
                        Player child = pop[i];
                        if(child.getNeighbourhood().size() > 0){ // prevent evolution if child is isolated.


//                            Player parent = null;
//                            switch(sel){
//                                case "RW" -> parent = selRW(child);
//                                case "fittest" -> parent = selFittest(child);
////                                case "intensity" -> parent = selIntensity(child);
//                                case "crossover" -> crossover(child);
//                                case "randomNeigh" -> parent = selRandomNeigh(child);
//                                case "randomPop" -> parent = selRandomPop();
//                            }


                            Player parent = sel(child);


//                            switch (evo) {
//                                case "copy" -> evoCopy(child, parent);
//                                case "approach" -> evoApproach(child, parent);
//                                case "copyFitter" -> evoCopyFitter(child, parent);
//                            }


                            evo(child, parent);


//                            switch (mut){
//                                case "global" -> {
//                                    if(mutationCheck())
//                                        mutGlobal(child);
//                                }
//                                case "local" -> {
//                                    if(mutationCheck())
//                                        mutLocal(child);
//                                }
//                            }


                            mut(child);


                        }
                    }
                }
                case "MC" -> {

                    // all players play
                    for(int i = 0; i < N; i++){
                        play(pop[i]);
                    }
                    for(int i=0;i<N;i++){
                        updateUtility(pop[i]);
                    }

                    // randomly select a player NIS times to learn, rewire and evolve.
                    // evolution segment is inspired by cardinot2016optional.
                    for(int i = 0; i < NIS; i++){
                        Player player = selRandomPop();
                        EWL(player);
                        if(EWT.equals("rewire"))
                            rewire(player);
                        if(player.getNeighbourhood().size() > 0) {


//                            Player parent = null;
//                            switch(sel){
//                                case "RW" -> parent = selRW(player);
//                                case "fittest" -> parent = selFittest(player);
////                                case "intensity" -> parent = selIntensity(player);
//                                case "crossover" -> crossover(player);
//                                case "randomNeigh" -> parent = selRandomNeigh(player);
//                                case "randomPop" -> parent = selRandomPop();
//                            }
//                            switch (evo) {
//                                case "copy" -> evoCopy(player, parent);
//                                case "approach" -> evoApproach(player, parent);
//                                case "copyFitter" -> evoCopyFitter(player, parent);
//                            }
//                            switch (mut){
//                                case "global" -> {
//                                    if(mutationCheck())
//                                        mutGlobal(player);
//                                }
//                                case "local" -> {
//                                    if(mutationCheck())
//                                        mutLocal(player);
//                                }
//                            }


                            // TODO DEBUG ME
                            Player parent = sel(player);
                            evo(player, parent);
//                            mut()


                        }
                    }
                }

                case "oldER" -> { // for the program to function as if its using the old ER system, remember that ive added a bit later on that breaks the gen loop to essentially nullify it.
//                    int iters = 10000;
                    int iter = 1;
                    int gensOccurred = 0;
                    while(iter <= iters) {
                        for(int i=0;i<N;i++){
                            play(pop[i]);
                        }
                        for(int i=0;i<N;i++){
                            updateUtility(pop[i]);
                        }
                        for(int i=0;i<N;i++){
                            EWL(pop[i]);
                        }
                        if(iter % ER == 0){
                            for(int i=0;i<N;i++) {
                                Player child = pop[i];
                                if(child.getNeighbourhood().size() > 0) { // prevent evolution if child is isolated.
                                    Player parent = selRW(child);
                                    evoCopy(child, parent);
                                }
                            }
                            gensOccurred++;
                        }
                        prepare();
                        iter++; // move on to the next iteration
                    }
//                    System.out.println("gens occurred = " + gensOccurred);
                    gens = gensOccurred;
                }
            }

            // calculate stats
            switch(game){
                case "UG" -> {
                    calculateMeanP();
                    calculateStandardDeviationP();
                    calculateMeanQ();
                    calculateStandardDeviationQ();
                }
                case "DG" -> {
                    calculateMeanP();
                    calculateStandardDeviationP();
                    calculatePMax();
                }
                case "PD" -> {}
            }
            calculateMeanU();
            calculateStandardDeviationU();
            for(int i = 0; i < N; i++){
                pop[i].calculateDegree();
            }
            calculatemeanDegree();
            calculateStandardDeviationDegree();


            // this block of code is required for oldER EM to operate as intended.
            if(EM.equals("oldER"))
                break;


//            // save non-result and non-setting data every dataRate gens when:
//            // dataRate has been initialised past 0, AND
//            // it is the first run of the experiment, AND
//            //// it is the only or first experiment in the series, AND
//            // it is not the first generation of the run.
//            if(dataRate != 0 && run_num == 1 && gen % dataRate == 0){
//                switch(game){
//                    case "UG" -> {
//                        System.out.println("gen "+gen+" mean p="+DF4.format(mean_p)+" sigma p="+DF4.format(sigma_p)+" mean q="+DF4.format(mean_q)+" sigma q="+DF4.format(sigma_q));
//                        writePData();
//                        writeMacroPData();
//                        writeQData();
//                        writeMacroQData();
//                    }
//                    case "DG" -> {
//                        System.out.println("gen "+gen+" mean p="+DF4.format(mean_p)+" sigma p="+DF4.format(sigma_p));
//                        writePData();
//                        writeMacroPData();
//                    }
//                    case "PD" -> {}
//                }
//                writeUData();
//                writeMacroUData();
//                writeDegreeData();
//                writeMacroDegreeData();
//            }


            if(writePPop) writePPop();
            if(writePStats) writePStats();
            if(writeUPop) writeUPop();
            if(writeUStats) writeUStats();
            if(writeDegPop) writeDegPop();
            if(writeDegStats) writeDegStats();


            // progress to the next generation
            gen++;
            prepare();
        }


//        if(writeP){
//            writePRun();
//        }


        writeResultsExperiment();

    }



    /**
     * a initiates games with neighbours.
     * @param a player
     */
    public void play(Player a) {
        ArrayList<Player> omega_a = a.getNeighbourhood(); // neighbourhood of a
        for(int i = 0; i < omega_a.size(); i++){
            Player b = omega_a.get(i); // neighbour of a
            ArrayList <Player> omega_b = b.getNeighbourhood(); // neighbourhood of b
            for (int j = 0; j < omega_b.size(); j++) {
                Player c = omega_b.get(j); // neighbour of b
                if (a.equals(c)) {
                    ArrayList <Double> weights_b = b.getEdgeWeights(); // weights of b
                    double w_ba = weights_b.get(j); // weight of edge from b to a
                    switch(EWT){
                        case "proposalProb" -> {
                            double d = ThreadLocalRandom.current().nextDouble();
                            if(w_ba > d){
                                switch(game){
                                    case "UG", "DG" -> UG(a, b);
                                    case "PD" -> PD(a, b);
                                }
                            } else{
                                updateStats(a,0);
                                updateStats(b,0);
                            }
                        }
                        case "payoffPercent" -> {
                            switch(game){
                                case "UG", "DG" -> UG(a, b, w_ba);
                                case "PD" -> PD(a, b, w_ba);
                            }
                        }
                        default -> {
                            switch(game){
                                case "UG", "DG" -> UG(a, b);
                                case "PD" -> PD(a, b);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }



    /**
     * Proposer initiates ultimatum game interaction with responder.<br>
     * If p_a >= q_b, b accepts the proposal made by a, otherwise b rejects it.<br>
     * @param a proposer
     * @param b responder
     */
    public void UG(Player a, Player b){
        double p_a = a.getP();
        double q_b = b.getQ();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(p_a >= q_b){
            pi_b = M * p_a;
            pi_a = M - pi_b;
        }
        updateStatsUG(a, pi_a);
        updateStatsUG(b, pi_b);
    }
    /**
     * Proposer initiates ultimatum game interaction with responder.<br>
     * If p_a >= q_b, b accepts the proposal made by a, otherwise b rejects it.<br>
     * Uses w_ba to calculate payoffs of players.
     * Prize is effectively cut by w_ba percent.<br>
     * @param a proposer
     * @param b responder
     * @param w_ba weight of edge from b to a
     */
    public void UG(Player a, Player b, double w_ba){
        double p_a = a.getP();
        double q_b = b.getQ();
        double pi_a = 0.0;
        double pi_b = 0.0;
        double modified_M = M * w_ba; // denote formally by M'
        if(p_a >= q_b) {
            pi_b = modified_M * p_a;
            pi_a = modified_M - pi_b;
        }
        updateStatsUG(a, pi_a);
        updateStatsUG(b, pi_b);
    }




    public void updateStatsUG(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }





    /**
     * Prisoner's dilemma function.<br>
     * @param a player
     * @param b player
     */
    public void PD(Player a, Player b){
        String s_a = a.getStrategyPD();
        String s_b = b.getStrategyPD();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(s_a.equals("C") && s_b.equals("C")){
            pi_a = R;
            pi_b = R;
        } else if(s_a.equals("C") && s_b.equals("D")){
            pi_a = S;
            pi_b = T;
        }else if(s_a.equals("D") && s_b.equals("C")){
            pi_a = T;
            pi_b = S;
        }else if(s_a.equals("D") && s_b.equals("D")){
            pi_a = P;
            pi_b = P;
        }else if(s_a.equals("A") || s_b.equals("A")){
            pi_a = l;
            pi_b = l;
        }
        updateStatsPD(a, pi_a);
        updateStatsPD(b, pi_b);
    }
    /**
     * Prisoner's dilemma function.<br>
     * Uses w_ba to calculate payoffs of players.<br>
     * @param a player
     * @param b player
     */
    public void PD(Player a, Player b, double w_ba){
        String s_a = a.getStrategyPD();
        String s_b = b.getStrategyPD();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(s_a.equals("C") && s_b.equals("C")){
            pi_a = R * w_ba;
            pi_b = R * w_ba;
        } else if(s_a.equals("C") && s_b.equals("D")){
            pi_a = S * w_ba;
            pi_b = T * w_ba;
        }else if(s_a.equals("D") && s_b.equals("C")){
            pi_a = T * w_ba;
            pi_b = S * w_ba;
        }else if(s_a.equals("D") && s_b.equals("D")){
            pi_a = P * w_ba;
            pi_b = P * w_ba;
        }else if(s_a.equals("A") || s_b.equals("A")){
            pi_a = l * w_ba;
            pi_b = l * w_ba;
        }
        updateStatsPD(a, pi_a);
        updateStatsPD(b, pi_b);
    }



    public void updateStatsPD(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }



    public void initialiseEdgeWeights(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        ArrayList <Double> edge_weights = new ArrayList<>();
        int size = neighbourhood.size();
        for(int i=0;i<size;i++){
            edge_weights.add(1.0);
        }
        player.setEdgeWeights(edge_weights);
    }




    /**
     * player performs edge weight learning (EWL) with all of its edges.
     * @param a player performing EWL
     */
    public void EWL(Player a){
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Player> omega_a = a.getNeighbourhood();
        for(int i = 0; i < omega_a.size(); i++){

            // b denotes neighbour at end of edge
            Player b = omega_a.get(i);

            // w_ab denotes weight of edge from a to b. technically this is a copy of the player's weight; note how this is a double while player's weight is a Double.
            double w_ab = weights.get(i);

            // e indicates whether w_ab will undergo learning
//            boolean e = checkEWLP(a, b);


            boolean e = true;


            if(e){
                w_ab += calculateLearning(a, b);
                if(w_ab > 1.0)
                    w_ab = 1.0;
                else if(w_ab < 0.0)
                    w_ab = 0.0;
                weights.set(i, w_ab); // set player's weight to copy.
            }
        }
    }



    public boolean checkEWLP(Player a, Player b){
        // manually set noise k.
        double k = 0.1;

        double x = 0.0;
        switch(EWLP){
            case "UFD" -> x = 1 / (1 + Math.exp((a.getU() - b.getU()) / k)); // utility fermi-dirac
            case "PFD" -> x = 1 / (1 + Math.exp((a.getP() - b.getP()) / k)); // proposal value fermi-dirac
            case "always" -> x = 1.0;
        }

//        System.out.println(a.getP() + "\t" + b.getP() + "\t" + x);
//        System.out.println(a.getU() + "\t" + b.getU() + "\t" + x);

        double y = ThreadLocalRandom.current().nextDouble();

        boolean z = x > y;

        return z;
    }





    public double calculateLearning(Player a, Player b){
        double learning = 0.0;
        switch(EWLF){
//            case "ROC" -> learning = ROC;
            case "ROC" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                if(p_a < p_b) // if b fairer than a, increase weight
                    learning = ROC;
                else if(p_a > p_b) // else if a fairer than b, decrease weight
                    learning = -ROC;
            }
            case "PD" -> learning = b.getP() - a.getP();
            case "PED" -> learning = Math.exp(b.getP() - a.getP());
            case "UD" -> learning = b.getU() - a.getU();
            case "UED" -> learning = Math.exp(b.getU() - a.getU());
//            case "PDR" -> {
//                double p_a = a.getP();
//                double p_b = b.getP();
//                if(p_a < p_b){ // if b fairer than a, a wants to increase w_ab
//                    learning = ThreadLocalRandom.current().nextDouble(0, p_b - p_a);
//                } else if(p_a > p_b){ // if a is fairer than b, a wants to decrease w_ab
//                    learning = ThreadLocalRandom.current().nextDouble(p_b - p_a, 0);
//                }
//            }
            case "PDR" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                double diff = p_b - p_a;
                if(p_a < p_b){
                    learning = ThreadLocalRandom.current().nextDouble(0, diff);
                } else if(p_a > p_b){
                    learning = -ThreadLocalRandom.current().nextDouble(0, -(diff)); // the minuses work around exceptions. ultimately, learning will be assigned a negative value.
                }
            }
        }
        return learning;
    }





    /**
     * Roulette wheel (RW) selection function where the fitter a candidate, the more likely
     * it is to be selected as parent. Candidates including child and neighbours.
     * @param child player undergoing roulette wheel selection
     */
    public Player selRW(Player child){
        Player parent = null;
        ArrayList <Player> pool = new ArrayList<>(child.getNeighbourhood()); // pool of candidates for parent
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        for(int i = 0; i < size; i++){
            switch(RWT){
                case "normal" -> pockets[i] = pool.get(i).getU();
                case "exponential" -> pockets[i] = Math.exp(pool.get(i).getU() * selNoise);
            }
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
        for(int i = 0; i < size; i++){
            tally += pockets[i];
            double percent_taken = tally / roulette_total; // how much space in the wheel has been taken up so far
            if(random_double < percent_taken){ // if true, the ball landed in the candidate's slot
                parent = pool.get(i); // select candidate as parent
                break;
            }
        }
        return parent;
    }



    /**
     * child selects fittest player in neighbourhood as parent unless there does not
     * exist fitter neighbour than child.
     * @param a child
     * @return parent
      */
    public Player selFittest(Player a){
        ArrayList<Player> omega_a = a.getNeighbourhood(); // denotes neighbourhood of child
        Player b = a; // denotes parent
        for(int i = 0; i < omega_a.size(); i++){
            Player c = omega_a.get(i); // denotes neighbour of child; candidate for parent
            if(c.getU() > b.getU()){ // if candidate fitter than parent, parent is set to candidate
                b = c;
            }
        }
        return b;
    }



//    /**
//     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
//     * The greater w (intensity of selection) is,
//     * the more likely a fitter player is selected as child's parent.
//     * @param child
//     * @return parent player
//     */
////    public Player selRand(Player child){
//    public Player selIntensity(Player child){
//        double w = selNoise;
//        double[] effective_payoffs = new double[N];
//        Player parent = null;
//        double total = 0.0;
//        double tally = 0.0;
//
//        // calculate effective payoffs
//        for(int i=0;i<N;i++){
//            Player player = pop[i];
//            double u = player.getU();
//            double effective_payoff = Math.exp(w * u);
//            effective_payoffs[i] = effective_payoff;
//            total += effective_payoff;
//        }
//
//        // fitter player ==> more likely to be selected
//        double random_double = ThreadLocalRandom.current().nextDouble();
//        for(int i = 0; i < N; i++){
//            tally += effective_payoffs[i];
//            double percentile = tally / total;
//            if(random_double < percentile){
//                parent = pop[i];
//                break;
//            }
//        }
//
//        return parent;
//    }



    /**
     * sel and evo effectively occur at once in one function.
     * crossover where one child adopts midway point between two parent strategies.
     *
     * @param child
     */
    public void crossover(Player child){

        // how to select parents?

        // select two fittest neighbours?
        ArrayList <Player> neighbourhood = child.getNeighbourhood();
        Player parent1 = child; // fittest neighbour
        Player parent2 = child; // second-fittest neighbour
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double neighbour_u = neighbour.getU();
            double parent2_u = parent2.getU();
            if(neighbour_u > parent2_u){
                parent2 = neighbourhood.get(i);
                parent2_u = parent2.getU();
                double parent1_mean_score = parent1.getU();
                if(parent2_u > parent1_mean_score){
                    Player temp = parent1;
                    parent1 = parent2;
                    parent2 = temp;
                }
            }
        }

        double p1 = parent1.getP();
        double p2 = parent2.getP();
        double new_p = (p1 + p2) / 2;
        child.setP(new_p);
    }




    /**
     * Evolution method where child wholly copies parent's strategy.
     * @param parent is the parent the player is copying.
     */
    public void evoCopy(Player child, Player parent){
        switch(game){
            case "UG" -> {
//                setStrategy(child, parent.getOldP(), parent.getOldQ());
                child.setP(parent.getOldP());
                child.setQ(parent.getOldQ());
            }
            case "DG" -> child.setP(parent.getOldP());
            case "PD" -> {}
        }
    }



    /**
     * Use evo noise to move child strategy in direction of parent strategy.
     * @param child
     * @param parent
     */
    public void evoApproach(Player child, Player parent){
        int ID = child.getID();
        int parent_ID = parent.getID();
        double p = child.getP();
        double q = child.getQ();
        double parent_old_p = parent.getOldP();
        double parent_old_q = parent.getOldQ();

        // do not approach evolve if parent is child
        if(parent_ID != ID){

            // for attribute, if parent is lower, reduce child; else, increase.
            double approach = ThreadLocalRandom.current().nextDouble(evoNoise);
            if(parent_old_p < p){
                approach *= -1;
            }
            double new_p = p + approach;
            if(parent_old_q < q){
                approach *= -1;
            }
            double new_q = q + approach;

//            setStrategy(child, new_p, new_q);
            child.setP(new_p);
            child.setQ(new_q);
        }
    }



    /**
     * Mutation rate parameter determines the probability for mutation to occur.
     * @return boolean indicating whether mutation will occur
     */
    public boolean mutationCheck(){
        double random_double = ThreadLocalRandom.current().nextDouble();
        boolean mutation = random_double < mutRate;

        return mutation;
    }



    /**
     * Child's attributes are randomly and independently generated.
     */
    public void mutGlobal(Player child){
        double new_p = ThreadLocalRandom.current().nextDouble();
        double new_q = ThreadLocalRandom.current().nextDouble();
        child.setP(new_p);
        child.setQ(new_q);
    }



    /**
     * Slight mutations are independently applied to child's attributes.
     */
    public void mutLocal(Player child){
        double p = child.getP();
        double q = child.getQ();
        double new_p = ThreadLocalRandom.current().nextDouble(p - mutBound, p + mutBound);
        double new_q = ThreadLocalRandom.current().nextDouble(q - mutBound, q + mutBound);
        child.setP(new_p);
        child.setQ(new_q);
    }



    public void calculateMeanEdgeWeights(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            calculateMeanSelfEdgeWeight(player);
            calculateMeanNeighbourEdgeWeight(player);
        }
    }



    public void calculateMeanSelfEdgeWeight(Player player){
        ArrayList <Double> weights = player.getEdgeWeights();
        int size = weights.size();
        double mean_self_edge_weight = 0;

        // calculate mean of edge weights from player to its neighbours
        for(int i = 0; i < size; i++){
            mean_self_edge_weight += weights.get(i);
        }
        mean_self_edge_weight /= size;

        player.setMeanSelfEdgeWeight(mean_self_edge_weight);
    }



    public void calculateMeanNeighbourEdgeWeight(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int size = neighbourhood.size();
        double mean_neighbour_edge_weight = 0;

        // calculate mean of edge weights directed at player
        for(int i = 0; i < size; i++) {
            Player neighbour = neighbourhood.get(i);
            ArrayList <Double> weights = neighbour.getEdgeWeights();
            int index = findPlayerIndex(player, neighbour);
            mean_neighbour_edge_weight += weights.get(index);
        }
        mean_neighbour_edge_weight /= size;

        player.setMeanNeighbourEdgeWeight(mean_neighbour_edge_weight);
    }



    /**
     * Assume num player neighbours = num neighbour neighbours.<br>
     * Return index corresponding to player within neighbours neighbourhood.
     */
    public int findPlayerIndex(Player player, Player neighbour){
        int index = 0;
        int ID = player.getID();
        ArrayList <Player> neighbourhood = neighbour.getNeighbourhood();
        int size = neighbourhood.size();

        for (int i = 0; i < size; i++) {
            Player neighbour2 = neighbourhood.get(i);
            int ID2 = neighbour2.getID();
            if (ID == ID2) {
                index = i;
                break;
            }
        }

        return index;
    }



    /**
     * Loads in a configuration of settings from the config file, allowing the user to choose the values of the environmental parameters.
     */
    public static void configureEnvironment(){
        // load configurations
        ArrayList<String> configurations = new ArrayList<>(); // stores configs
        try{
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialise String to store rows of data
            br.readLine(); // purposely ignore the row of headings
            while((line = br.readLine()) != null){
                configurations.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        // display intro and config table headings
        System.out.printf("=========================================%n");
        System.out.printf("   Evolutionary Game Theory Simulator%n");
        System.out.printf("   By Evan O'Riordan%n");
        printTableBorder();
        System.out.printf("%-10s |" +//config
                        " %-10s |" +//game
                        " %-10s |" +//runs
                        " %-10s |" +//gens
                        " %-15s |" +//space
                        " %-15s |" +//neigh
                        " %-15s |" +//EM
                        " %-30s |" +//EW
                        " %-25s |" +//EWL
                        " %-20s |" +//sel
                        " %-15s |" +//evo
                        " %-15s |" +//mut
                        " %-10s |" +//UF
                        " %-10s |" +//writing
                        " %s%n"//series
//                        " %-15s |" +//inj
                ,"config"
                ,"game"
                ,"runs"
                ,"gens"
                ,"space"
                ,"neigh"
                ,"EM"
                ,"EW"
                ,"EWL"
                ,"sel"
                ,"evo"
                ,"mut"
                ,"UF"
                ,"writing"
                ,"series"
        );
        printTableBorder();

        // display config table rows
        int CI; // configuration index
        String[] settings;
        for(int i=0;i<configurations.size();i++){
            settings = configurations.get(i).split(",");
            CI = 0; // reset to 0 for each config
            System.out.printf("%-10d ", i); //config
            System.out.printf("| %-10s ", settings[CI++]); //game
            System.out.printf("| %-10s ", settings[CI++]); //runs
            System.out.printf("| %-10s ", settings[CI++]); //gens
            System.out.printf("| %-15s ", settings[CI++]); //space
            System.out.printf("| %-15s ", settings[CI++]); //neigh
            System.out.printf("| %-15s ", settings[CI++]); //EM
            System.out.printf("| %-30s ", settings[CI++]); //EW
            System.out.printf("| %-25s ", settings[CI++]); //EWL
            System.out.printf("| %-20s ", settings[CI++]); //sel
            System.out.printf("| %-15s ", settings[CI++]); //evo
            /*
            this try catch template serves to handle ArrayIndexOutOfBoundsException.
            use it whenever printing a param / param group where the param / the first param
            in the group is unrequired.

            i came up with it because settings[] wouldnt have an entry for
            unrequired params that arent eventually followed up by a required param.
            specifically, i was trying to print dataRate (unrequired) but since there were no more
            required params coming anytime afterward, there was no "" entry in
            settings corresponding to dataRate hence ArrayIndexOutOfBoundsException would occur.
             */
            try{
                System.out.printf("| %-15s ", settings[CI++]);//mut
            }catch(ArrayIndexOutOfBoundsException e){
                System.out.printf("| %-15s ", " ");
                CI++;
            }
            System.out.printf("| %-10s ", settings[CI++]); //UF
            System.out.printf("| %-10s ", settings[CI++]); //writing


            try{
                System.out.printf("| %s ", settings[CI++]);//series
            }catch(ArrayIndexOutOfBoundsException e){
                System.out.printf("| %s ", " ");
                CI++;
            }
            System.out.println();
        }
        printTableBorder();

        // ask user which config they wish to use
        System.out.println("Which config would you like to use? (int)");
        boolean config_selected = false;
        int config_num;
        do{ // ensure user selects valid config
            config_num = scanner.nextInt();
            if(0 <= config_num && config_num < configurations.size()){
                config_selected = true;
            } else{
                System.out.println("ERROR: invalid config number, try again");
            }
        }while(!config_selected);


        // apply config
        settings = configurations.get(config_num).split(",");
        CI = 0;
        int CI2;


        String[] game_params = settings[CI++].split(" ");
        CI2 = 0;
        game = game_params[CI2++];
        Player.setGame(game);
//        switch(game){
//            case "UG", "DG" -> M = Double.parseDouble(game_params[CI2++]);
//        }
        runs = Integer.parseInt(settings[CI++]);
        gens = Integer.parseInt(settings[CI++]);
        if(gens < 1){
            System.out.println("ERROR: cannot have gens < 1");
            System.exit(0);
        }


        String[] space_params = settings[CI++].split(" "); // space parameters
        CI2 = 0;
        space = space_params[CI2++];
        if(space.equals("grid")){
            length = Integer.parseInt(space_params[CI2++]);
            width = Integer.parseInt(space_params[CI2++]);
            N = length * width;
        }


        String[] neigh_params = settings[CI++].split(" "); // neighbourhood parameters
        CI2 = 0;
        neighType = neigh_params[CI2++]; // required field
        if(neighType.equals("VN") || neighType.equals("Moore") || neighType.equals("dia")){
            neighRadius = Integer.parseInt(neigh_params[CI2++]);
        } else if(neighType.equals("random")){
            neighSize = Integer.parseInt(neigh_params[CI2++]);
        }


        String[] EM_params = settings[CI++].split(" "); // evolution mechanism parameters
        CI2 = 0;
        EM = EM_params[CI2++];
        switch(EM){
            case "newER" -> ER = Integer.parseInt(EM_params[CI2++]);
            case "oldER" -> {
                ER = Integer.parseInt(EM_params[CI2++]);
                iters = Integer.parseInt(EM_params[CI2++]);
            }
            case "MC" -> NIS = Integer.parseInt(EM_params[CI2]);
        }


        String[] EWT_params = settings[CI++].split(" "); // edge weight parameters
        CI2 = 0;
        EWT = EWT_params[CI2++];
        if(EWT.equals("rewire")){
            RP = Double.parseDouble(EWT_params[CI2++]);
            RA = EWT_params[CI2++];
            RT = EWT_params[CI2++];
        }


        String[] EWL_params = settings[CI++].split(" "); // edge weight learning parameters
        if(!EWL_params[0].equals("")){
            CI2 = 0;
            EWLF = EWL_params[CI2++];
            if(EWLF.equals("ROC"))
                ROC = Double.parseDouble(EWL_params[CI2++]);
            if(EWLF.equals("AB")){
                alpha = Double.parseDouble(EWL_params[CI2++]);
                beta = Double.parseDouble(EWL_params[CI2++]);
            }
            // tentatively removed EWLP functionality.
//            EWLP = EWL_params[CI2++];
        }


        String[] sel_params = settings[CI++].split(" "); // selection parameters
        CI2 = 0;
        sel = sel_params[CI2++];


        if(sel.equals("RW")){
            RWT = sel_params[CI2++];
            if(RWT.equals("exponential")){
                selNoise = Double.parseDouble(sel_params[CI2++]);
            }
        }


        String[] evo_params = settings[CI++].split(" "); // evolution parameters
        CI2 = 0;
        evo = evo_params[CI2++];
        if(evo.equals("approach")){
            evoNoise = Double.parseDouble(evo_params[CI2++]);
        }


        String[] mut_params = settings[CI++].split(" "); // mutation parameters
        CI2 = 0;
        mut = mut_params[CI2++];
        if(!mut_params[0].equals("")){
            mutRate = Double.parseDouble(mut_params[CI2++]);
            if(mut.equals("local")){
                mutBound = Double.parseDouble(mut_params[CI2++]);
            }
        }


        UF = settings[CI++];


        // experiment run data writing params
        String write_params = settings[CI++];
        writePPop = write_params.charAt(0) == '1'? true: false;
        writePStats = write_params.charAt(1) == '1'? true: false;
        writeUPop = write_params.charAt(2) == '1'? true: false;
        writeUStats = write_params.charAt(3) == '1'? true: false;
        writeDegPop = write_params.charAt(4) == '1'? true: false;
        writeDegStats = write_params.charAt(5) == '1'? true: false;


        try{
            String[] series_params = settings[CI++].split(" ");
            CI2 = 0;
            varying = series_params[CI2++];
            for(int i = 1; i < series_params.length; i++)
                str_variations.add(series_params[i]);
        }catch(ArrayIndexOutOfBoundsException e){}


        // tentatively removed injection functionality.
//        String[] inj_params = settings[CI++].split(" "); // injection parameters
//        if(!inj_params[0].equals("")){
//            CI2 = 0;
//            injIter = Integer.parseInt(inj_params[CI2++]);
//            injP = Double.parseDouble(inj_params[CI2++]);
//            injSize = Integer.parseInt(inj_params[CI2++]);
//        }
    }



    /**
     * Initialises a lattice grid population of players with randomly generated strategies.
     */
    public void initRandomPop(){
        pop = new Player[N];
        Player.setCount(0);
        int index = 0;
        switch(space){
            case "grid" -> {
                for(int y=0;y<length;y++){
                    for(int x=0;x<width;x++){
                        Player new_player = null;
                        switch(game){
                            case "UG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                double q = ThreadLocalRandom.current().nextDouble();
                                new_player = new Player(x, y, p, q);
                            }
                            case "DG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                new_player = new Player(x, y, p, 0.0);
                            }
                            case "PD" -> {}
                        }
                        pop[index] = new_player;
                        index++;
                    }
                }
            }
            case "hex" -> {}
        }
    }



    /**
     * Prints the border of the table of configurations.
     */
    public static void printTableBorder(){
        System.out.printf(
                "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "%n");
    }



    public static void createDataFolders(){
        try{
            Files.createDirectories(Paths.get(experiment_path));
            for(int i=1;i<=runs;i++){
                if(writePPop || writeUPop || writeDegPop) Files.createDirectories(Paths.get(experiment_path + "\\run" + i));
                if(writePPop) Files.createDirectories(Paths.get(experiment_path + "\\run" + i + "\\p_pop"));
                if(writeUPop) Files.createDirectories(Paths.get(experiment_path + "\\run" + i + "\\u_pop"));
                if(writeDegPop) Files.createDirectories(Paths.get(experiment_path + "\\run" + i + "\\deg_pop"));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Prints path of experiment results folder.
     */
    public static void printPath(){
        System.out.println("Address of experimentation data: \n" + this_path);
    }



    /**
     * Assigns adjacent neighbour to player's neighbourhood.<br>
     * d denotes Manhattan distance for von Neumann neighbourhood or Chebyshev distance
     * for Moore neighbourhood.
    */
    public void assignAdjacentNeighbours(Player player){
        ArrayList<Player> neighbourhood = new ArrayList<>();
        double y = player.getY();
        double x = player.getX();
        for(int i=1;i<=neighRadius;i++){
            double x_plus = adjustPosition(x, i, width);
            double x_minus = adjustPosition(x, -i, width);
            double y_plus = adjustPosition(y, i, length);
            double y_minus = adjustPosition(y, -i, length);
            neighbourhood.add(findPlayerByPos(y,x_plus));
            neighbourhood.add(findPlayerByPos(y,x_minus));
            neighbourhood.add(findPlayerByPos(y_plus,x));
            neighbourhood.add(findPlayerByPos(y_minus,x));
            if(neighType.equals("dia")) {
                if(i > 1) {
                    double x_plus_minus = adjustPosition(x_plus, -1.0, width);
                    double x_minus_plus = adjustPosition(x_minus, 1.0, width);
                    double y_plus_minus = adjustPosition(y_plus, -1.0, length);
                    double y_minus_plus = adjustPosition(y_minus, 1.0, length);
                    neighbourhood.add(findPlayerByPos(y_plus_minus,(x_plus_minus)));
                    neighbourhood.add(findPlayerByPos(y_minus_plus,(x_plus_minus)));
                    neighbourhood.add(findPlayerByPos(y_minus_plus,(x_minus_plus)));
                    neighbourhood.add(findPlayerByPos(y_plus_minus,(x_minus_plus)));
                }
            }
            if(neighType.equals("Moore")){
                neighbourhood.add(findPlayerByPos(y_plus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_minus));
                neighbourhood.add(findPlayerByPos(y_plus,x_minus));
            }
        }
        player.setNeighbourhood(neighbourhood);
    }



    /**
     * Randomly assigns either uni-directional or bi-directional edges to player.<br>
     * Assumes 2D square lattice grid population structure.
     * @param player
     * @param size
     */
    public void assignRandomNeighbours(Player player, int size){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();
        Set<Integer> IDs = new HashSet<>();
        while(IDs.size() < size){
            int ID = ThreadLocalRandom.current().nextInt(N);
            IDs.add(ID);
        }
        for(int ID: IDs){
            neighbourhood.add(findPlayerByID(ID));
        }
    }



    // assign all other players to neighbourhood
    public void assignAllNeighbours(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int ID = player.getID();
        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID != ID2){
                neighbourhood.add(player2);
            }
        }
    }



    public void calculateMeanP(){
        mean_p = 0;
        for(int i = 0; i < N; i++){
            mean_p += pop[i].getP();
        }
        mean_p /= N;
    }

    public void calculateStandardDeviationP(){
        sigma_p = 0;
        for(int i = 0; i < N; i++){
            sigma_p += Math.pow(pop[i].getP() - mean_p, 2);
        }
        sigma_p = Math.pow(sigma_p / N, 0.5);
    }

    public void calculateMeanQ(){
        mean_q = 0;
        for(int i = 0; i < N; i++){
            mean_q += pop[i].getQ();
        }
        mean_q /= N;
    }

    public void calculateStandardDeviationQ(){
        sigma_q = 0;
        for(int i = 0; i < N; i++){
            sigma_q += Math.pow(pop[i].getQ() - mean_q, 2);
        }
        sigma_q = Math.pow(sigma_q / N, 0.5);
    }

    public void calculateMeanU(){
        mean_u = 0;
        for(int i = 0; i < N; i++){
            mean_u += pop[i].getU();
        }
        mean_u /= N;
    }

    public void calculateStandardDeviationU(){
        sigma_u = 0;
        for(int i = 0; i < N; i++){
            sigma_u += Math.pow(pop[i].getU() - mean_u, 2);
        }
        sigma_u = Math.pow(sigma_u / N, 0.5);
    }

    public void calculatemeanDegree(){
        mean_degree = 0;
        for(int i = 0; i < N; i++){
            mean_degree += pop[i].getDegree();
        }
        mean_degree /= N;
    }

    public void calculateStandardDeviationDegree(){
        sigma_degree = 0;
        for(int i = 0; i < N; i++){
            sigma_degree += Math.pow(pop[i].getDegree() - mean_degree, 2);
        }
        sigma_degree = Math.pow(sigma_degree / N, 0.5);
    }



    /**
     * Prepare program for the next generation.<br>
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void prepare(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            player.setPi(0);
            player.setMNI(0);
            switch(game){
                case "UG" -> {
                    player.setOldP(player.getP());
                    player.setOldQ(player.getQ());
                }
                case "DG" -> {
                    player.setOldP(player.getP());
                    p_max = 0.0;
                }
                case "PD" -> {}
            }
        }
    }



    public void writePStats(){
        String filename = experiment_path + "\\run" + run + "\\p_stats.csv";
        String s="";
        if(gen == 1){ // apply headings to file before writing data
            s+="gen";
            s+=",mean p";
            s+=",sigma p";
            s+=",p max";
            s+="\n";
        }
        s+=gen;
        s+=","+DF4.format(mean_p);
        s+=","+DF4.format(sigma_p);
        s+=","+DF4.format(p_max);
        s+="\n";
        try{
            fw = new FileWriter(filename, true);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    public void writeMacroQData(){
//        try{
//            String filename = experiment_path + "\\macro_" + qDataStr + ".csv";
//            String output = "";
//            if(gen == dataRate){
//                output += "gen";
//                output += ",mean q";
//                output += ",sigma q";
//                output += "\n";
//            }
//            fw = new FileWriter(filename, true);
//            output += gen;
//            output += ","+DF4.format(mean_q);
//            output += ","+DF4.format(sigma_q);
//            output += "\n";
//            fw.append(output);
//            fw.close();
//        } catch(IOException e){
//            e.printStackTrace();
//        }
    }



    // writes IDs and positions of players
    public void writePosData(){
        try{
//            String filename = this_path + "\\pos_data.csv";
            String filename = experiment_path + "\\pos_data.csv";
            fw = new FileWriter(filename, false);
            String s = "";
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    int ID = player.getID();
//                    s += ID + " ("+x+" "+y+")";
                    s += ID;
                    if (x + 1 < width)
                        s += ",";
                }
                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    // Records proposal values of the population to a .csv file.
    public void writePPop(){
        String filename = experiment_path + "\\run" + run + "\\p_pop\\gen" + gen + ".csv";
        String s = "";
        for(int y=length-1;y>=0;y--){
            for(int x=0;x<width;x++){
                Player player = findPlayerByPos(y,x);
                double p = player.getP();
                s += DF4.format(p);
                if(x + 1 < length){
                    s += ",";
                }
            }
            s += "\n";
        }
        try{
            fw = new FileWriter(filename, false);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }



    // Records acceptance thresholds of the population to a .csv file.
    public void writeQData(){
//        try{
//            String filename = experiment_path + "\\" + qDataStr + "\\gen" + gen + ".csv";
//            fw = new FileWriter(filename, false);
//            String output = "";
//            for(int y=length-1;y>=0;y--){
//                for(int x=0;x<width;x++){
//                    Player player = findPlayerByPos(y,x);
//                    double q = player.getQ();
//                    output += DF4.format(q) + ",";
//                }
//                output += "\n";
//            }
//            fw.append(output);
//            fw.close();
//        } catch(IOException e){
//            e.printStackTrace();
//        }
    }



    /**
     * Records aggregate EW data by writing a grid of 4x4 sub-grids.<br>
     * Assumes von Neumann, square grid.<br>
     * {@code EW_health} equals sum of weights divided by num weights.
     */
    public void writeEWData(){
//        try{
//            String filename = EW_data_filename + "\\gen" + gen + ".csv";
//            fw = new FileWriter(filename);
//            String string = "";
//            String[] substrings = new String[(length * 4)];
//            for(int i=0;i<substrings.length;i++){
//                substrings[i] = "";
//            }
//            int a=0;
//            for(int y=length-1;y>=0;y--){
//                for(int x=0;x<width;x++){
//                    Player current = findPlayerByPos(y,x);
//                    ArrayList <Double> weights = current.getEdgeWeights();
//                    ArrayList<Player> neighbourhood = current.getNeighbourhood();
//                    double EW_health = (current.getMeanSelfEdgeWeight() + current.getMeanNeighbourEdgeWeight()) / 2;
//                    substrings[a] += "0,"
//                            +weights.get(2)+","
//                            +neighbourhood.get(2).getEdgeWeights().get(3)+","
//                            +"0";
//                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights().get(0)+","
//                            +DF2.format(EW_health)+","
//                            +DF2.format(EW_health)+","
//                            +weights.get(0);
//                    substrings[a+2] += current.getEdgeWeights().get(1)+","
//                            +DF2.format(EW_health)+","
//                            +DF2.format(EW_health)+","
//                            +neighbourhood.get(0).getEdgeWeights().get(1);
//                    substrings[a+3] += "0,"
//                            +neighbourhood.get(3).getEdgeWeights().get(2)+","
//                            +weights.get(3)+","
//                            +"0";
//                    if(x + 1 < width){
//                        for(int b=a;b<a+4;b++){
//                            substrings[b] += ",";
//                        }
//                    } else {
//                        for(int b=a;b<a+4;b++){
//                            substrings[b] += "\n";
//                        }
//                    }
//                }
//                a += 4;
//            }
//            for(int i=0;i<substrings.length;i++){
//                string += substrings[i];
//            }
//            fw.append(string);
//            fw.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
    }



    /**
     * Uses NSI data to write a grid of 4x4 sub-grids into a .csv file.<br>
     * Assumes von Neumann neighbourhood type and rows=columns.
     */
    public void writeNSIData(){
//        try{
//            String filename = NSI_data_filename + "\\gen" + gen + ".csv";
//            fw = new FileWriter(filename);
//            String string = "";
////            String[] substrings = new String[(rows * 4)];
////            String[] substrings = new String[(width * 4)];
//            String[] substrings = new String[(length * 4)];
//            for(int i=0;i<substrings.length;i++){
//                substrings[i] = "";
//            }
//            int a=0;
////            for(int y = rows - 1; y >= 0; y--) {
////                for (int x = 0; x < columns; x++) {
////                    Player current = grid.get(y).get(x);
//
//
//            for(int y=length-1;y>=0;y--){
//                for(int x=0;x<width;x++){
//                    Player current = findPlayerByPos(y,x);
//
//
//                    int[] NSI_per_neighbour = current.getNSIPerNeighbour();
//
//
////                    TESTING TO SEE IF ITS EVER 0
////                    for(int i: NSI_per_neighbour){
////                        if(i == 0.0){
////                            System.out.println("hello"); // can place breakpoint here
////                        }
////                    }
//
//
//
//
//                    substrings[a] += "0,"
//                            +NSI_per_neighbour[2]+","
//                            +NSI_per_neighbour[2]+","
//                            +"0";
//                    substrings[a+1] += NSI_per_neighbour[1]+","
//                            +"0,"
//                            +"0,"
//                            +NSI_per_neighbour[0];
//                    substrings[a+2] += NSI_per_neighbour[1]+","
//                            +"0,"
//                            +"0,"
//                            +NSI_per_neighbour[0];
//                    substrings[a+3] += "0,"
//                            +NSI_per_neighbour[3]+","
//                            +NSI_per_neighbour[3]+","
//                            +"0";
////                    if(x + 1 < columns){
////                    if(x + 1 < length){
//                    if(x + 1 < width){
//                        for(int b=a;b<a+4;b++){
//                            substrings[b] += ",";
//                        }
//                    } else {
//                        for(int b=a;b<a+4;b++){
//                            substrings[b] += "\n";
//                        }
//                    }
//                }
//                a += 4;
//            }
//            for(int i=0;i<substrings.length;i++){
//                string += substrings[i];
//            }
//            fw.append(string);
//            fw.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
    }





    /**
     * Finds a player given the integer ID parameter.<br>
     * Currently not used.
     * @param ID of the player to find
     * @return player object with the given ID
     */
    public Player findPlayerByID(int ID){
        Player player = null;
        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID == ID2){
                player = player2;
                break;
            }
        }

        return player;
    }



    /**
     * find player by position in the grid.<br>
     * e.g. if you call findPlayerByPos(5, 2), it returns the player at position (2, 5).
     * @param y y co-ordinate of the player
     * @param x x co-ordinate of the player
     * @return Player object at position (x, y) in the grid.
     */
    public Player findPlayerByPos(double y, double x){
        Player player = null;
        boolean found = false;
        int i=0;
        do{
            Player player2=pop[i];
            double y2=player2.getY();
            double x2=player2.getX();
            if(y2==y && x2==x){
                player=player2;
                found=true;
            }
            i++;
        }
        while(!found);
        return player;
    }



    /**
     * Assign same strategy to cluster of players within the grid.
     */
    public void injectStrategyCluster(){
        for(int i = 0; i < injSize; i++){
            for(int j = 0; j < injSize; j++){
                Player player = findPlayerByPos(j, i);
                player.setP(injP);
            }
        }
    }



    // adjust position with respect to periodic boundaries
    public double adjustPosition(double position, double adjustment, int max){
        double new_position = (((position + adjustment) % max) + max) % max;
        return new_position;
    }



    /**
     * Find new neighbour by randomly choosing a neighbour of a neighbour.<br>
     * New neighbour cannot be rewirer or already a neighbour.<br>
     */
    public void RTLocal(Player a, int num_rewires){
        // pool of candidates the rewirer could rewire to to form a new edge.
        ArrayList<Player> pool = new ArrayList<>();

        // omega_a denotes neighbourhood of rewirer.
        ArrayList<Player> omega_a = a.getNeighbourhood();

        // b denotes neighbour of rewirer
        for(Player b: omega_a){

            // omega_b denotes neighbourhood of neighbour of rewirer.
            ArrayList<Player> omega_b = b.getNeighbourhood();

            // c denotes neighbour of neighbour of rewirer.
            for(Player c: omega_b){

                // do not add c to pool if c = a
                if(!c.equals(a)){

                    // boolean tracking whether c should be added to pool or not.
                    boolean add = true;

                    // d denotes candidate in pool
                    for (Player d : pool) {

                        // if c = d, c must already be in the pool, therefore do not add c to the pool.
                        if(c.equals(d)){
                            add = false;
                            break;
                        }
                    }

                    // move on to next c if this c has already been ruled out of contention.
                    if(!add){
                        continue;
                    }

                    // e denotes neighbour of rewirer.
                    for (Player e : omega_a) {

                        // if c = e, c must already be in omega_a, therefore do not add c to the pool.
                        if(c.equals(e)){
                            add = false;
                            break;
                        }
                    }

                    // if c has been deemed to be valid, add it to the pool.
                    if(add){
                        pool.add(c);
                    }
                }
            }
        }

        // if pool empty, default to rewiring to a random player in the pop.
        if(pool.size() == 0){
            RTPop(a, num_rewires);
        } else{ // connect to local player.
            for(int rewires_done = 0; rewires_done < num_rewires; rewires_done++){

                // f denotes new neighbour of a.
                Player f = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));

                // connect a to f.
                omega_a.add(f);
                a.getEdgeWeights().add(1.0);

                // connect f to a.
                f.getNeighbourhood().add(a);
                f.getEdgeWeights().add(1.0);
            }
        }
    }



    /**
     * Find new neighbour by randomly choosing a player from the population.<br>
     * New neighbour cannot be rewirer or already a neighbour.<br>
     * @param a rewirer
     * @param b number of rewires to do
     */
    public void RTPop(Player a, int b){
        // c denotes number of rewires done so far.
        for(int c=0;c<b;c++){

            // denotes neighbourhood of a.
            ArrayList<Player> omega_a = a.getNeighbourhood();

            // d denotes new neighbour.
            Player d = null;

            // e indicates: a valid new neighbour d has been found.
            boolean e = false;
            while(!e){

                // randomly choose player from the pop.
                d = pop[ThreadLocalRandom.current().nextInt(pop.length)];

                // do not connect a to d if d = a.
                if(!d.equals(a)){

                    // f indicates: there does not exist g in omega_a such that g = d.
                    boolean f = true;

                    // f denotes neighbour of a.
                    for(Player g: omega_a){
                        if(d.equals(g)){
                            f = false;
                            break;
                        }
                    }

                    e = f;
                }
            }

            // connect a to d.
            omega_a.add(d);
            a.getEdgeWeights().add(1.0);

            // connect d to a.
            d.getNeighbourhood().add(a);
            d.getEdgeWeights().add(1.0);
        }
    }



    /**
     * Generic function to update stats
     */
    public void updateStats(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }








    /*
    a will pick a random c with 0.0 weight. if a or c would be isolated
    as a result of cutting ties, rewiring will not occur. in that case, a will NOT
    look for a new c. by this point, a has lost its opportunity to rewire this gen.
     */
    public int rewireAway0Single(Player a){
        // omega_a denotes copy of neighbourhood of a.
        ArrayList<Player> omega_a = new ArrayList<>(a.getNeighbourhood());

        // denotes list of weighted edges connecting a to its neigbours.
        ArrayList<Double> weights = new ArrayList(a.getEdgeWeights());

        // denotes pool of rewireable edges represented by their indices.
        ArrayList<Integer> rewirable_edge_indices = new ArrayList();

        // b_index denotes index of weighted edge w_ab that connects a to neighbour b.
        for(int b_index = 0; b_index < weights.size(); b_index++){
            double w = weights.get(b_index);

            // can cut the edge if weight w = 0.
            if(w == 0){
                rewirable_edge_indices.add(b_index);
            }
        }

        // indicates number of 0.0 weights originating at a.
        int num_rewirable_edges = rewirable_edge_indices.size();

        // supports the process of rewiring to new neighbour.
        // 1 if successful rewire away, 0 otherwise.
        int num_rewires = 0;

        // do not rewire if a has < 2 edges.
        if(omega_a.size() > 1){

            // do not rewire if there are no 0.0 weights to cut.
            if(num_rewirable_edges > 0){

                // randomly select a 0.0 weight.
                // c_index denotes the index of c within omega_a.
                int c_index = rewirable_edge_indices.get(ThreadLocalRandom.current().nextInt(num_rewirable_edges));

                // c denotes neighbour of a that a will try to rewire away from.
                Player c = omega_a.get(c_index);

                // omega_c denotes neighbourhood of c.
                ArrayList<Player> omega_c = c.getNeighbourhood();

                // do not rewire if c has < 2 edges.
                if(omega_c.size() > 1){

                    // d denotes neighbour of c.
                    for(int d_index = 0; d_index < omega_c.size(); d_index++) {
                        Player d = omega_c.get(d_index);

                        // if d = a, then c_index is the location of a in omega_b.
                        if(d.equals(a)){

                            // disconnect a from c.
                            omega_a.remove(c_index);
                            weights.remove(c_index);

                            // disconnect c from a.
                            omega_c.remove(d_index);
                            c.getEdgeWeights().remove(d_index);

                            num_rewires++;

                            // once the cutting of edges has been completed, stop looping.
                            break;
                        }
                    }
                } // if false, c would be isolated as a result of a rewiring.
            } // if false, a has no 0.0 weights to cut.

            a.setNeighbourhood(omega_a);
            a.setEdgeWeights(weights);
        } // if false, a would be isolated as a result of rewiring away.

        return num_rewires;
    }




    // incomplete
    public int rewireAwayRW(Player a){

        ArrayList<Player> omega_a = new ArrayList<>(a.getNeighbourhood());

        ArrayList<Double> weights = new ArrayList<>(a.getEdgeWeights());


        //apply roulette wheel approach to selecting edges...

        return 0;
    }



    /**
     * use the varying field to help determine whether a field should be
     * included in the settings String. if a field equals 0.0 and is not being
     * varied, it does not need to be added.<br>
     */
    public static void writeSettings(){
        String settings_filename = this_path + "\\" + "settings.csv";
        String settings = "";
        try{
            if(expNum == 1){
                fw = new FileWriter(settings_filename, false);
                settings += "game";
//                settings += varying.equals("M")? ",M": "";
                settings += ",runs";
                settings += ",gens";
                settings += ",space";
//                settings += length == 0 && !varying.equals("length")? "": ",length";
//                settings += width == 0 && !varying.equals("width")? "": ",width";
                settings += ",length";
                settings += ",width";
                settings += ",neighType";
                settings += neighRadius == 0 && !varying.equals("neighRadius")? "": ",neighRadius";
                settings += neighSize == 0 && !varying.equals("neighSize")? "": ",neighSize";
                settings += ",EM";
//                settings += ER == 0 && !varying.equals("ER")? "": ",ER";
//                settings += ER == 0 && !varying.equals("newER") || !varying.equals("oldER")? "": ",ER";
                settings += ER != 0? ",ER": "";
//                settings += NIS == 0 && !varying.equals("NIS")? "": ",NIS";
                settings += iters != 0? ",iters": "";
                settings += NIS != 0? ",NIS": "";
                settings += ",EWT";
                settings += RP == 0.0 && !varying.equals("RP")? "": ",RP";
                settings += RA.equals("")? "": ",RA";
                settings += RT.equals("")? "": ",RT";
                settings += EWLF.equals("")? "": ",EWLF";
                settings += ROC == 0.0 && !varying.equals("ROC")? "": ",ROC";
                settings += alpha == 0.0 && !varying.equals("alpha")? "": ",alpha";
                settings += beta == 0.0 && !varying.equals("beta")? "": ",beta";
//                settings += EWLP.equals("")? "": ",EWLP";
                settings += ",sel";
                settings += sel.equals("RW")? ",RWT": "";
                settings += selNoise == 0.0 && !varying.equals("selNoise")? "": ",selNoise";
                settings += ",evo";
                settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": ",evoNoise";
                settings += mut.equals("")? "": ",mut";
                settings += mutRate == 0.0 && !varying.equals("mutRate")? "": ",mutRate";
//                settings += mutBound == 0.0 && !varying.equals("mutBound")? "": ",mutBound";
                settings += mut.equals("local")? ",mutBound": "";
                settings += ",UF";
                settings += injIter == 0? "": ",injIter";
                settings += injP == 0.0? "": ",injP";
                settings += injSize == 0? "": ",injSize";
            } else {
                fw = new FileWriter(settings_filename, true);
            }
            settings += "\n";
            settings += game;
//            settings += varying.equals("M")? "," + M: "";
            settings += "," + runs;
            settings += "," + gens;
            settings += "," + space;
            settings += length == 0 && !varying.equals("length")? "": "," + length;
            settings += width == 0 && !varying.equals("width")? "": "," + width;
            settings += "," + neighType;
            settings += neighRadius == 0 && !varying.equals("neighRadius")? "": "," + neighRadius;
            settings += neighSize == 0 && !varying.equals("neighSize")? "": "," + neighSize;
            settings += "," + EM;
//            settings += ER == 0 && !varying.equals("ER")? "": "," + ER;
//            settings += ER == 0 && !varying.equals("newER") || !varying.equals("newER")? "": "," + ER;
            settings += ER != 0? "," + ER: "";
            settings += iters != 0? "," + iters: "";
//            settings += NIS == 0 && !varying.equals("NIS")? "": "," + NIS;
            settings += NIS != 0? "," + NIS: "";
            settings += "," + EWT;
            settings += RP == 0.0 && !varying.equals("RP")? "": "," + RP;
            settings += RA.equals("")? "": "," + RA;
            settings += RT.equals("")? "": "," + RT;
            settings += EWLF.equals("")? "": "," + EWLF;
            settings += ROC == 0.0 && !varying.equals("ROC")? "": "," + ROC;
            settings += alpha == 0.0 && !varying.equals("alpha")? "": "," + alpha;
            settings += beta == 0.0 && !varying.equals("beta")? "": "," + beta;
//            settings += EWLP.equals("")? "": "," + EWLP;
            settings += "," + sel;
            settings += sel.equals("RW")? "," + RWT: "";
            settings += selNoise == 0.0 && !varying.equals("selNoise")? "": "," + selNoise;
            settings += "," + evo;
            settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": "," + evoNoise;
            settings += mut.equals("")? "": "," + mut;
            settings += mutRate == 0.0 && !varying.equals("mutRate")? "": "," + mutRate;
            settings += mutBound == 0.0 && !varying.equals("mutBound")? "": "," + mutBound;
            settings += "," + UF;
            settings += injIter == 0? "": "," + injIter;
            settings += injP == 0.0? "": "," + injP;
            settings += injSize == 0? "": "," + injSize;
            fw.append(settings);
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }



    public static void writeResults(){
        String results_filename = this_path + "\\" + "results.csv";
        String results = "";
        try{
            if(expNum == 1){
                fw = new FileWriter(results_filename, false);
                switch(game){
                    case "UG" -> {
                        results += "mean mean p";
                        results += ",sigma mean p";
                        results += ",mean mean q";
                        results += ",sigma mean q";
                    }
                    case "DG" -> {
                        results += "mean mean p";
                        results += ",sigma mean p";
                    }
                    case "PD" -> {}
                }
                results += ",mean mean u";
                results += ",sigma mean u";
                results += ",mean mean degree";
                results += ",sigma mean degree";
                results += ",mean sigma degree";
                if(!varying.equals(""))
                    results += "," + varying;
                results += ",duration";
            }else {
                fw = new FileWriter(results_filename, true);
            }
            results += "\n";
            switch(game){
                case "UG" -> {
                    results += DF4.format(mean_mean_p);
                    results += "," + DF4.format(sigma_mean_p);
                    results += "," + DF4.format(mean_mean_q);
                    results += "," + DF4.format(sigma_mean_q);
                }
                case "DG" -> {
                    results += DF4.format(mean_mean_p);
                    results += "," + DF4.format(sigma_mean_p);}
                case "PD" -> {}
            }
            results += "," + DF4.format(mean_mean_u);
            results += "," + DF4.format(sigma_mean_u);
            results += "," + DF4.format(mean_mean_degree);
            results += "," + DF4.format(sigma_mean_degree);
            results += "," + DF4.format(mean_sigma_degree);


            // write value of varying parameter.
            switch(varying){
//                case "runs" -> results += "," + runs;
                case "ER" -> results += "," + ER;
//                case "newER", "oldER" -> results += "," + ER;
                case "NIS" -> results += "," + NIS;
                case "ROC" -> results += "," + ROC;
                case "length" -> results += "," + length;
                case "width" -> results += "," + width;
                case "RP" -> results += "," + RP;
                case "gens" -> results += "," + gens;
                case "EWLF" -> results += "," + EWLF;
                case "EWT" -> results += "," + EWT;
                case "RA" -> results += "," + RA;
                case "RT" -> results += "," + RT;
                case "sel" -> results += "," + sel;
                case "evo" -> results += "," + evo;
//                case "M" -> results += "," + M;
                case "selNoise" -> results += "," + selNoise;
            }

            // write duration of experiment
            LocalDateTime current_timestamp = LocalDateTime.now();
            Duration duration = Duration.between(old_timestamp, current_timestamp);
            results += "," + duration.toHours() +":" + duration.toMinutes() % 60 + ":" + duration.toSeconds() % 60;
            old_timestamp = current_timestamp;

            fw.append(results);
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Calculate utility of player.
     * With UF MNI, utility is basically equivalent to the old average score metric.<br>
     * With UF normalised, the degree of the player reduces their utility.
     */
    public void updateUtility(Player player){
        switch(UF){
            case "MNI" -> { // minimum number of interactions; with neighType="VN", neighRadius=1, no rewiring, MNI of all players is always 8.
                if(player.getNeighbourhood().size() == 0)
                    player.setU(0.0);
                else
                    player.setU(player.getPi() / player.getMNI());
            }
            case "cumulative" -> player.setU(player.getPi()); // cumulative payoff
//            case "normalised" -> player.setU(player.getPi() / player.getNeighbourhood().size());
            case "normalised" -> { // normalised payoff; with neighType="VN", neighRadius=1, no rewiring, denominator is always 4.
                if(player.getNeighbourhood().size() == 0){
                    player.setU(0.0);
                }else{
                    player.setU(player.getPi() / player.getNeighbourhood().size());
                }
            }
        }
//        if(Double.isNaN(player.getU())){
//            System.out.println("BP");
//        }
    }



    // probability for a to rewire away from b is calculated using Fermi-Dirac equation.
    // disregards weights of edges.
    public int rewireAwayFermiDirac(Player a){
        int num_rewires = 0;
        ArrayList<Player> omega_a = a.getNeighbourhood();
        int degree_a = omega_a.size();
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
        double k = 0.1; // noise in F-D eqn
        for(int i = 0; i < degree_a; i++){
            Player b = omega_a.get(i);
            double prob_rewire = 1 / (1 + Math.exp((a.getU() - b.getU()) / k)); // denotes probability of rewiring away from b
            double c = ThreadLocalRandom.current().nextDouble();
            if(prob_rewire > c){
                indices_of_edges_to_be_rewired.add(i);
            }
        }
        for(int i=indices_of_edges_to_be_rewired.size()-1;i >= 0;i--){
            int d = indices_of_edges_to_be_rewired.get(i);
            Player e = omega_a.get(d);
            ArrayList<Player> omega_e = e.getNeighbourhood();
            for(int j = 0; j < omega_e.size(); j++){
                Player f = omega_e.get(j);
                if(f.equals(a)){
                    omega_a.remove(d);
                    weights.remove(d);
                    omega_e.remove(j);
                    e.getEdgeWeights().remove(j);
                    num_rewires++;
                    break;
                }
            }
        }
        return num_rewires;
    }



    // as w decreases, prob to rewire exponentially increases.
    public int rewireAwayExponential(Player a){
        int num_rewires = 0;
        ArrayList<Player> omega_a = a.getNeighbourhood();
        int degree_a = omega_a.size();
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
        double k = 5; // k denotes rate of decay. as k increases,
        for(int i = 0; i < degree_a; i++){
            double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
            double prob_rewire = Math.exp(-k * w_ab);
            double c = ThreadLocalRandom.current().nextDouble();
            if(prob_rewire > c){
                indices_of_edges_to_be_rewired.add(i);
            }
        }
        for(int i=indices_of_edges_to_be_rewired.size()-1;i >= 0;i--){
            int d = indices_of_edges_to_be_rewired.get(i);
            Player e = omega_a.get(d);
            ArrayList<Player> omega_e = e.getNeighbourhood();
            for(int j = 0; j < omega_e.size(); j++){
                Player f = omega_e.get(j);
                if(f.equals(a)){
                    omega_a.remove(d);
                    weights.remove(d);
                    omega_e.remove(j);
                    e.getEdgeWeights().remove(j);
                    num_rewires++;
                    break;
                }
            }
        }
        return num_rewires;
    }



    public Player selRandomNeigh(Player a){
        ArrayList<Player> omega_a = a.getNeighbourhood();
        return omega_a.get(ThreadLocalRandom.current().nextInt(omega_a.size()));
    }



    public Player selRandomPop(){
        return findPlayerByID(ThreadLocalRandom.current().nextInt(N));
    }



    // Records utilities of the population to a .csv file.
    public void writeUPop(){
        String filename = experiment_path + "\\run" + run + "\\u_pop\\gen" + gen + ".csv";
        String s = "";
        for(int y=length-1;y>=0;y--){
            for(int x=0;x<width;x++){
                Player player = findPlayerByPos(y,x);
                double u = player.getU();
                s += DF4.format(u);
                if(x + 1 < length){
                    s += ",";
                }
            }
            s += "\n";
        }
        try{
            fw = new FileWriter(filename, false);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    public void writeUStats(){
        String filename = experiment_path + "\\run" + run + "\\u_stats.csv";
        String s="";
        if(gen == 1){ // apply headings to file before writing data
            s+="gen";
            s+=",mean u";
            s+=",sigma u";
            s+="\n";
        }
        s+=gen;
        s+=","+DF4.format(mean_u);
        s+=","+DF4.format(sigma_u);
        s+="\n";
        try{
            fw = new FileWriter(filename, true);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    // Records degrees of the population to a .csv file.
    public void writeDegPop(){
        String filename = experiment_path + "\\run" + run + "\\deg_pop\\gen" + gen + ".csv";
        String s = "";
        for(int y=length-1;y>=0;y--){
            for(int x=0;x<width;x++){
                Player player = findPlayerByPos(y,x);
                int degree = player.getDegree();
                s += degree;
                if(x + 1 < length){
                    s += ",";
                }
            }
            s += "\n";
        }
        try{
            fw = new FileWriter(filename, false);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }



    public void writeDegStats() {
        String filename = experiment_path + "\\run" + run + "\\deg_stats.csv";
        String s="";
        if(gen == 1){ // apply headings to file before writing data
            s+="gen";
            s+=",mean deg";
            s+=",sigma deg";
            s+="\n";
        }
        s+=gen;
        s+=","+DF4.format(mean_degree);
        s+=","+DF4.format(sigma_degree);
        s+="\n";
        try{
            fw = new FileWriter(filename, true);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Player may rewire their edges.<br>
     * Rewiring is guaranteed to occur once the edge is referenced in the
     * indices_of_edges_to_be_rewired ArrayList.<br>
     * @param a player who may rewire
      */
    public void rewire(Player a){
        double random_number = ThreadLocalRandom.current().nextDouble();
        if(RP > random_number){
            int num_rewires = 0;
            ArrayList<Player> omega_a = a.getNeighbourhood();
            ArrayList<Double> weights = a.getEdgeWeights();
            ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
            for(int i = 0; i < omega_a.size(); i++){
                double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
                double prob_rewire = 0;
                switch(RA){ // I decided to bunch the rewire away functions into one switch because they are very similar functionally.
                    case "smoothstep" -> prob_rewire = 1 - (3 * Math.pow(w_ab, 2) - 2 * Math.pow(w_ab, 3));
                    case "smootherstep" -> prob_rewire = 1 - (6 * Math.pow(w_ab, 5) - 15 * Math.pow(w_ab, 4) + 10 * Math.pow(w_ab, 3));
                    case "0Many" -> prob_rewire = w_ab == 0.0? 1.0: 0.0;
                    case "linear" -> prob_rewire = 1 - w_ab;
                    case "exponential" -> {
                        double k = 0.1; // manually set noise
                        prob_rewire = Math.exp(-k * w_ab);
                    }
                    case "FD" -> {
                        Player b = omega_a.get(i);
                        double k = 0.1; // manually set noise
                        prob_rewire = 1 / (1 + Math.exp((a.getU() - b.getU()) / k));
                    }
                }
                double c = ThreadLocalRandom.current().nextDouble();
                if(prob_rewire > c){
                    indices_of_edges_to_be_rewired.add(i);
                }
            }
            for(int i = indices_of_edges_to_be_rewired.size() - 1; i >= 0; i--){
                int d = indices_of_edges_to_be_rewired.get(i);
                Player e = omega_a.get(d);
                ArrayList<Player> omega_e = e.getNeighbourhood();
                for(int j = 0; j < omega_e.size(); j++){
                    Player f = omega_e.get(j);
                    if(f.equals(a)){
                        omega_a.remove(d);
                        weights.remove(d);
                        omega_e.remove(j);
                        e.getEdgeWeights().remove(j);
                        num_rewires++;
                        break;
                    }
                }
            }


            if(num_rewires > 0){
                switch (RT){
                    case"local"->RTLocal(a, num_rewires);
                    case"pop"->RTPop(a, num_rewires);
                }
            }
        }
    }


    /**
     * Probability to evolve equals utility difference divided by degree.<br>
     * Evolution cannot occur if child fitter than parent.
     * @param child
     * @param parent
     */
    public void evoCopyFitter(Player child, Player parent){
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = (parent.getU() - child.getU()) / child.getNeighbourhood().size();
        if(random_number < prob_evolve)
            evoCopy(child, parent);
    }



    public void writeResultsExperiment(){
        String filename = experiment_path + "\\exp_results.csv";
        String output = "";
        try{
            if(run == 1){
                fw = new FileWriter(filename, false);
                output += "mean p";
                output += ",sigma p";
                output += ",mean u";
                output += ",sigma u";
                output += ",mean degree";
                output += ",sigma degree";
            } else{
                fw = new FileWriter(filename, true);
            }
            output += "\n";
            output += DF4.format(mean_p);
            output += "," + DF4.format(sigma_p);
            output += "," + DF4.format(mean_u);
            output += "," + DF4.format(sigma_u);
            output += "," + DF4.format(mean_degree);
            output += "," + DF4.format(sigma_degree);
            fw.append(output);
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }



    public void calculatePMax(){
        for(int i=0;i<N;i++){
            double p = pop[i].getP();
            if(p > p_max)
                p_max = p;
        }
    }



    public Player sel(Player child){
        Player parent = null;
        switch(sel){
            case "RW" -> parent = selRW(child);
            case "fittest" -> parent = selFittest(child);
            case "crossover" -> crossover(child);
            case "randomNeigh" -> parent = selRandomNeigh(child);
            case "randomPop" -> parent = selRandomPop();
        }
        return parent;
    }



    public void evo(Player child, Player parent){
        switch (evo) {
            case "copy" -> evoCopy(child, parent);
            case "approach" -> evoApproach(child, parent);
            case "copyFitter" -> evoCopyFitter(child, parent);
        }
    }



    public void mut(Player child){
        switch (mut){
            case "global" -> {
                if(mutationCheck())
                    mutGlobal(child);
            }
            case "local" -> {
                if(mutationCheck())
                    mutLocal(child);
            }
        }
    }
}

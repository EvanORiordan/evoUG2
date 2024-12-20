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
 * 10/12/2024<br>
 * School of Computer Science<br>
 * University of Galway<br>
 */
public class Env extends Thread{ // simulated game environment

    // fields related to the game environment
    static String game; // what game is being played
    static int length; // length of space; in 2D grid, len = num rows
    static int width; // width of space; in 2D grid, wid = num cols i.e. num players per row
    static int N; // population size
    static int iters; // how many iterations occur per experiment run
    static int runs; // how many times this experiment will be run.
    Player[] pop; // array of players; assumes 2D space
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    int gen = 0; // indicates current generation. indicates how many generations have passed.
    int iter = 1; // indicates current iteration of the core algorithm (play + EWL)
    static int injIter; // injection iteration: indicates when strategy injection will occur. 0 ==> no injection.
    static double injP = 0.0; // injection p: indicates p value to be injected
    static int injSize = 0; // injection cluster size: indicates size of cluster to be injected
    static double T; // PD: temptation to defect
    static double R; // PD: reward for mutual coopeation
    static double P; // PD: punishment for mutual defection
    static double S; // PD: sucker's payoff for cooperating with a defector
    static double l; // loner's payoff
    static double M = 1.0; // default prize amount in an UG/DG interaction
    static String UF; // utility formula: indicates how utility is calculated
    double avg_q; // average acceptance threshold across population
    double q_SD; // the standard deviation of q across the pop
    static String space; // the space the population exists within


    // fields related to experiment statistics
    static String varying; // indicates which parameter will be varied in experiment series
    static double variation; // amount by which varying parameter will vary between subsequent experiments. the double data is used because it works for varying integer parameters as well as doubles.
    static int numExp = 1; // number of experiments to occur; indicates whether series of experiments to occur; set to 1 by default.
    static int experiment_num = 1; //tracks which experiment is taking place at any given time during a series
    static int run_num; // tracks which of the runs is currently executing
    static ArrayList<Double> various_amounts;
    static double experiment_mean_avg_p;
    static double experiment_mean_avg_q;
    static double experiment_SD_avg_p;
    static double experiment_SD_avg_q;
    static double[] experiment_avg_p_values;
    static double[] experiment_avg_q_values;


    // fields related to I/O operations
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static String desc; //description of experiment
    static String start_timestamp_string;
    static String project_path = Paths.get("").toAbsolutePath().toString();
    static String data_folder_path = project_path + "\\csv_data";
    static String experiment_results_folder_path;
    static String series_data_filename;
    static String p_data_filename;
    static String EW_data_filename;
//    static String NSI_data_filename;
    static int dataRate; // factors into which gens have their data recorded. if 0, no gens are recorded.
    static String q_data_filename;
    static String pos_data_filename;
    static String edge_count_data_filename;


    // fields related to neighbourhoods
    static String neighType; // indicates type of neighbourhood players will have.
    static int neighRange; // range of neighbourhood with von Neumann / Moore / diamond neighbourhood type.
    static int neighSize; // size of neighbours with random neighbourhood type.


    // fields related to edge weights (EWs) and edge weight learning (EWL)
    static String EWT; // EW type
//    static String EWLC; // EWL condition
    static String EWLF; // EWL formula
    static double ROC = 0; // rate of change: fixed learning amount to EW
    static double alpha = 0; // used in alpha-beta rating
    static double beta = 0; // used in alpha-beta rating
    static String EWLP; // EWL probability


    static double leeway1 = 0; // defines global leeway affecting all players.
    static double leeway2 = 0; // defines bounds of interval that local leeway is generated from.
    static double leeway3 = 0; // defines factor used in calculation of leeway given wrt weight of edge to neighbour.
    static double leeway4 = 0; // defines factor used in calculation of leeway given wrt comparison of p vs neighbour p.
    static double leeway5 = 0; // defines factor used in calculation of leeway given wrt neighbour p.
    static double leeway6 = 0; // defines bounds of interval that random leeway is generated from.
    static double leeway7 = 0; // defines avg score comparison leeway factor.


    // fields related to evolution, selection, mutation
    static String evo; // indicates which evolution function to call
    static double evoNoise = 0; // noise affecting evolution
    static String sel; // indicates which selection function to call
    static double selNoise = 0; // noise affecting selection
    static String mut; // indicates which mutation function to call
    static double mutRate = 0; // probability of mutation
    static double mutBound = 0; // denotes max mutation possible
    static int ER; // evolution rate: indicates how many iterations pass before a generation occurs e.g. ER=5 means every gen has 5 iters
    static String EM; // evolution mechanism: the mechanism by which evolution occurs.
    static int NIS; // num inner steps: number of inner steps per generation using the monte carlo method; usually is set to value of N


    // fields related to rewiring
    static String RA = ""; // rewire away
//    static String EW_rewire_away_qty;
//    static String rewire_qty;
    static String RT = ""; // rewire to
    static double RP = 0.0; // rewire probability
//    static int num_rewires; // number of rewires to be performed by the given player



    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        configureEnvironment();
//        setInitialSettings();
        LocalDateTime start_timestamp = LocalDateTime.now(); // marks the beginning of the main algorithm's runtime
        start_timestamp_string = start_timestamp.getYear()
                +"-"+start_timestamp.getMonthValue()
                +"-"+start_timestamp.getDayOfMonth()
                +"_"+start_timestamp.getHour()
                +"-"+start_timestamp.getMinute()
                +"-"+start_timestamp.getSecond();
        experiment_results_folder_path = data_folder_path+"\\"+start_timestamp_string+" "+desc;
        try {
            Files.createDirectories(Paths.get(experiment_results_folder_path)); // create results storage folder
        }catch(IOException e){
            e.printStackTrace();
        }
        printExperimentResultsFolderPath();


        // create result data folders
        if(dataRate != 0) {
            createFolders();
        }


        System.out.println("Starting timestamp: "+start_timestamp);


//        if(experiment_series){
        if(numExp > 1){
            experimentSeries(); // run an experiment series
        } else {
            experiment(); // run a single experiment
        }
        LocalDateTime finish_timestamp = LocalDateTime.now(); // marks the end of the main algorithm's runtime
        System.out.println("Finishing timestamp: "+finish_timestamp);
        Duration duration = Duration.between(start_timestamp, finish_timestamp);
        long secondsElapsed = duration.toSeconds();
        long minutesElapsed = duration.toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
        printExperimentResultsFolderPath();
    }



    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(){
//        System.out.println();
//        printInitialSettings();


        various_amounts = new ArrayList<>(); // stores initial value of varying parameter
        switch(varying){
            case"runs"->various_amounts.add((double)runs);
            case"iters"->various_amounts.add((double)iters);
            case"length"->various_amounts.add((double) length);
            case"width"->various_amounts.add((double) width);
            case"ER"->various_amounts.add((double)ER);
            case "NIS" -> various_amounts.add((double) NIS);
            case"ROC"->various_amounts.add(ROC);
            case"leeway1"->various_amounts.add(leeway1);
            case"leeway2"->various_amounts.add(leeway2);
            case"leeway3"->various_amounts.add(leeway3);
            case"leeway4"->various_amounts.add(leeway4);
            case"leeway5"->various_amounts.add(leeway5);
            case"leeway6"->various_amounts.add(leeway6);
            case"leeway7"->various_amounts.add(leeway7);
            case"selNoise"->various_amounts.add(selNoise);
            case"evoNoise"->various_amounts.add(evoNoise);
            case"mutRate"->various_amounts.add(mutRate);
            case"mutBound"->various_amounts.add(mutBound);
            case"injIter"->various_amounts.add((double)injIter);
            case"injP"->various_amounts.add(injP);
            case"injSize"->various_amounts.add((double)injSize);
            case"RP"->various_amounts.add(RP);
        }

        // run experiment series
        for(int i=1;i<=numExp;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("\n===================\n" +
                    "NOTE: Start of experiment "+experiment_num+": "+varying+"="+DF4.format(various_amounts.get(i - 1))+".");

            experiment(); // run an experiment of the series

            switch(varying){ // after experiment, change the value of the varying parameter
                case "runs"->{
                    runs+=(int)variation;
                    various_amounts.add((double)runs);
                }
                case "iters"->{
                    iters+=(int)variation;
                    various_amounts.add((double)iters);
                }
                case "length"->{
                    length+=(int)variation;
                    N = length * width;
                    various_amounts.add((double)length);
                }
                case"width"->{
                    width+=(int)variation;
                    N = length * width;
                    various_amounts.add((double)width);
                }
                case "ER"->{
                    ER+=(int)variation;
                    various_amounts.add((double)ER);
                }
                case "NIS" -> {
                    NIS += (int) variation;
                    various_amounts.add((double) NIS);
                }
                case "ROC"->{
                    ROC+=variation;
                    various_amounts.add(ROC);
                }
                case "leeway1"->{
                    leeway1+=variation;
                    various_amounts.add(leeway1);
                }
                case "leeway2"->{
                    leeway2+=variation;
                    various_amounts.add(leeway2);
                }
                case "leeway3"->{
                    leeway3+=variation;
                    various_amounts.add(leeway3);
                }
                case "leeway4"->{
                    leeway4+=variation;
                    various_amounts.add(leeway4);
                }
                case "leeway5"->{
                    leeway5+=variation;
                    various_amounts.add(leeway5);
                }
                case "leeway6"->{
                    leeway6+=variation;
                    various_amounts.add(leeway6);
                }
                case "leeway7"->{
                    leeway7+=variation;
                    various_amounts.add(leeway7);
                }
                case "selNoise"->{
                    selNoise+=variation;
                    various_amounts.add(selNoise);
                }
                case "evoNoise"->{
                    evoNoise+=variation;
                    various_amounts.add(evoNoise);
                }
                case "mutRate"->{
                    mutRate+=variation;
                    various_amounts.add(mutRate);
                }
                case "mutBound"->{
                    mutBound+=variation;
                    various_amounts.add(mutBound);
                }
                case "injIter"->{
                    injIter += (int) variation;
                    various_amounts.add((double) injIter);
                }
                case "injP"->{
                    injP += variation;
                    various_amounts.add(injP);
                }
                case "injSize"->{
                    injSize += (int) variation;
                    various_amounts.add((double) injSize);
                }
                case"RP"->{
                    RP+=variation;
                    various_amounts.add(RP);
                }
            }

            // inform user what the varying parameter's value was
            System.out.println("NOTE: End of "+varying+"="+DF4.format(various_amounts.get(i - 1))+"."
                    +"\n===================");
        }
    }



    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){
        switch(game){
            case "UG" -> {
                experiment_mean_avg_p = 0;
                experiment_avg_p_values = new double[runs];
                experiment_SD_avg_p = 0;
                experiment_mean_avg_q = 0;
                experiment_avg_q_values = new double[runs];
                experiment_SD_avg_q = 0;
            }
            case "DG" -> {
                experiment_mean_avg_p = 0;
                experiment_avg_p_values = new double[runs];
                experiment_SD_avg_p = 0;}
            case "PD" -> {}
        }
        for(run_num = 1; run_num <= runs; run_num++){
            Env run = new Env(); // represents one run of experiment
            run.start();
            String output = "experiment "+experiment_num+", run "+run_num+": ";
            switch(game){
                case "UG" -> {
                    experiment_mean_avg_p += run.avg_p;
                    experiment_avg_p_values[run_num] = run.avg_p;
                    experiment_mean_avg_q += run.avg_q;
                    experiment_avg_q_values[run_num] = run.avg_q;
                    output += "avg p=" + DF4.format(run.avg_p);
                    output += " avg q="+DF4.format(run.avg_q);
                }
                case "DG" -> {
                    experiment_mean_avg_p += run.avg_p;
                    experiment_avg_p_values[run_num - 1] = run.avg_p;
                    output += "avg p=" + DF4.format(run.avg_p);
                }
                case "PD" -> {} // IDEA: if game is PD, then print number of C, D and A strategy players...
            }
            System.out.println(output);
        }
        String output = "";
        switch(game){
            case "UG" -> {
                experiment_mean_avg_p /= runs;
                experiment_mean_avg_q /= runs;
                for(int i = 0; i < runs; i++){
                    experiment_SD_avg_p += Math.pow(experiment_avg_p_values[i] - experiment_mean_avg_p, 2);
                    experiment_SD_avg_q += Math.pow(experiment_avg_q_values[i] - experiment_mean_avg_q, 2);
                }
                experiment_SD_avg_p = Math.pow(experiment_SD_avg_p / runs, 0.5);
                experiment_SD_avg_q = Math.pow(experiment_SD_avg_q / runs, 0.5);
                output += "mean avg p=" + DF4.format(experiment_mean_avg_p);
                output += " avg p SD=" + DF4.format(experiment_SD_avg_p);
                output += " mean avg q=" + DF4.format(experiment_mean_avg_q);
                output += " avg q SD=" + DF4.format(experiment_SD_avg_q);
            }
            case "DG" -> {
                experiment_mean_avg_p /= runs;
                for(int i = 0; i < runs; i++){
                    experiment_SD_avg_p += Math.pow(experiment_avg_p_values[i] - experiment_mean_avg_p, 2);
                }
                experiment_SD_avg_p = Math.pow(experiment_SD_avg_p / runs, 0.5);
                output += "mean avg p=" + DF4.format(experiment_mean_avg_p);
                output += " avg p SD=" + DF4.format(experiment_SD_avg_p);
            }
            case "PD" -> {}
        }
        System.out.println(output);


        writeSettings();
        writeResults();
        experiment_num++; // move on to the next experiment in the series
    }



    /**
     * Method for running the core algorithm at the heart of the program.
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

//        if(space.equals("grid") && run_num == 1){
//            writePosData();
//        }

        // initialise neighbourhoods
        for(int i=0;i<N;i++){
            initialiseEdgeWeights(pop[i]);
        }

        // the algorithm iterates.
        while(iter <= iters) {

            // injection
//            if(iter == injIter){
//                injectStrategyCluster();
//            }

            // playing
            for(int i=0;i<N;i++){
                play(pop[i]);
            }

            // update utility
            for(int i=0;i<N;i++){
                updateUtility(pop[i]);
            }


            // edge weight learning
            for(int i=0;i<N;i++){
                EWL(pop[i]);
            }

            switch(EM){
                // evolution rate
                case "ER" -> {
                    // one generation passes after every ER iterations.
                    // rewiring and strategy evolution may occur every generation.
                    if(iter % ER == 0){

                        // rewiring
//                        if(EWT.equals("3")){
                        if(EWT.equals("rewire")){
                            for(int i=0;i<N;i++){
                                Player player = pop[i];
                                double random_number = ThreadLocalRandom.current().nextDouble();
                                if(RP > random_number){
                                    int num_rewires = 0;
                                    switch (RA) {
                                        case "0Single" -> num_rewires = rewireAway0Single(player);
                                        case "0Many" -> num_rewires = rewireAway0Many(player);
                                        case "linear" -> num_rewires = rewireAwayLinear(player);
                                        case "FD" -> num_rewires = rewireAwayFermiDirac(player);
                                        case "exponential" -> num_rewires = rewireAwayExponential(player);
                                        case "smoothstep" -> num_rewires = rewireAwaySmoothstep(player);
                                    }
                                    if(num_rewires > 0){
                                        switch (RT){
                                            case"local"->rewireToLocal(player, num_rewires);
                                            case"pop"->rewireToPop(player, num_rewires);
                                        }
                                    }
                                }
                            }
                        }

                        // strategy evolution
                        for(int i=0;i<N;i++) {
                            Player child = pop[i];

                            // prevent evolution if child is isolated.
                            if(pop[i].getNeighbourhood().size() > 0){

                                // selection
                                Player parent = null;
                                switch(sel){
                                    case "NRW" -> parent = selNRW(child);
                                    case "ERW" -> parent = selERW(child);
                                    case "fittest" -> parent = selFittest(child);
                                    case "rand" -> parent = selRand(child);
                                    case "crossover" -> crossover(child); // "sel" and "evo" occur in one func
                                }

                                // evolution
                                switch (evo) {
                                    case "copy" -> evoCopy(child, parent);
                                    case "approach" -> evoApproach(child, parent);
                                }

                                // mutation
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

                        gen++; // move on to the next generation
                    }
                }

                // monte carlo
                case "MC" -> {
                    for(int i = 0; i < NIS; i++){
                        int a = ThreadLocalRandom.current().nextInt(NIS);
                        Player b = findPlayerByID(a);
                        ArrayList<Player> omega_b = b.getNeighbourhood();
                        int c = ThreadLocalRandom.current().nextInt();
                        Player d = omega_b.get(c);
                        double e = ThreadLocalRandom.current().nextDouble();
                        double f = (d.getU() - b.getU()) / omega_b.size();
                        if(e < f){
                            evoCopy(b, d);
                        }
                    }
                }
            }



            switch(game){
                case"UG","DG"->calculateStatsUG();
//                case"PD"->calculateStatsPD(); // implement this func later...
            }

            // save data.
            // do not save data if dataRate has not been initialised past 0.
            // must check this before future dataRate checks that assume dataRate != 0
            if(dataRate != 0){

                // do not save data if not the first run
                if(run_num == 1

                        // do not save data if not the first experiment
                        && experiment_num == 1

                        // data rate is used to determine rate at which data is saved.
                        // save data every x gens where x = dataRate.
                        // e.g. dataRate = 10 ==> save data every 10 gens.
                        && iter % (ER * dataRate) == 0
//                        && gen % dataRate == 0
                ){
                    String output = "";
//                    if(game.equals("UG") || game.equals("DG")){
//                        output += "iter "+iter+", gen "+gen+": avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD);
//                        writePData();
//                        writeAvgPData();
//                        writeEdgeCountData();
//                        if(game.equals("UG")){
//                            output += " avg q="+DF4.format(avg_q)+", q SD="+DF4.format(q_SD);
//                            writeQData();
//                            writeAvgQData();
//                        }
//                    }


                    switch(game){
                        case "UG" -> {
                            output += "iter "+iter+", gen "+gen+": avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD);
                            writePData();
                            writeAvgPData();
                            writeEdgeCountData();
                            writeQData();
                            writeAvgQData();
                        }
                        case "DG" -> {
                            writePData();
                            writeAvgPData();
                            writeEdgeCountData();
                        }
                        case "PD" -> {}
                    }


                    System.out.println(output);

//                    if(!EWT.equals("3") && length==width && neighType.split(" ")[0].equals("VN")){
                    if(!EWT.equals("rewire") && length==width && neighType.split(" ")[0].equals("VN")){
                        calculateAverageEdgeWeights();
                        writeEWData();
//                        writeNSIData();
                    }

                }
            }

            prepare();
            iter++; // move on to the next iteration
        }
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
//                    boolean interact = true;
////                    if(EWT.equals("1")){
//                    if(EWT.equals("proposalProb")){
//                        double d = ThreadLocalRandom.current().nextDouble();
//                        if(w_ba <= d){
//                            interact = false;
//
//                            // generically update stats
//                            updateStats(a,0);
//                            updateStats(b,0);
//
//                        }
//                    }
//                    if(interact){
//                        switch(game){
//                            case"UG","DG"->UG(a, b, w_ba);
//                            case"PD"->PD(a, b, w_ba);
//                        }
//                    }
//                    break;



//                    if(EWT.equals("proposalProb")){
//                        double d = ThreadLocalRandom.current().nextDouble();
//                        if(w_ba > d){
//                            switch(game){
//                                case "UG", "DG" -> UG(a, b);
//                                case "PD" -> PD(a, b);
//                            }
//                        } else{
//                            updateStats(a,0);
//                            updateStats(b,0);
//                        }
//                    } else if(){
//                        switch(game){
//                            case "UG", "DG" -> UG(a, b);
//                            case "PD" -> PD(a, b);
//                        }
//                    }


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
        player.setScore(player.getScore() + payoff);
        player.setMNI(player.getMNI() + 1);
//        if(payoff > 0){
//            player.setNSI(player.getNSI() + 1);
//        }
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
        player.setScore(player.getScore() + payoff);
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




//    public void EWL(Player player){
//        if(EWLT.equals("")) { // old implementation of EWL
//            ArrayList<Player> neighbourhood = player.getNeighbourhood();
//            for (int i = 0; i < neighbourhood.size(); i++) {
//                Player neighbour = neighbourhood.get(i);
//                double total_leeway = calculateTotalLeeway(player, neighbour, i);
//                int option = checkEWLC(player, neighbour, total_leeway);
//                ArrayList<Double> weights = player.getEdgeWeights();
//                double weight = weights.get(i);
//                if (option == 0) {
//                    weight += calculateLearning(player, neighbour);
//                    if (weight > 1)
//                        weight = 1;
//                } else if (option == 1) {
//                    weight -= calculateLearning(player, neighbour);
//                    if (weight < 0)
//                        weight = 0;
//                }
//                String str = DF2.format(weight);
//                double x = Double.parseDouble(str);
//                weights.set(i, x);
//            }
//        } else { // new way of EWL; might eventually replace old way.
//
//
//            Player a = player;
//            ArrayList<Player> omega_a = a.getNeighbourhood();
//            for (int i = 0; i < omega_a.size(); i++) {
//                Player b = omega_a.get(i);
//                ArrayList<Double> weights = a.getEdgeWeights();
//                double weight = weights.get(i);
//                double k = 0.1; // SHORTCUT WAY OF ASSIGNING NOISE; CAN IMPLEMENT BETTER WAY IN FUTURE
//                switch (EWLT) {
//                    case "pfixed" -> weight += b.getP() > a.getP()? ROC: -ROC; // increment or decrement by fixed amount ROC depending on p_a and p_b
//                    case "pD" -> weight += b.getP() - a.getP();
//                    case "pFD" -> weight += 1 / (1 + Math.exp((a.getUtility() - b.getUtility()) / k)); // fermi-dirac equation
//                }
//                if(weight > 1.0)
//                    weight = 1.0;
//                else if(weight < 0.0)
//                    weight = 0.0;
//                String str = DF2.format(weight);
//                double x = Double.parseDouble(str);
//                weights.set(i, x);
//            }
//        }
//    }
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
            boolean e = checkEWLP(a, b);
            if(e){
                w_ab += calculateLearning(a, b);
                if(w_ab > 1.0)
                    w_ab = 1.0;
                else if(w_ab < 0.0)
                    w_ab = 0.0;
                weights.set(i, w_ab); // set player's weight to copy.



                // the function of this code block is to round the weight, thus removing the issue with how doubles are not
                // fully accurate such that a weight should be 1 but is like 0.9999 instead.
                // i hope this isnt having an unintended effect, messing up in some other way. maybe the
                // rounding is adversely affecting the program in a way that is more damaging than without it.
//                String str = DF2.format(w_ab);
//                double x = Double.parseDouble(str);
//                weights.set(i, x);



            }
        }
    }



    public boolean checkEWLP(Player a, Player b){
        // manually set noise k.
        double k = 0.1;

        double x = 0.0;
        switch(EWLP){
            case "UFD" -> x = 1 / (1 + Math.exp((a.getU() - b.getU()) / k));
            case "PFD" -> x = 1 / (1 + Math.exp((a.getP() - b.getP()) / k));
            case "always" -> x = 1.0;
        }

//        System.out.println(a.getP() + "\t" + b.getP() + "\t" + x);
//        System.out.println(a.getU() + "\t" + b.getU() + "\t" + x);

        double y = ThreadLocalRandom.current().nextDouble();

        boolean z = x > y;

        return z;
    }




//    public double calculateTotalLeeway(Player player, Player neighbour, int i) {
////        double[] edge_weights = player.getEdgeWeights();
//        ArrayList <Double> weights = player.getEdgeWeights();
//        double weight = weights.get(i);
//        double p = player.getP();
//        double neighbour_p = neighbour.getP();
//        double avg_score = player.getAvgScore();
//        double neighbour_avg_score = player.getAvgScore();
//        double local_leeway = player.getLocalLeeway();
//
//
//        double global_leeway = leeway1;
////        double edge_weight_leeway = edge_weights[i] * leeway3;
//        double edge_weight_leeway = weight * leeway3;
//        double p_comparison_leeway = (neighbour_p - p) * leeway4;
//        double p_leeway = neighbour_p * leeway5;
//        double random_leeway;
//        if(leeway6 == 0){
//            random_leeway = 0;
//        } else{
//            random_leeway = ThreadLocalRandom.current().nextDouble(-leeway6, leeway6);
//        }
//        double avg_score_comparison_leeway = (avg_score - neighbour_avg_score) * leeway7;
//
//        double total_leeway = global_leeway
//                + local_leeway
//                + edge_weight_leeway
//                + p_comparison_leeway
//                + p_leeway
//                + random_leeway
//                + avg_score_comparison_leeway;
//
//        return total_leeway;
//    }



//    /**
//     * Checks the edge weight learning condition to determine what kind of edge weight
//     * learning should occur, if any.
//     *
//     * @param neighbour is the neighbour being pointed at by the edge
//     * @param total_leeway is the leeway being given to the neighbour by the edge's owner
//     * @return 0 for positive edge weight learning, 1 for negative, 2 for none
//     */
//    public int checkEWLC(Player player, Player neighbour, double total_leeway) {
//        double p = player.getP();
//        double p2 = neighbour.getP();
//        double avg_score = player.getAvgScore();
//        double avg_score2 = neighbour.getAvgScore();
//        double q = player.getQ();
//        double q2 = neighbour.getQ();
//
//        int option = 2;
//        switch(EWLC){
//
//            case"p"->{
//                if (p2 + total_leeway > p){
//                    option = 0;
//                } else if (p2 + total_leeway < p){
//                    option = 1;
//                }
//            }
//
//            case"avgscore"->{
//                if (avg_score2 < avg_score + total_leeway){
//                    option = 0;
//                } else if (avg_score2 > avg_score + total_leeway){
//                    option = 1;
//                }
//            }
//
//            case"AB"->{
//                double AB_rating1 = ((alpha * p) + (beta * avg_score)) / (alpha + beta);
//                double AB_rating2 = ((alpha * p2) + (beta * avg_score2)) / (alpha + beta);
//                if(AB_rating1 < AB_rating2)
//                    option = 0;
//                else if(AB_rating1 > AB_rating2)
//                    option = 1;
//            }
//
//            case"q"->{
//                if(q2 > q)
//                    option = 0;
//                else if(q2 < q)
//                    option = 1;
//            }
//        }
//
//        return option;
//    }



//    /**
//     * Calculate amount of edge weight learning to be applied to the weight of the edge.
//     */
//    public double calculateLearning(Player player, Player neighbour){
//        double p = player.getP();
//        double neighbour_p = neighbour.getP();
//        double avg_score = player.getAvgScore();
//        double neighbour_avg_score = neighbour.getAvgScore();
//        double q = player.getQ();
//        double neighbour_q = neighbour.getQ();
//
//
//
//        double learning = 0;
//        switch(EWLF){
//            case"ROC"->         learning = ROC;
//            case"PD"->         learning = Math.abs(neighbour_p - p);
//            case"PED"->        learning = Math.exp(Math.abs(neighbour_p - p));
//            case"avgscoreAD"->  learning = Math.abs(neighbour_avg_score - avg_score);
//            case"avgscoreEAD"-> learning = Math.exp(Math.abs(neighbour_avg_score - avg_score));
//            case"pAD2"->        learning = Math.pow(Math.abs(neighbour_p - p), 2);
//            case"pAD3"->        learning = Math.pow(Math.abs(neighbour_p - p), 3);
//            case"AB"->          learning = Math.abs((((alpha * p) + (beta * avg_score)) / (alpha + beta)) - (((alpha * neighbour_p) + (beta * neighbour_avg_score)) / (alpha + beta)));
//            case"qAD"->         learning = Math.abs(neighbour_q - q);
//            case"qEAD"->        learning = Math.exp(Math.abs(neighbour_q - q));
//            case"qAD2"->        learning = Math.pow(Math.abs(neighbour_q - q), 2);
//            case"qAD3"->        learning = Math.pow(Math.abs(neighbour_q - q), 3);
//            case"UD"->learning=
//        }
//
//        return learning;
//    }
    public double calculateLearning(Player a, Player b){
        double learning = 0.0;

        switch(EWLF){
            case "ROC" -> learning = ROC;
            case "PD" -> learning = b.getP() - a.getP();
            case "PED" -> learning = Math.exp(b.getP() - a.getP());
            case "UD" -> learning = b.getU() - a.getU();
            case "UED" -> learning = Math.exp(b.getU() - a.getU());
            case "PAD" -> learning = Math.abs(b.getP() - a.getP());
            case "PEAD" -> learning = Math.exp(Math.abs(b.getP() - a.getP()));
            case "UAD" -> learning = Math.abs(b.getU() - a.getU());
            case "UEAD" -> learning = Math.exp(Math.abs(b.getU() - a.getU()));
        }

        return learning;
    }





    /**
     * Exponential Roulette Wheel (ERW) Selection method compares fitness of child to neighbours. The
     * fitter the neighbour, the exponentially more likely they are to be selected as child's
     * parent.
     * @param child
     * @return parent
     */
    public Player selERW(Player child){
        Player parent = null;
        ArrayList <Player> pool = new ArrayList<>(child.getNeighbourhood()); // pool of candidates for parent
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        for(int i = 0; i < size; i++){
            pockets[i] = pool.get(i).getU();
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
        for(int i = 0; i < size; i++){
            tally += pockets[i];
            double percentile = tally / roulette_total;
            if(random_double < percentile){
                parent = pool.get(i);
                break;
            }
        }
        return parent;
    }






    /**
     * Normal Roulette Wheel (NRW) Selection method compares fitness of child to neighbours. The
     * fitter the neighbour, the exponentially more likely they are to be selected as child's
     * parent. If a parent is not selected, the child is selected as parent
     * by default.<br>
     */
    public Player selNRW(Player child){
        Player parent = null;
        ArrayList <Player> pool = new ArrayList<>(child.getNeighbourhood());
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        int i;
        for(i=0;i<size;i++){
//            pockets[i] = pool.get(i).getAvgScore();
            pockets[i] = pool.get(i).getU();
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
        for(i=0;i<size;i++){
            tally += pockets[i];
            double percentile = tally / roulette_total;
            if(random_double < percentile){
                parent = pool.get(i);
                break;
            }
        }

        return parent;
    }



//    /**
//     * Child selects highest scoring neighbour.
//     */
//    public Player selBest(Player child){
//        ArrayList <Player> neighbourhood = child.getNeighbourhood();
//        double avg_score = child.getAvgScore();
//        int size = neighbourhood.size();
//        double parent_avg_score;
//
//        // find highest scoring neighbour
//        Player parent = neighbourhood.get(0);
//        for(int i = 1; i < size; i++){
//            Player neighbour = neighbourhood.get(i);
//            double neighbour_avg_score = neighbour.getAvgScore();
//            parent_avg_score = parent.getAvgScore();
//            if(neighbour_avg_score > parent_avg_score){
//                parent = neighbour;
//            }
//        }
//
//        // if parent score less than child score, parent is child
//        parent_avg_score = parent.getAvgScore();
//        if(parent_avg_score <= avg_score){
//            parent = child;
//        }
//
//        return parent;
//    }


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



    /**
     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
     * The greater w (intensity of selection) is,
     * the more likely a fitter player is selected as child's parent.
     * @param child
     * @return parent player
     */
    public Player selRand(Player child){
        double w = selNoise;
        double[] effective_payoffs = new double[N];
        Player parent = null;
        double total = 0.0;
        double tally = 0.0;

        // calculate effective payoffs
        for(int i=0;i<N;i++){
            Player player = pop[i];
            double player_avg_score = player.getAvgScore();
            double player_effective_payoff = Math.exp(w * player_avg_score);
            effective_payoffs[i] = player_effective_payoff;
            total += player_effective_payoff;
        }

        // fitter player ==> more likely to be selected
        double random_double = ThreadLocalRandom.current().nextDouble();
        for(int i = 0; i < N; i++){
            tally += effective_payoffs[i];
            double percentile = tally / total;
            if(random_double < percentile){
                parent = pop[i];
                break;
            }
        }

        return parent;
    }



    // crossover where one child adopts midway point between two parent strategies.
//    public void crossover(Player child, Player parent1, Player parent2){
    public void crossover(Player child){

        // how to select parents?

        // select two fittest neighbours?
        ArrayList <Player> neighbourhood = child.getNeighbourhood();
        Player parent1 = child; // fittest neighbour
        Player parent2 = child; // second-fittest neighbour
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double neighbour_avg_score = neighbour.getAvgScore();
            double parent2_avg_score = parent2.getAvgScore();
            if(neighbour_avg_score > parent2_avg_score){
                parent2 = neighbourhood.get(i);
                parent2_avg_score = parent2.getAvgScore();
                double parent1_avg_score = parent1.getAvgScore();
                if(parent2_avg_score > parent1_avg_score){
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



//    public void setStrategy(Player player, double p, double q){
//        player.setP(p);
//        player.setQ(q);
//    }



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
            // case "PD"
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

//        setStrategy(child, new_p, new_q);
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

//        setStrategy(child, new_p,new_q);
        child.setP(new_p);
        child.setQ(new_q);
    }



    public void calculateAverageEdgeWeights(){
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
                        " %-5s |" +//game
                        " %-10s |" +//runs
                        " %-10s |" +//iters
                        " %-15s |" +//space
                        " %-15s |" +//neigh
                        " %-15s |" +//EM
                        " %-30s |" +//EW
                        " %-25s |" +//EWL
                        " %-15s |" +//sel
                        " %-15s |" +//evo
                        " %-15s |" +//mut
                        " %-10s |" +//UF
                        " %-10s |" +//dataRate
                        " %-25s |" +//series
                        " %-20s |" +//leeway
                        " %-15s |" +//inj
                        " desc%n" // ensure desc is the last column
                ,"config"
                ,"game"
                ,"runs"
                ,"iters"
                ,"space"
                ,"neigh"
                ,"EM"
                ,"EW"
                ,"EWL"
                ,"sel"
                ,"evo"
                ,"mut"
                ,"UF"
                ,"dataRate"
                ,"series"
                ,"leeway"
                ,"inj"
        );
        printTableBorder();

        // display config table rows
        int CI; // configuration index
        String[] settings;
        for(int i=0;i<configurations.size();i++){
            settings = configurations.get(i).split(",");
            CI = 0; // reset to 0 for each config
            System.out.printf("%-10d ", i); //config
            System.out.printf("| %-5s ", settings[CI++]); //game
            System.out.printf("| %-10s ", settings[CI++]); //runs
            System.out.printf("| %-10s ", settings[CI++]); //iters
            System.out.printf("| %-15s ", settings[CI++]); //space
            System.out.printf("| %-15s ", settings[CI++]); //neigh
            System.out.printf("| %-15s ", settings[CI++]); //EM
            System.out.printf("| %-30s ", settings[CI++]); //EW
            System.out.printf("| %-25s ", settings[CI++]); //EWL
            System.out.printf("| %-15s ", settings[CI++]); //sel
            System.out.printf("| %-15s ", settings[CI++]); //evo
            System.out.printf("| %-15s ", settings[CI++]); //mut
            System.out.printf("| %-10s ", settings[CI++]); //UF
            System.out.printf("| %-10s ", settings[CI++]); //dataRate
            System.out.printf("| %-25s ", settings[CI++]); //series
            System.out.printf("| %-20s ", settings[CI++]); //leeway
            System.out.printf("| %-15s ", settings[CI++]); //inj
            System.out.printf("| %s ", settings[CI]); //desc
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
        game = settings[CI++];
        Player.setGame(game);
        runs = Integer.parseInt(settings[CI++]);
        iters = Integer.parseInt(settings[CI++]);

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
            neighRange = Integer.parseInt(neigh_params[CI2++]);
        } else if(neighType.equals("random")){
            neighSize = Integer.parseInt(neigh_params[CI2++]);
        }

        String[] EM_params = settings[CI++].split(" "); // evolution mechanism parameters
        CI2 = 0;
        EM = EM_params[CI2++];
        if(EM.equals("ER"))
            ER = Integer.parseInt(EM_params[CI2++]);
        else if(EM.equals("MC")) {
            if (EM_params[CI2].equals("N"))
                NIS = N;
            else
                NIS = Integer.parseInt(EM_params[CI2++]);
        }

        String[] EW_params = settings[CI++].split(" "); // edge weight parameters
        CI2 = 0;
        EWT = EW_params[CI2++];
        if(EWT.equals("rewire")){
            RP = Double.parseDouble(EW_params[CI2++]);
            RA = EW_params[CI2++];
            RT = EW_params[CI2++];
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
            EWLP = EWL_params[CI2++];
        }

        String[] sel_params = settings[CI++].split(" "); // selection parameters
        CI2 = 0;
        sel = sel_params[CI2++];
        if(sel.equals("rand")){
            selNoise = Double.parseDouble(sel_params[CI2++]);
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
        dataRate = settings[CI].equals("")? 0: Integer.parseInt(settings[CI]);
        CI++;

        String[] series_params = settings[CI++].split(" ");
        CI2 = 0;
        varying = series_params[CI2++];
        if(!series_params[0].equals("")){
            variation = Double.parseDouble(series_params[CI2++]);
            numExp = Integer.parseInt(series_params[CI2++]);
        }

        String[] leeway_params = settings[CI++].split(" "); // leeway parameters
        if(!leeway_params[0].equals("")){
            CI2 = 0;
            leeway1 = Double.parseDouble(leeway_params[CI2++]);
            leeway2 = Double.parseDouble(leeway_params[CI2++]);
            leeway3 = Double.parseDouble(leeway_params[CI2++]);
            leeway4 = Double.parseDouble(leeway_params[CI2++]);
            leeway5 = Double.parseDouble(leeway_params[CI2++]);
            leeway6 = Double.parseDouble(leeway_params[CI2++]);
            leeway7 = Double.parseDouble(leeway_params[CI2++]);
        }

        String[] inj_params = settings[CI++].split(" "); // injection parameters
        if(!inj_params[0].equals("")){
            CI2 = 0;
            injIter = Integer.parseInt(inj_params[CI2++]);
            injP = Double.parseDouble(inj_params[CI2++]);
            injSize = Integer.parseInt(inj_params[CI2++]);
        }

        desc = settings[CI];
    }



    /**
     * Initialises a lattice grid population of players with randomly generated strategies.
     */
    public void initRandomPop(){
        pop = new Player[N];
        Player.setCount(0);
        int index = 0;
        if(space.equals("grid")){
            for(int y=0;y<length;y++){
                for(int x=0;x<width;x++){
                    Player new_player = null;
//                    switch(game){
//                        case"UG","DG"->{
//                            double p = ThreadLocalRandom.current().nextDouble();
//                            double q = 0;
//                            switch(game){
//                                case"UG"->q=ThreadLocalRandom.current().nextDouble();
//                                case"DG"->q=0;
//                            }
//                            new_player=new Player(x,y,p,q,leeway2);
//                        }
//                    }


                    switch(game){
                        case "UG" -> {
                            double p = ThreadLocalRandom.current().nextDouble();
                            double q = ThreadLocalRandom.current().nextDouble();
//                            new_player = new Player(x, y, p, q, leeway2);
                            new_player = new Player(x, y, p, q);
                        }
                        case "DG" -> {
                            double p = ThreadLocalRandom.current().nextDouble();
//                            new_player = new Player(x, y, p, 0.0, leeway2);
                            new_player = new Player(x, y, p, 0.0);
                        }
                        case "PD" -> {}
                    }


                    pop[index] = new_player;
                    index++;
                }
            }
        }else if(space.equals("hex")){
            // maybe implement this in future
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



    // Create folders to store data.
    public static void createFolders(){
//        if(game.equals("UG") || game.equals("DG")){
//            p_data_filename = experiment_results_folder_path + "\\p_data";
//            if(game.equals("UG")){
//                q_data_filename = experiment_results_folder_path + "\\q_data";
//            }
//        }


        switch(game){
            case "UG" -> {
                p_data_filename = experiment_results_folder_path + "\\p_data";
                q_data_filename = experiment_results_folder_path + "\\q_data";
            }
            case "DG" -> p_data_filename = experiment_results_folder_path + "\\p_data";
            case "PD" -> {}
        }


        edge_count_data_filename = experiment_results_folder_path + "\\edge_count_data";
        EW_data_filename = experiment_results_folder_path + "\\EW_data";
//        NSI_data_filename = experiment_results_folder_path + "\\NSI_data";
        try{
//            if(game.equals("UG") || game.equals("DG")){
//                Files.createDirectories(Paths.get(p_data_filename));
//                if(game.equals("UG")){
//                    Files.createDirectories(Paths.get(q_data_filename));
//                }
//            }


            switch(game){
                case "UG" -> {
                    Files.createDirectories(Paths.get(p_data_filename));
                    Files.createDirectories(Paths.get(q_data_filename));
                }
                case "DG" -> Files.createDirectories(Paths.get(p_data_filename));
                case "PD" -> {}
            }


            Files.createDirectories(Paths.get(edge_count_data_filename));
            Files.createDirectories(Paths.get(EW_data_filename));
//            Files.createDirectories(Paths.get(NSI_data_filename));
        }catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Prints path of experiment results folder.
     */
    public static void printExperimentResultsFolderPath(){
        System.out.println("Experiment results folder path: \n" + experiment_results_folder_path);
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
        for(int i=1;i<=neighRange;i++){
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



//    /**
//     * Calculate the average value of p and the standard deviation of p across the population
//     * at the current generation.<br>
//     * Assumes the game is UG/DG.
//     */
//    public void calculateStatsUG(){
//        // calculate average p
//        avg_p = 0;
//        for(int i=0;i<N;i++){
////            Player player = pop[i];
////            double p = player.getP();
////            avg_p += p;
//
//            avg_p += pop[i].getP();
//        }
//        avg_p /= N;
//
//        // calculate p SD
//        p_SD = 0;
//        for(int i=0;i<N;i++){
//            Player player = pop[i];
//            double p = player.getP();
//            p_SD += Math.pow(p - avg_p, 2);
//        }
//        p_SD = Math.pow(p_SD / N, 0.5);
//
//
////        // calculate average q
////        avg_q = 0;
////        for(int i=0;i<N;i++){
//////            Player player = pop[i];
//////            double q = player.getQ();
//////            avg_q += q;
////        }
////        avg_q /= N;
////
////        // calculate p SD
////        q_SD = 0;
////        for(int i=0;i<N;i++){
////            Player player = pop[i];
////            double q = player.getQ();
////            q_SD += Math.pow(q - avg_q, 2);
////        }
////        q_SD = Math.pow(q_SD / N, 0.5);
//
//
//        if(game.equals("UG")){
//            avg_p = 0;
//            for(int i=0;i<N;i++){
//
//            }
//        }
//    }
//    // calculate p and/or q stats of population
//    public void calculateStatsUG(){
//        avg_p = 0;
//        for(int i = 0; i < N; i++){
//            avg_p += pop[i].getP();
//        }
//        avg_p /= N;
//        p_SD = 0;
//        for(int i = 0; i < N; i++){
//            p_SD += Math.pow(pop[i].getP() - avg_p, 2);
//        }
//        p_SD = Math.pow(p_SD / N, 0.5);
//        if(game.equals("UG")){
//            avg_q = 0;
//            for(int i = 0; i < N; i++){
//                avg_q += pop[i].getQ();
//            }
//            avg_q /= N;
//            q_SD = 0;
//            for(int i = 0; i < N; i++){
//                q_SD += Math.pow(pop[i].getQ() - avg_q, 2);
//            }
//            q_SD = Math.pow(q_SD / N, 0.5);
//        }
//    }
    // calculate p and/or q stats of population
    public void calculateStatsUG(){
        switch(game){
            case "UG" -> {
                avg_p = 0;
                for(int i = 0; i < N; i++){
                    avg_p += pop[i].getP();
                }
                avg_p /= N;
                p_SD = 0;
                for(int i = 0; i < N; i++){
                    p_SD += Math.pow(pop[i].getP() - avg_p, 2);
                }
                p_SD = Math.pow(p_SD / N, 0.5);
                avg_q = 0;
                for(int i = 0; i < N; i++){
                    avg_q += pop[i].getQ();
                }
                avg_q /= N;
                q_SD = 0;
                for(int i = 0; i < N; i++){
                    q_SD += Math.pow(pop[i].getQ() - avg_q, 2);
                }
                q_SD = Math.pow(q_SD / N, 0.5);
            }
            case "DG" -> {
                avg_p = 0;
                for(int i = 0; i < N; i++){
                    avg_p += pop[i].getP();
                }
                avg_p /= N;
                p_SD = 0;
                for(int i = 0; i < N; i++){
                    p_SD += Math.pow(pop[i].getP() - avg_p, 2);
                }
                p_SD = Math.pow(p_SD / N, 0.5);
            }
            case "PD" -> {}
        }
    }





    /**
     * Prepare program for the next generation.<br>
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void prepare(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            player.setScore(0);
            player.setMNI(0);
//            player.setNSI(0);
//            switch(game){
//                case"UG","DG"->{
//                    player.setOldP(player.getP());
//                    switch(game){
//                        case"UG"->player.setOldQ(player.getQ());
//                        case"DG"->player.setQ(0);
//                    }
//                }
//            }


            switch(game){
                case "UG" -> {
                    player.setOldP(player.getP());
                    player.setOldQ(player.getQ());
                }
                case "DG" -> player.setOldP(player.getP());
                case "PD" -> {}
            }
        }
    }



    /**
     * Allows for the visualisation of the avg p of a run with respect to iteration.<br>
     * iteration on x-axis.<br>
     * avg p on y-axis.<br>
     * collects standard deviation (SD) data.<br>
     * <br>Steps:<br>
     * - Export the data of a single run to a .csv file<br>
     * - Import the .csv data into an Excel sheet<br>
     * - Separate the data into columns: gen number, avg p and SD for that gen<br>
     * - Create a line chart with the data.<br>
     */
    public void writeAvgPData(){
        try{
            String filename = experiment_results_folder_path + "\\avg_p_data.csv";
            String s="";
            if(gen == 0){ // apply headings to file before writing data
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                s+="gen";
                s+=",avg p";
                s+=",p SD";
                s+="\n";
            }
            fw = new FileWriter(filename, true);
            s+=gen;
            s+=","+DF4.format(avg_p);
            s+=","+DF4.format(p_SD);
            s+="\n";
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    public void writeAvgQData(){
        try{
            String filename = experiment_results_folder_path + "\\avg_q_data.csv";
            String output = "";
            if(gen == 0){
                fw = new FileWriter(filename, false);
                output += "gen";
                output += ",avg q";
                output += ",q SD";
                output += "\n";
            }
            fw = new FileWriter(filename, true);
            output += gen;
            output += ","+DF4.format(avg_q);
            output += ","+DF4.format(q_SD);
            output += "\n";
            fw.append(output);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    // writes IDs and positions of players
    public void writePosData(){
        try{
            String filename = experiment_results_folder_path + "\\pos_data.csv";
            fw = new FileWriter(filename, false);
            String s = "";
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    int ID = player.getID();
                    s += ID + " ("+x+" "+y+"),";
                }
                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    // Saves proposal values of pop to .csv file.
    public void writePData(){
        try{
            String filename = p_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
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
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    public void writeQData(){
        try{
            String filename = q_data_filename +"\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
            String output = "";
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    double q = player.getQ();
                    output += DF4.format(q) + ",";
                }
                output += "\n";
            }
            fw.append(output);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Records aggregate EW data by writing a grid of 4x4 sub-grids.<br>
     * Assumes von Neumann, square grid.<br>
     * {@code EW_health} equals sum of weights divided by num weights.
     */
    public void writeEWData(){
        try{
            String filename = EW_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename);
            String string = "";
            String[] substrings = new String[(length * 4)];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player current = findPlayerByPos(y,x);
                    ArrayList <Double> weights = current.getEdgeWeights();
                    ArrayList<Player> neighbourhood = current.getNeighbourhood();
                    double EW_health = (current.getMeanSelfEdgeWeight() + current.getMeanNeighbourEdgeWeight()) / 2;
                    substrings[a] += "0,"
                            +weights.get(2)+","
                            +neighbourhood.get(2).getEdgeWeights().get(3)+","
                            +"0";
                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights().get(0)+","
                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","
                            +weights.get(0);
                    substrings[a+2] += current.getEdgeWeights().get(1)+","
                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","
                            +neighbourhood.get(0).getEdgeWeights().get(1);
                    substrings[a+3] += "0,"
                            +neighbourhood.get(3).getEdgeWeights().get(2)+","
                            +weights.get(3)+","
                            +"0";
                    if(x + 1 < width){
                        for(int b=a;b<a+4;b++){
                            substrings[b] += ",";
                        }
                    } else {
                        for(int b=a;b<a+4;b++){
                            substrings[b] += "\n";
                        }
                    }
                }
                a += 4;
            }
            for(int i=0;i<substrings.length;i++){
                string += substrings[i];
            }
            fw.append(string);
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }




    /**
     * Uses NSI data to write a grid of 4x4 sub-grids into a .csv file.<br>
     * Assumes von Neumann neighbourhood type and rows=columns.<br>
     *
     *
     * DISCONTINUED FOR NOW...
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
    public void rewireToLocal(Player a, int num_rewires){
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
            rewireToPop(a, num_rewires);
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
    public void rewireToPop(Player a, int b){
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
        player.setScore(player.getScore() + payoff);
        player.setMNI(player.getMNI() + 1);
    }



    public int rewireAway0Many(Player a){
        // omega_a denotes copy of neighbourhood of a.
        ArrayList<Player> omega_a = new ArrayList<>(a.getNeighbourhood());

        // denotes list of weighted edges connecting a to its neigbours.
        ArrayList<Double> weights = new ArrayList(a.getEdgeWeights());

        // denotes pool of rewireable edges represented by their indices.
        ArrayList<Integer> rewire_edge_indices = new ArrayList();

        // b_index denotes index of edge w_ab that connects a to neighbour b.
        for(int b_index = 0; b_index < weights.size(); b_index++){
            double w = weights.get(b_index);
            if(w == 0){
                rewire_edge_indices.add(b_index);
            }
        }

        int num_rewirable_edges = rewire_edge_indices.size();

        // supports the process of rewiring to new neighbour.
        int num_rewires = num_rewirable_edges;

        // you could use a while loop like this if you were rewiring multiple edges.
        while(num_rewirable_edges > 0){

            // randomly select an edge to cut. the randomness here makes it so that we dont know which neighbour is necessarily going to be rewired away from first. relevant if rewiring is isolation is being prevented (since we then dont know which neighbour is going to get away with being pointed at by a 0 weight edge!)
            int c_index = rewire_edge_indices.get(ThreadLocalRandom.current().nextInt(num_rewirable_edges));

            // c denotes neighbour of a.
            Player c = omega_a.get(c_index);

            // omega_c denotes neighbourhood of c.
            ArrayList<Player> omega_c = c.getNeighbourhood();

            // do not rewire if c has < 2 edges.
            //if(omega_c.size() > 1){


            // d denotes neighbour of c.
            for(int d_index = 0; d_index < omega_c.size(); d_index++){
                Player d = omega_c.get(d_index);

                // if d = a, then d_index is the index/location of a in omega_c.
                if(d.equals(a)){

                    // disconnect a from c.
                    omega_a.remove(c_index);
                    weights.remove(c_index);

                    // disconnect c from a.
                    omega_c.remove(d_index);
                    c.getEdgeWeights().remove(d_index);

                    // once the cutting of edges has been completed, stop looping.
                    break;
                }
            }

            num_rewirable_edges--; // you could do this if you were rewiring multiple edges.

        }

        a.setNeighbourhood(omega_a);
        a.setEdgeWeights(weights);

        return num_rewires;

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




    public void writeEdgeCountData(){
        try{
            String filename = edge_count_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
            String s = "";
            int actual_total_edges = 0;
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    int n_omega = player.getNeighbourhood().size();

                    actual_total_edges += n_omega;

                    s += n_omega;
                    if(x + 1 < length){
                        s += ",";
                    }
                }
                s += "\n";
            }
            int expected_total_edges = N * 4;
            if(actual_total_edges != expected_total_edges){
                System.out.println("ERROR: assuming 4 initial edges per player, total edges must equal N * 4!");
                Runtime.getRuntime().exit(0);
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * use the varying field to help determine whether a field should be
     * included in the settings String. if a field equals 0.0 and is not being
     * varied, it does not need to be added.<br>
     */
    public static void writeSettings(){
        String settings_filename = experiment_results_folder_path + "\\" + "settings.csv";
        String settings = "";
        try{
            if(experiment_num == 1){
                fw = new FileWriter(settings_filename, false);
                settings += "game";
                settings += ",runs";
                settings += ",iters";
                settings += ",space";
                settings += length == 0 && !varying.equals("length")? "": ",length";
                settings += width == 0 && !varying.equals("width")? "": ",width";
                settings += ",neighType";
                settings += neighRange == 0 && !varying.equals("neighRange")? "": ",neighRange";
                settings += neighSize == 0 && !varying.equals("neighSize")? "": ",neighSize";
                settings += ",EM";
                settings += ER == 0 && !varying.equals("ER")? "": ",ER";
                settings += NIS == 0 && !varying.equals("NIS")? "": ",NIS";
                settings += ",EWT";
                settings += RP == 0.0 && !varying.equals("RP")? "": ",RP";
                settings += RA.equals("")? "": ",RA";
                settings += RT.equals("")? "": ",RT";
//                settings += !EWLC.equals("")? "": ",EWLC";
                settings += EWLF.equals("")? "": ",EWLF";
                settings += ROC == 0.0 && !varying.equals("ROC")? "": ",ROC";
                settings += alpha == 0.0 && !varying.equals("alpha")? "": ",alpha";
                settings += beta == 0.0 && !varying.equals("beta")? "": ",beta";
                settings += EWLP.equals("")? "": ",EWLP";
                settings += ",sel";
                settings += selNoise == 0.0 && !varying.equals("selNoise")? "": ",selNoise";
                settings += ",evo";
                settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": ",evoNoise";
                settings += mut.equals("")? "": ",mut";
                settings += mutRate == 0.0 && !varying.equals("mutRate")? "": ",mutRate";
                settings += mutBound == 0.0 && !varying.equals("mutBound")? "": ",mutBound";
                settings += ",UF";
                settings += leeway1 == 0.0 && !varying.equals("leeway1")? "": ",leeway1";
                settings += leeway2 == 0.0 && !varying.equals("leeway2")? "": ",leeway2";
                settings += leeway3 == 0.0 && !varying.equals("leeway3")? "": ",leeway3";
                settings += leeway4 == 0.0 && !varying.equals("leeway4")? "": ",leeway4";
                settings += leeway5 == 0.0 && !varying.equals("leeway5")? "": ",leeway5";
                settings += leeway6 == 0.0 && !varying.equals("leeway6")? "": ",leeway6";
                settings += leeway7 == 0.0 && !varying.equals("leeway7")? "": ",leeway7";
                settings += injIter == 0? "": ",injIter";
                settings += injP == 0.0? "": ",injP";
                settings += injSize == 0? "": ",injSize";
            } else {
                fw = new FileWriter(settings_filename, true);
            }
            settings += "\n";
            settings += game;
            settings += "," + runs;
            settings += "," + iters;
            settings += "," + space;
            settings += length == 0 && !varying.equals("length")? "": "," + length;
            settings += width == 0 && !varying.equals("width")? "": "," + width;
            settings += "," + neighType;
            settings += neighRange == 0 && !varying.equals("neighRange")? "": "," + neighRange;
            settings += neighSize == 0 && !varying.equals("neighSize")? "": "," + neighSize;
            settings += "," + EM;
            settings += ER == 0 && !varying.equals("ER")? "": "," + ER;
            settings += NIS == 0 && !varying.equals("NIS")? "": "," + NIS;
            settings += "," + EWT;
            settings += RP == 0.0 && !varying.equals("RP")? "": "," + RP;
            settings += RA.equals("")? "": "," + RA;
            settings += RT.equals("")? "": "," + RT;
            settings += EWLF.equals("")? "": "," + EWLF;
            settings += ROC == 0.0 && !varying.equals("ROC")? "": "," + ROC;
            settings += alpha == 0.0 && !varying.equals("alpha")? "": "," + alpha;
            settings += beta == 0.0 && !varying.equals("beta")? "": "," + beta;
            settings += EWLP.equals("")? "": "," + EWLP;
            settings += "," + sel;
            settings += selNoise == 0.0 && !varying.equals("selNoise")? "": "," + selNoise;
            settings += "," + evo;
            settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": "," + evoNoise;
            settings += mut.equals("")? "": "," + mut;
            settings += mutRate == 0.0 && !varying.equals("mutRate")? "": "," + mutRate;
            settings += mutBound == 0.0 && !varying.equals("mutBound")? "": "," + mutBound;
            settings += "," + UF;
            settings += leeway1 == 0.0 && !varying.equals("leeway1")? "": "," + leeway1;
            settings += leeway2 == 0.0 && !varying.equals("leeway2")? "": "," + leeway2;
            settings += leeway3 == 0.0 && !varying.equals("leeway3")? "": "," + leeway3;
            settings += leeway4 == 0.0 && !varying.equals("leeway4")? "": "," + leeway4;
            settings += leeway5 == 0.0 && !varying.equals("leeway5")? "": "," + leeway5;
            settings += leeway6 == 0.0 && !varying.equals("leeway6")? "": "," + leeway6;
            settings += leeway7 == 0.0 && !varying.equals("leeway7")? "": "," + leeway7;
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
        String results_filename = experiment_results_folder_path + "\\" + "results.csv";
        String results = "";
        try{
            if(experiment_num == 1){
                fw = new FileWriter(results_filename, false);
//                if(game.equals("UG") || game.equals("DG")){
//                    results += "mean avg p";
//                    results += ",avg p SD";
//                    if(game.equals("UG")){
//                        results += ",mean avg q";
//                        results += ",avg q SD";
//                    }
//                }


                switch(game){
                    case "UG" -> {
                        results += "mean avg p";
                        results += ",avg p SD";
                        results += ",mean avg q";
                        results += ",avg q SD";
                    }
                    case "DG" -> {
                        results += "mean avg p";
                        results += ",avg p SD";
                    }
                    case "PD" -> {}
                }


                results += "," + varying;
            }else {
                fw = new FileWriter(results_filename, true);
            }
            results += "\n";
//            if(game.equals("UG") || game.equals("DG")){
//                results += DF4.format(experiment_mean_avg_p);
//                results += "," + DF4.format(experiment_SD_avg_p);
//                if(game.equals("UG")){
//                    results += "," + DF4.format(experiment_mean_avg_q);
//                    results += "," + DF4.format(experiment_SD_avg_q);
//                }
//            }


            switch(game){
                case "UG" -> {
                    results += DF4.format(experiment_mean_avg_p);
                    results += "," + DF4.format(experiment_SD_avg_p);
                    results += "," + DF4.format(experiment_mean_avg_q);
                    results += "," + DF4.format(experiment_SD_avg_q);
                }
                case "DG" -> {
                    results += DF4.format(experiment_mean_avg_p);
                    results += "," + DF4.format(experiment_SD_avg_p);}
                case "PD" -> {}
            }


            // write value of varying parameter.
            switch(varying){
                case "runs" -> results += "," + runs;
                case "ER" -> results += "," + ER;
                case "NIS" -> results += "," + NIS;
                case "ROC" -> results += "," + ROC;
                case "length" -> results += "," + length;
                case "width" -> results += "," + width;
                case "RP" -> results += "," + RP;
                case "iters" -> results += "," + iters;
            }
            fw.append(results);
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }



    // chance to rewire is equal to 1 - w. as w decreases, prob linearly increases.
    public int rewireAwayLinear(Player a){
        // denotes number of rewires a has performed (this gen).
        int num_rewires = 0;

        // omega_a denotes neighbourhood of a.
        ArrayList<Player> omega_a = a.getNeighbourhood();

        // denotes degree of a
        int degree_a = omega_a.size();

        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
        for(int i = 0; i < degree_a; i++){
            double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
            double prob_rewire = 1 - w_ab; // denotes probability of rewiring away from b
            double c = ThreadLocalRandom.current().nextDouble();
            if(prob_rewire > c){
                indices_of_edges_to_be_rewired.add(i);
            }
        }

        // iterating backwards means with multiple edges to rewire,
        // as items are removed from arraylist, the subsequent shuffling of
        // items does not cause an IndexOutOfBoundsException.
        // if removing edges at indices 2 and 3 using forward iteration,
        // if you remove edge at 2 first, there no longer exists an
        // edge at index 3. therefore when we then try to remove an edge at 3, we
        // encounter an IndexOutOfBoundsException.
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



    /**
     * with UF MNI, utility is basically equivalent to the old average score metric.
     */
    public void updateUtility(Player player){
        switch(UF){
            case "MNI" -> player.setU(player.getScore() / player.getMNI()); // minimum number of interactions; with neighType="VN", neighRange=1, no rewiring, denominator is always 8.
            case "cumulative" -> player.setU(player.getScore()); // cumulative payoff
            case "normalised" -> player.setU(player.getScore() / player.getNeighbourhood().size()); // normalised payoff; with neighType="VN", neighRange=1, no rewiring, denominator is always 4.
        }
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




    // probability to rewire is calculated using smoothstep function.
    public int rewireAwaySmoothstep(Player a){
        int num_rewires = 0;
        ArrayList<Player> omega_a = a.getNeighbourhood();
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
        for(int i = 0; i < omega_a.size(); i++){
            double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
            double prob_rewire = 1 - (3 * Math.pow(w_ab, 2) - 2 * Math.pow(w_ab, 3));
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
        return num_rewires;
    }
}

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * 02/10/2023<br>
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
    static String neigh; // indicates the type of neighbourhood being enforced
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // 2D square lattice contains the population
//    Player[][] pop; // array of players; assumes 2D space
    Player[]pop; // array of players; assumes 2D space
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    int gen = 0; // indicates current generation. marks the progression of evo.
    int iter = 1; // indicates current iteration. each gen is made up of ER iters.
//    static double DCF = 0; // distance cost factor
    static String initial_settings = ""; // stores initial experimentation settings
    static int injiter; // injection iteration: indicates when strategy injection will occur. 0 ==> no injection.
    static double injp = 0.0; // injection p: indicates p value to be injected
    static int injsize = 0; // injection cluster size: indicates size of cluster to be injected
    static double T; // PD: temptation to defect
    static double R; // PD: reward for mutual coopeation
    static double P; // PD: punishment for mutual defection
    static double S; // PD: sucker's payoff for cooperating with a defector
    static double l; // loner's payoff
//    static double gross_prize_UG = 1.0; // default prize per UG interaction. as long as value != 0, value doesnt matter if fitness metric is avg score rather than score.
    static double prize = 1.0; // default prize per UG interaction. as long as value != 0, value doesnt matter if fitness metric is avg score rather than score.
    static String ASD; // average score denominator: determines how average score is calculated
    double avg_q; // average acceptance threshold across population
    double q_SD; // the standard deviation of q across the pop
    static String space; // the space the population exists within


    // fields related to experiment statistics
    static boolean experiment_series; //indicates whether to run experiment or experiment series where a parameter is varied
    static String varying; //indicates which parameter will be varied in an experiment series
    static double variation; //indicates by how much parameter will vary between subsequent experiments. the double data is used because it works for varying integer parameters as well as doubles.
    static int numexp; //indicates the number of experiments to occur in the series
    static int experiment_num = 1; //tracks which experiment is taking place at any given time during a series
    static int run_num; // tracks which of the runs is currently executing
    static ArrayList<Double> various_amounts;
    static double experiment_mean_avg_p;
    static double experiment_mean_avg_q;
    static double experiment_SD_avg_p;
    static double experiment_SD_avg_q;


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
    static String NSI_data_filename;
    static int datarate; // factors into which gens have their data recorded. if 0, no gens are recorded.
    static String[] settings;
    static int CI; // config index: facilitates construction of table of configs
    static String q_data_filename;
    String[] neigh_params;
    static String pos_data_filename;


    // fields related to edge weight learning (EWL)
    static String EWT; // edge weight type
    static String EWLC; // edge weight learning condition
    static String EWLF; // edge weight learning formula
    static int ER = 1; // evolution rate: indicates how many iterations occur each generation. e.g. ER=5 means every gen has 5 iters
    static double ROC = 0; // rate of edge weight change per edge weight learning call


    static double alpha = 0;
    static double beta = 0;


    static double leeway1 = 0; // defines global leeway affecting all players.
    static double leeway2 = 0; // defines bounds of interval that local leeway is generated from.
    static double leeway3 = 0; // defines factor used in calculation of leeway given wrt weight of edge to neighbour.
    static double leeway4 = 0; // defines factor used in calculation of leeway given wrt comparison of p vs neighbour p.
    static double leeway5 = 0; // defines factor used in calculation of leeway given wrt neighbour p.
    static double leeway6 = 0; // defines bounds of interval that random leeway is generated from.
    static double leeway7 = 0; // defines avg score comparison leeway factor.


    // fields related to evolution, selection, mutation
    static String evo; // indicates which evolution function to call
    static double evonoise = 0; // noise affecting evolution
    static String sel; // indicates which selection function to call
    static double selnoise = 0; // noise affecting selection
    static String mut; // indicates which mutation function to call
    static double mutrate = 0; // probability of mutation
    static double mutbound = 0; // denotes max mutation possible






    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        setupEnvironment();
        setInitialSettings();
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
        if(datarate != 0) {
            createFolders();
        }


        System.out.println("Starting timestamp: "+start_timestamp);
        if(experiment_series){
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
        System.out.println();
        printInitialSettings();


        various_amounts = new ArrayList<>(); // stores initial value of varying parameter
        switch(varying){
            case"runs"->various_amounts.add((double)runs);
            case"iters"->various_amounts.add((double)iters);
            case"length"->various_amounts.add((double)length);
            case"width"->various_amounts.add((double)width);
            case"ER"->various_amounts.add((double)ER);
            case"ROC"->various_amounts.add(ROC);
            case"leeway1"->various_amounts.add(leeway1);
            case"leeway2"->various_amounts.add(leeway2);
            case"leeway3"->various_amounts.add(leeway3);
            case"leeway4"->various_amounts.add(leeway4);
            case"leeway5"->various_amounts.add(leeway5);
            case"leeway6"->various_amounts.add(leeway6);
            case"leeway7"->various_amounts.add(leeway7);
            case"selnoise"->various_amounts.add(selnoise);
            case"evonoise"->various_amounts.add(evonoise);
            case"mutrate"->various_amounts.add(mutrate);
            case"mutbound"->various_amounts.add(mutbound);
            case"injiter"->various_amounts.add((double)injiter);
            case"injp"->various_amounts.add(injp);
            case"injsize"->various_amounts.add((double)injsize);
        }

        // run experiment series
        for(int i=0;i<numexp;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("\n===================\n" +
                    "NOTE: Start of experiment "+experiment_num+": "+varying+"="+DF4.format(various_amounts.get(i))+".");

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
                case "selnoise"->{
                    selnoise+=variation;
                    various_amounts.add(selnoise);
                }
                case "evonoise"->{
                    evonoise+=variation;
                    various_amounts.add(evonoise);
                }
                case "mutrate"->{
                    mutrate+=variation;
                    various_amounts.add(mutrate);
                }
                case "mutbound"->{
                    mutbound+=variation;
                    various_amounts.add(mutbound);
                }
                case "injiter"->{
                    injiter += (int) variation;
                    various_amounts.add((double) injiter);
                }
                case "injp"->{
                    injp += variation;
                    various_amounts.add(injp);
                }
                case "injsize"->{
                    injsize += (int) variation;
                    various_amounts.add((double) injsize);
                }
            }

            // inform user what the varying parameter's value was
            System.out.println("NOTE: End of "+varying+"="+DF4.format(various_amounts.get(i))+"."
                    +"\n===================");
        }
    }



    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){
        if(!experiment_series){
            System.out.println();
            printInitialSettings();
        }

        // stats to be tracked
        experiment_mean_avg_p = 0;
        double[] experiment_avg_p_values = new double[runs + 1];
        experiment_SD_avg_p = 0;
        experiment_mean_avg_q = 0;
        double[] experiment_avg_q_values = new double[runs + 1];
        experiment_SD_avg_q = 0;

        // run experiment x times
        for(run_num = 1; run_num <= runs; run_num++){
            Env run = new Env(); // represents one run of experiment
            run.start();

            // collect data
            experiment_mean_avg_p += run.avg_p;
            experiment_avg_p_values[run_num] = run.avg_p;
            experiment_mean_avg_q += run.avg_q;
            experiment_avg_q_values[run_num] = run.avg_q;

            // display run results (varies depending on game)
            String output = "experiment "+experiment_num+", run "+run_num+": ";
            if(game.equals("UG") || game.equals("DG")){
                output += "avg p=" + DF4.format(run.avg_p);
                if(game.equals("UG")){
                    output += " avg q="+DF4.format(run.avg_q);
                }
            }
            // MAYBE INSERT THIS: else if game is PD, then print number of C, D and A players...
            System.out.println(output);

        }

        if(!experiment_series){
            System.out.println();
            printInitialSettings();
        }

        // calculate stats of experiment
        experiment_mean_avg_p /= runs;
        experiment_mean_avg_q /= runs;
        for(int i=0;i<runs;i++){
            experiment_SD_avg_p += Math.pow(experiment_avg_p_values[i] - experiment_mean_avg_p, 2);
            experiment_SD_avg_q += Math.pow(experiment_avg_q_values[i] - experiment_mean_avg_q, 2);

        }
        experiment_SD_avg_p = Math.pow(experiment_SD_avg_p / runs, 0.5);
        experiment_SD_avg_q = Math.pow(experiment_SD_avg_q / runs, 0.5);

        // display experiment results (varies depending on game)
        String output = "";
        if(game.equals("UG") || game.equals("DG")){
            output += "mean avg p=" + DF4.format(experiment_mean_avg_p)
                    + " avg p SD=" + DF4.format(experiment_SD_avg_p);
            if(game.equals("UG")){
                output += " mean avg q=" + DF4.format(experiment_mean_avg_q)
                        + " avg q SD=" + DF4.format(experiment_SD_avg_q);
            }
        }
        System.out.println(output);

        writeSeriesData();

        experiment_num++; // move on to the next experiment in the series
    }



    /**
     * Method for running the core algorithm at the heart of the program.
     */
    @Override
    public void start(){
        initRandomPop();

        for(int i=0;i<N;i++){
            Player player = pop[i];
            neigh_params = neigh.split(" ");
            switch(neigh_params[0]){
                case"VN","Moore","dia"->{
                    String type = neigh_params[0];
                    int distance = Integer.parseInt(neigh_params[1]);
//                    assignAdjacentNeighbours(player, i, j, type, distance);
                    assignAdjacentNeighbours(player, type, distance);
                }
                case"random"->{
                    String type = neigh_params[1];
                    int size = Integer.parseInt(neigh_params[2]);
                    assignRandomNeighbours(player, type, size);
                }
                case"all"->assignAllNeighbours(player);
//                case"Margolus"->assignMargolusNeighbourhood(player, i, j);
                default -> {
                    System.out.println("[ERROR] Invalid neighbourhood type configured. Exiting...");
                    Runtime.getRuntime().exit(0);
                }
            }
        }

        if(space.equals("grid")){
            writePosData();
        }

        // initialise neighbourhoods
        for(int i=0;i<N;i++){
            Player player = pop[i];
            initialiseEdgeWeights(player);
            assignNeighbourIDs(player);
            resetNSIPerNeighbour(player);
        }

        // population stops once this condition is reached
        while(iter <= iters) {

            // apply margolus neighbourhood if applicable.
//            if(neigh.split(" ")[0].equals("Margolus")){
//                for(int i=0;i<rows;i++) {
//                    for (int j = 0; j < columns; j++) {
//                        Player player = grid.get(i).get(j);
//                        assignMargolusNeighbourhood(player, i, j);
//                        initialiseEdgeWeights(player);
//                        assignNeighbourIDs(player);
//                        resetNSIPerNeighbour(player);
//                    }
//                }
//            }

            // injection
//            if(iter == injiter){
//                injectStrategyCluster(injp, injsize);
//            }

            // playing
            for(int i=0;i<N;i++){
                Player player = pop[i];
                play(player);
            }

            // edge weight learning
            for(int i=0;i<N;i++){
                Player player = pop[i];
                EWL(player);
            }

            // a generation occurs every ER iterations; evolution occurs each generation
            if(iter % ER == 0){
                for(int i=0;i<N;i++){
                    Player child = pop[i];

                    // rewiring
                    if(EWT.equals("3")){
                        rewire(child);
                    }

                    // selection
                    Player parent = null;
                    switch(sel){
                        case "RW" -> parent = RWSelection(child);
                        case "elitist" -> parent = bestSelection(child);
                        case "rand" -> parent = randSelection(child);
                        case "crossover" -> crossover(child); // "sel" and "evo" occur in one func
                        default -> {
                            System.out.println("[ERROR] Invalid selection function configured. Exiting...");
                            Runtime.getRuntime().exit(0);
                        }
                    }

                    // evolution
                    switch (evo) {
                        case "copy" -> copyEvolution(child, parent);
                        case "approach" -> approachEvolution(child, parent);
                        case "crossover" -> {} // do nothing; already completed above.
                        default -> {
                            System.out.println("[ERROR] Invalid evolution function configured. Exiting...");
                            Runtime.getRuntime().exit(0);
                        }
                    }

                    // mutation
                    switch (mut){
                        case "global" -> {
                            if(mutationCheck())globalMutation(child);
                        }
                        case "local" -> {
                            if(mutationCheck())localMutation(child);
                        }
                    }
                }
                gen++; // move on to the next generation
            }

            calculateOverallStats();
            calculateAverageEdgeWeights();

            // periodically record individual data
            if(datarate != 0){
                if(run_num == 1 // if first run
                        && experiment_num == 1 // if first experiment
                        && iter % (ER * datarate) == 0){ // record gen's data every x gens, where x=datarate
                    String output = "";
                    if(game.equals("UG") || game.equals("DG")){
                        output += "iter "+iter+", gen "+gen+": avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD);
                        writepData();
                        writeAvgpData();
                        if(game.equals("UG")){
                            output += " avg q="+DF4.format(avg_q)+", q SD="+DF4.format(q_SD);
                            writeqData();
                            writeAvgqData();
                        }
                    }
                    System.out.println(output);

                    if(!EWT.equals("3") && length==width && neigh.split(" ")[0].equals("VN")){
                        writeEWData();
                        writeNSIData();
                    }

                }
            }

            prepare();
            iter++; // move on to the next iteration
        }
    }



    public void play(Player player) {
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int ID = player.getID();

        // find partner
        for(int i = 0; i < neighbourhood.size(); i++){
            Player neighbour = neighbourhood.get(i);

            // partner finds player (use neighbourIDs to do this?)
            ArrayList <Player> neighbour_neighbourhood = neighbour.getNeighbourhood();
            for (int j = 0; j < neighbour_neighbourhood.size(); j++) {
                Player neighbours_neighbour = neighbour_neighbourhood.get(j);
                int neighbours_neighbour_ID = neighbours_neighbour.getID();
                if (neighbours_neighbour_ID == ID) {
//                    double[] neighbour_edge_weights = neighbour.getEdgeWeights();
//                    double neighbour_edge_weight = neighbour_edge_weights[j];
                    ArrayList <Double> neighbour_edge_weights = neighbour.getEdgeWeights();
                    double neighbour_edge_weight = neighbour_edge_weights.get(j);

                    // play
                    boolean interact = true; // by default, interaction occurs.
                    if(EWT.equals("1")){
                        double random_double = ThreadLocalRandom.current().nextDouble();
                        if(neighbour_edge_weight <= random_double){
                            interact = false;
                        }
                    }
                    if(interact){
                        switch(game){
                            case"UG","DG"->UG(player, neighbour, neighbour_edge_weight);
                            case"PD"->PD(player, neighbour, neighbour_edge_weight);
                        }
                    }
                    break;
                }
            }
        }
    }



//    public void UG(double net_prize, Player proposer, Player responder){
    public void UG(Player proposer, Player responder, double responder_edge_weight){
        double proposer_p = proposer.getP();
        double responder_q = responder.getQ();
        int proposer_ID = proposer.getID();
        int responder_ID = responder.getID();

        if(proposer_p >= responder_q){ // accept
            double proposer_payoff = prize - (prize * proposer_p);
            double responder_payoff = prize * proposer_p;

            // apply EWT 2 if applicable
            if(EWT.equals("2")){
                proposer_payoff *= responder_edge_weight;
                responder_payoff *= responder_edge_weight;
            }

            updateStatsUG(proposer, proposer_payoff, responder_ID, true);
            updateStatsUG(responder, responder_payoff, proposer.getID(), false);
        } else { // reject
            updateStatsUG(proposer, 0, responder_ID, true);
            updateStatsUG(responder, 0, proposer_ID, false);
        }
    }



    public void updateStatsUG(Player player, double payoff, int partner_ID, boolean proposer){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        double score = player.getScore();
        int NI = player.getNI();
        int NSI = player.getNSI();
        int NSP = player.getNSP();
        int NSR = player.getNSR();

        player.setScore(score + payoff);
        player.setNI(NI + 1);
        if(payoff > 0){
            player.setNSI(NSI + 1);
            for(int i = 0; i < neighbourhood.size(); i++){
                if(neighbourhood.get(i).getID() == partner_ID){
                    player.getNSIPerNeighbour()[i]++;
                    break;
                }
            }
            if(proposer)
                player.setNSP(NSP + 1);
            else
                player.setNSR(NSR + 1);
        }
        score = player.getScore();
        NI = player.getNI();
        NSI = player.getNSI();
        switch (ASD){
            case "NI" -> player.setAvgScore(score / NI);
            case "NSI" -> player.setAvgScore(score / NSI);

            default -> {
                System.out.println("[ERROR] Invalid average score denominator configured. Exiting...");
                Runtime.getRuntime().exit(0);
            }
        }
    }



//    public void PD(Player player, Player partner){
    public void PD(Player player, Player partner, double partner_edge_weight){
        String strategy_PD = player.getStrategyPD();
        String partner_strategy_PD = partner.getStrategyPD();

//        double score = player.getScore();
//        double partner_score = partner.getScore();

        double player_payoff = 0;
        double partner_payoff = 0;

        if(strategy_PD.equals("C") && partner_strategy_PD.equals("C")){
//            score += R;
//            partner_score += R;
//            updateStatsPD(player, R);
//            updateStatsPD(partner, R);
            player_payoff = R;
            partner_payoff = R;
        } else if(strategy_PD.equals("C") && partner_strategy_PD.equals("D")){
//            score += S;
//            partner_score += T;
//            updateStatsPD(player, S);
//            updateStatsPD(partner, T);
            player_payoff = S;
            partner_payoff = T;
        }else if(strategy_PD.equals("D") && partner_strategy_PD.equals("C")){
//            score += T;
//            partner_score += S;
//            updateStatsPD(player, T);
//            updateStatsPD(partner, S);
            player_payoff = T;
            partner_payoff = S;
        }else if(strategy_PD.equals("D") && partner_strategy_PD.equals("D")){
//            score += P;
//            partner_score += P;
//            updateStatsPD(player, P);
//            updateStatsPD(partner, P);
            player_payoff = P;
            partner_payoff = P;
        }else if(strategy_PD.equals("A") || partner_strategy_PD.equals("A")){
//            score += l;
//            partner_score += l;
//            updateStatsPD(player, l);
//            updateStatsPD(partner, l);
            player_payoff = l;
            partner_payoff = l;
        }

//        NI++;
//        partner.NI++;
//        avg_score = score / NI;
//        partner.avg_score = partner_score / partner.NI;

        // apply EWT 2 if applicable
        if(EWT.equals("2")){
            player_payoff *= partner_edge_weight;
            partner_payoff *= partner_edge_weight;
        }

        updateStatsPD(player, player_payoff);
        updateStatsPD(partner, partner_payoff);
    }



    public void updateStatsPD(Player player, double payoff){
//        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        double score = player.getScore();
        int NI = player.getNI();
        int NSI = player.getNSI();

        player.setScore(score + payoff);
        player.setNI(NI + 1);

        // insert segment here for NSI per neighbour counting if you get EWT 2 working with PD.
        // 25/9/24: ??? idk what above comment means.

        score = player.getScore();
        NI = player.getNI();
//        NSI = player.getNSI();
        switch(ASD){
            case "NI" -> player.setScore(score / NI);
            case "NSI" -> player.setScore(score / NSI);

            default -> {
                System.out.println("[ERROR] Invalid average score denominator configured. Exiting...");
                Runtime.getRuntime().exit(0);
            }
        }
    }



    public void initialiseEdgeWeights(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
//        player.setEdgeWeights(new double[neighbourhood.size()]);
//        double[] edge_weights = player.getEdgeWeights();
//        for(int i = 0; i < neighbourhood.size(); i++){
//            edge_weights[i] = 1.0;
//        }

        ArrayList <Double> edge_weights = new ArrayList<>();
        int size = neighbourhood.size();
        for(int i=0;i<size;i++){
            edge_weights.add(1.0);
        }
        player.setEdgeWeights(edge_weights);
    }


    /**
     * perform edge weight learning (EWL).
     * ensure weight resides within [0,1].
     * if option == 0, then increase weight.
     * if option == 1, then decrease weight.
     * if option == 2, then no effect.
     */
    public void EWL(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        for(int i = 0; i < neighbourhood.size(); i++){
            Player neighbour = neighbourhood.get(i);
            double total_leeway = calculateTotalLeeway(player, neighbour, i);
            int option = checkEWLC(player, neighbour, total_leeway);


//            double[] edge_weights = player.getEdgeWeights();
//            if(option == 0){ // increase EW
//                edge_weights[i] += calculateLearning(player, neighbour);
//                if(edge_weights[i] > 1.0){ // ensure edge weight resides within [0,1.0]
//                    edge_weights[i] = 1.0;
//                }
//            } else if (option == 1){ // decrease EW
//                edge_weights[i] -= calculateLearning(player, neighbour);
//                if(edge_weights[i] < 0){ // ensure edge weight resides within [0,1.0]
//                    edge_weights[i] = 0;
//                }
//            }


            ArrayList <Double> weights = player.getEdgeWeights();
            double weight = weights.get(i);
            if(option == 0){
                weight += calculateLearning(player, neighbour);
                if(weight > 1)
                    weight = 1;
            } else if(option == 1){
                weight -= calculateLearning(player, neighbour);
                if(weight < 0)
                    weight = 0;
            }

//            weights.set(i, weight);

//            weight = Math.floor(weight);
//            BigDecimal bd =
            String str = DF2.format(weight);
            double x = Double.parseDouble(str);
            if(x==0 && weight!=0)
                System.out.println("hello");
            weights.set(i, x);
        }
    }



    public double calculateTotalLeeway(Player player, Player neighbour, int i) {
//        double[] edge_weights = player.getEdgeWeights();
        ArrayList <Double> weights = player.getEdgeWeights();
        double weight = weights.get(i);
        double p = player.getP();
        double neighbour_p = neighbour.getP();
        double avg_score = player.getAvgScore();
        double neighbour_avg_score = player.getAvgScore();
        double local_leeway = player.getLocalLeeway();


        double global_leeway = leeway1;
//        double edge_weight_leeway = edge_weights[i] * leeway3;
        double edge_weight_leeway = weight * leeway3;
        double p_comparison_leeway = (neighbour_p - p) * leeway4;
        double p_leeway = neighbour_p * leeway5;
        double random_leeway;
        if(leeway6 == 0){
            random_leeway = 0;
        } else{
            random_leeway = ThreadLocalRandom.current().nextDouble(-leeway6, leeway6);
        }
        double avg_score_comparison_leeway = (avg_score - neighbour_avg_score) * leeway7;

        double total_leeway = global_leeway
                + local_leeway
                + edge_weight_leeway
                + p_comparison_leeway
                + p_leeway
                + random_leeway
                + avg_score_comparison_leeway;

        return total_leeway;
    }



    /**
     * Checks the edge weight learning condition to determine what kind of edge weight
     * learning should occur, if any.
     *
     * @param neighbour is the neighbour being pointed at by the edge
     * @param total_leeway is the leeway being given to the neighbour by the edge's owner
     * @return 0 for positive edge weight learning, 1 for negative, 2 for none
     */
    public int checkEWLC(Player player, Player neighbour, double total_leeway) {
        double p = player.getP();
        double neighbour_p = neighbour.getP();
        double avg_score = player.getAvgScore();
        double neighbour_avg_score = neighbour.getAvgScore();
        double q = player.getQ();
        double neighbour_q = neighbour.getQ();

        int option = 2;
        switch(EWLC){

            case"p"->{
                if (neighbour_p + total_leeway > p){
                    option = 0;
                } else if (neighbour_p + total_leeway < p){
                    option = 1;
                }
            }

            case"avgscore"->{
                if (neighbour_avg_score < avg_score + total_leeway){
                    option = 0;
                } else if (neighbour_avg_score > avg_score + total_leeway){
                    option = 1;
                }
            }

            case"AB"->{
                double AB_rating1 = ((alpha * p) + (beta * avg_score)) / (alpha + beta);
                double AB_rating2 = ((alpha * neighbour_p) + (beta * neighbour_avg_score)) / (alpha + beta);
                if(AB_rating1 < AB_rating2)
                    option = 0;
                else if(AB_rating1 > AB_rating2)
                    option = 1;
            }

            case"q"->{
                if(neighbour_q > q)
                    option = 0;
                else if(neighbour_q < q)
                    option = 1;
            }
        }

        return option;
    }



    /**
     * Calculate amount of edge weight learning to be applied to the weight of the edge.
     */
    public double calculateLearning(Player player, Player neighbour){
        double p = player.getP();
        double neighbour_p = neighbour.getP();
        double avg_score = player.getAvgScore();
        double neighbour_avg_score = neighbour.getAvgScore();
        double q = player.getQ();
        double neighbour_q = neighbour.getQ();

        double learning = 0;
        switch(EWLF){
            case"ROC"->         learning = ROC;
            case"pAD"->         learning = Math.abs(neighbour_p - p);
            case"pEAD"->        learning = Math.exp(Math.abs(neighbour_p - p));
            case"avgscoreAD"->  learning = Math.abs(neighbour_avg_score - avg_score);
            case"avgscoreEAD"-> learning = Math.exp(Math.abs(neighbour_avg_score - avg_score));
            case"pAD2"->        learning = Math.pow(Math.abs(neighbour_p - p), 2);
            case"pAD3"->        learning = Math.pow(Math.abs(neighbour_p - p), 3);
            case"AB"->          learning = Math.abs((((alpha * p) + (beta * avg_score)) / (alpha + beta)) - (((alpha * neighbour_p) + (beta * neighbour_avg_score)) / (alpha + beta)));
            case"qAD"->         learning = Math.abs(neighbour_q - q);
            case"qEAD"->        learning = Math.exp(Math.abs(neighbour_q - q));
            case"qAD2"->        learning = Math.pow(Math.abs(neighbour_q - q), 2);
            case"qAD3"->        learning = Math.pow(Math.abs(neighbour_q - q), 3);
        }

        return learning;
    }



    /**
     * Roulette Wheel (RW) Selection method compares fitness of child to neighbours. The
     * fitter the neighbour, the greater the likelihood of them being selected as child's
     * parent. If a parent is not selected, the child is selected as parent
     * by default.<br>
     */
    public Player RWSelection(Player child){
        ArrayList <Player> neighbourhood = child.getNeighbourhood();
        int size = neighbourhood.size();
        double fitness = child.getAvgScore(); // fitness equals avg score
        Player parent = child; // default parent is child
        double[] pockets = new double[size]; // pockets of roulette wheel
        double roulette_total = 0; // total fitness exp diff

        // allocate pockets
        for(int i = 0 ; i < size; i++){
            double neighbour_fitness = neighbourhood.get(i).getAvgScore();
            pockets[i] = Math.exp(neighbour_fitness - fitness);
            roulette_total += pockets[i];
        }
        roulette_total += 1.0; // allocate pocket to child

        // fitter player ==> more likely to be selected
        double tally = 0;
        double random_double = ThreadLocalRandom.current().nextDouble();
        for(int j = 0; j < size; j++){
            tally += pockets[j];
            double percentile = tally / roulette_total;
            if (random_double < percentile) {
                parent = neighbourhood.get(j);
                break;
            }
        }

        return parent;
    }



    /**
     * Child selects highest scoring neighbour.
     */
    public Player bestSelection(Player child){
        ArrayList <Player> neighbourhood = child.getNeighbourhood();
        double avg_score = child.getAvgScore();
        int size = neighbourhood.size();
        double parent_avg_score;

        // find highest scoring neighbour
        Player parent = neighbourhood.get(0);
        for(int i = 1; i < size; i++){
            Player neighbour = neighbourhood.get(i);
            double neighbour_avg_score = neighbour.getAvgScore();
            parent_avg_score = parent.getAvgScore();
            if(neighbour_avg_score > parent_avg_score){
                parent = neighbour;
            }
        }

        // if parent score less than child score, parent is child
        parent_avg_score = parent.getAvgScore();
        if(parent_avg_score <= avg_score){
            parent = child;
        }

        return parent;
    }



    /**
     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
     * The greater w (intensity of selection) is,
     * the more likely a fitter player is selected as child's parent.
     * @param child
     * @return parent player
     */
    public Player randSelection(Player child){
        double w = selnoise;
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



    public void setStrategy(Player player, double p, double q){
        player.setP(p);
        player.setQ(q);
    }



    /**
     * Evolution method where child wholly copies parent's strategy.
     * @param parent is the parent the player is copying.
     */
    public void copyEvolution(Player child, Player parent){
        double parent_old_p = parent.getOldP();
        double parent_old_q = parent.getOldQ();
        setStrategy(child, parent_old_p, parent_old_q);
    }



    /**
     * Use evo noise to move child strategy in direction of parent strategy.
     * @param child
     * @param parent
     */
    public void approachEvolution(Player child, Player parent){
        int ID = child.getID();
        int parent_ID = parent.getID();
        double p = child.getP();
        double q = child.getQ();
        double parent_old_p = parent.getOldP();
        double parent_old_q = parent.getOldQ();

        // do not approach evolve if parent is child
        if(parent_ID != ID){

            // for attribute, if parent is lower, reduce child; else, increase.
            double approach = ThreadLocalRandom.current().nextDouble(evonoise);
            if(parent_old_p < p){
                approach *= -1;
            }
            double new_p = p + approach;
            if(parent_old_q < q){
                approach *= -1;
            }
            double new_q = q + approach;

            setStrategy(child, new_p, new_q);
        }
    }



    /**
     * Mutation rate parameter determines the probability for mutation to occur.
     * @return boolean indicating whether mutation will occur
     */
    public boolean mutationCheck(){
        double random_double = ThreadLocalRandom.current().nextDouble();
        boolean mutation = random_double < mutrate;

        return mutation;
    }



    /**
     * Child's attributes are randomly and independently generated.
     */
    public void globalMutation(Player child){
        double new_p = ThreadLocalRandom.current().nextDouble();
        double new_q = ThreadLocalRandom.current().nextDouble();

        setStrategy(child, new_p, new_q);
    }



    /**
     * Slight mutations are independently applied to child's attributes.
     */
    public void localMutation(Player child){
        double p = child.getP();
        double q = child.getQ();

        double new_p = ThreadLocalRandom.current().nextDouble(p - mutbound, p + mutbound);
        double new_q = ThreadLocalRandom.current().nextDouble(q - mutbound, q + mutbound);

        setStrategy(child, new_p,new_q);
    }



    public void calculateAverageEdgeWeights(){
//        for(ArrayList<Player> row:grid){
//            for(Player player: row){
//                calculateMeanSelfEdgeWeight(player);
//                calculateMeanNeighbourEdgeWeight(player);
//            }
//        }

        for(int i=0;i<N;i++){
            Player player = pop[i];
            calculateMeanSelfEdgeWeight(player);
            calculateMeanNeighbourEdgeWeight(player);
        }
    }



    public void calculateMeanSelfEdgeWeight(Player player){
//        double[] edge_weights = player.getEdgeWeights();
//        int length = edge_weights.length;
//        double mean_self_edge_weight = 0;
//
//        // calculate mean of edge weights from player to its neighbours
//        for(int i = 0; i < length; i++){
//            mean_self_edge_weight += edge_weights[i];
//        }
//        mean_self_edge_weight /= length;
//
//        player.setMeanSelfEdgeWeight(mean_self_edge_weight);


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
//            double[] edge_weights = neighbour.getEdgeWeights();
            ArrayList <Double> weights = neighbour.getEdgeWeights();
            int index = findPlayerIndex(player, neighbour);
//            mean_neighbour_edge_weight += edge_weights[index];
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
            Player neighbours_neighbour = neighbourhood.get(i);
            int neighbours_neighbour_ID = neighbours_neighbour.getID();
            if (ID == neighbours_neighbour_ID) {
                index = i;
                break;
            }
        }

        return index;
    }



    /**
     * Loads in a configuration of settings from the config file, allowing the user to choose the values of the environmental parameters.
     */
    public static void setupEnvironment(){
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
        System.out.printf("%-6s |" +//config
                        " %-4s |" +//game
                        " %-6s |" +//runs
                        " %-9s |" +//iters
                        " %-6s |" +//length
                        " %-6s |" +//width
                        " %-10s |" +//space
                        " %-3s |" +//EWT
                        " %-9s |" +//EWLC
                        " %-11s |" +//EWLF
                        " %-3s |" +//ER
                        " %-6s |" +//ROC

                        " %-8s |" +//neigh
                        " %-8s |" +//sel
                        " %-8s |" +//selnoise
                        " %-3s |" +//ASD
                        " %-8s |" +//evo
                        " %-8s |" +//evonoise
                        " %-6s |" +//mut
                        " %-7s |" +//mutrate
                        " %-9s |" +//mutbound

                        " %-9s |" +//varying
                        " %-9s |" +//variation
                        " %-6s |" +//numexp

                        " %-8s |" +//datarate

                        " %-6s |" +//alpha
                        " %-6s |" +//beta
                        " %-7s |" +//leeway1
                        " %-7s |" +//leeway2
                        " %-7s |" +//leeway3
                        " %-7s |" +//leeway4
                        " %-7s |" +//leeway5
                        " %-7s |" +//leeway6
                        " %-7s |" +//leeway7

                        " %-9s |" +//injiter
                        " %-6s |" +//injp
                        " %-7s |" +//injsize
                        " desc%n" // ensure desc is the last column
                ,"config"
                ,"game"
                ,"runs"
                ,"iters"
                ,"length"
                ,"width"
                ,"space"
                ,"EWT"
                ,"EWLC"
                ,"EWLF"
                ,"ER"
                ,"ROC"

                ,"neigh"
                ,"sel"
                ,"selnoise"
                ,"ASD"
                ,"evo"
                ,"evonoise"
                ,"mut"
                ,"mutrate"
                ,"mutbound"
                ,"varying"
                ,"variation"
                ,"numexp"
                ,"datarate"

                ,"alpha"
                ,"beta"
                ,"leeway1"
                ,"leeway2"
                ,"leeway3"
                ,"leeway4"
                ,"leeway5"
                ,"leeway6"
                ,"leeway7"

                ,"injiter"
                ,"injp"
                ,"injsize"
        );
        printTableBorder();

        // display config table rows
        for(int i=0;i<configurations.size();i++){
            settings = configurations.get(i).split(",");
            CI = 0; // reset to 0 for each config
            System.out.printf("%-6d ", i); //config
            System.out.printf("| %-4s ", settings[CI++]); //game
            System.out.printf("| %-6s ", settings[CI++]); //runs
            System.out.printf("| %-9s ", settings[CI++]); //iters
            System.out.printf("| %-6s ", settings[CI++]); //length
            System.out.printf("| %-6s ", settings[CI++]); //width
            System.out.printf("| %-10s ", settings[CI++]); //space
            System.out.printf("| %-3s ", settings[CI++]); //EWT
            System.out.printf("| %-9s ", settings[CI++]); //EWLC
            System.out.printf("| %-11s ", settings[CI++]); //EWLF
            System.out.printf("| %-3s ", settings[CI++]); //ER
            System.out.printf("| %-6s ", settings[CI++]); //ROC

            System.out.printf("| %-8s ", settings[CI++]); //neigh
            System.out.printf("| %-8s ", settings[CI++]); //sel
            System.out.printf("| %-8s ", settings[CI++]); //selnoise
            System.out.printf("| %-3s ", settings[CI++]); //ASD
            System.out.printf("| %-8s ", settings[CI++]); //evo
            System.out.printf("| %-8s ", settings[CI++]); //evonoise
            System.out.printf("| %-6s ", settings[CI++]); //mut
            System.out.printf("| %-7s ", settings[CI++]); //mutrate
            System.out.printf("| %-9s ", settings[CI++]); //mutbound
            System.out.printf("| %-8s ", settings[CI++]); //datarate
            System.out.printf("| %-9s ", settings[CI++]); //varying
            System.out.printf("| %-9s ", settings[CI++]); //variation
            System.out.printf("| %-6s ", settings[CI++]); //numexp

            System.out.printf("| %-6s ", settings[CI++]); //alpha
            System.out.printf("| %-6s ", settings[CI++]); //beta
            System.out.printf("| %-7s ", settings[CI++]); //leeway1
            System.out.printf("| %-7s ", settings[CI++]); //leeway2
            System.out.printf("| %-7s ", settings[CI++]); //leeway3
            System.out.printf("| %-7s ", settings[CI++]); //leeway4
            System.out.printf("| %-7s ", settings[CI++]); //leeway5
            System.out.printf("| %-7s ", settings[CI++]); //leeway6
            System.out.printf("| %-7s ", settings[CI++]); //leeway7

            System.out.printf("| %-9s ", settings[CI++]); //injiter
            System.out.printf("| %-6s ", settings[CI++]); //injp
            System.out.printf("| %-7s ", settings[CI++]); //injsize
            System.out.printf("| %s ", settings[CI]); //desc
            System.out.println();
        }
        printTableBorder();

        // ask user which config they want to use
        System.out.println("Which config would you like to use? (int)");
        boolean config_found = false;
        int config_num;
        do{ // ensure user selects valid config
            config_num = scanner.nextInt();
            if(0 <= config_num && config_num < configurations.size()){
                config_found = true;
            } else{
                System.out.println("ERROR: invalid config number, try again");
            }
        }while(!config_found);


        // start applying the config
        settings = configurations.get(config_num).split(",");
        CI = 0;
        game = settings[CI++];
        Player.setGame(game);
//        runs = Integer.parseInt(settings[CI++]);
        runs=applySettingInt();
        if(runs < 1){
            System.out.println("[ERROR] Invalid number of runs configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
//        iters = Integer.parseInt(settings[CI++]);
        iters=applySettingInt();
        if(iters < 1){
            System.out.println("[ERROR] Invalid number of generations configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
//        length = Integer.parseInt(settings[CI++]);
        length=applySettingInt();
        if(length < 1){
            System.out.println("[ERROR] Invalid length. Exiting...");
            Runtime.getRuntime().exit(0);
        }
//        width=Integer.parseInt(settings[CI++]);
        width=applySettingInt();
        if(width < 1){
            System.out.println("[ERROR] Invalid width. Exiting...");
            Runtime.getRuntime().exit(0);
        }
//        columns = rows; // square lattice
//        N = rows * columns;
        N = length * width;
        space=settings[CI++];
        EWT = settings[CI++];
        EWLC = settings[CI++];
        if(!EWLC.equals("p") && !EWLC.equals("avgscore"))
            System.out.println("[NOTE] No edge weight learning condition configured.");
        EWLF = settings[CI++];
        if(!EWLF.equals("ROC")
                && !EWLF.equals("pAD")
                && !EWLF.equals("pEAD")
                && !EWLF.equals("avgscoreAD")
                && !EWLF.equals("avgscoreEAD")
                && !EWLF.equals("pAD2")
                && !EWLF.equals("pAD3")
                && !EWLF.equals("AB"))
            System.out.println("[NOTE] No edge weight learning formula configured.");
//        ER=Integer.parseInt(settings[CI++]);
        ER=applySettingInt();
        if(ER < 1){
            System.out.println("[ERROR] Invalid evolution rate configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
        ROC=applySettingDouble();

        neigh=settings[CI++];
        sel=settings[CI++];
        selnoise=applySettingDouble();
        ASD=settings[CI++];
        evo=settings[CI++];
        evonoise=applySettingDouble();
        mut=settings[CI++];
        mutrate=applySettingDouble();
        mutbound=applySettingDouble();

        varying=settings[CI++];
        experiment_series=(varying.equals(""))?false:true;
        variation=applySettingDouble();
        numexp=applySettingInt();

        datarate=applySettingInt();

        alpha=applySettingDouble();
        beta=applySettingDouble();

        leeway1=applySettingDouble();
        leeway2=applySettingDouble();
        leeway3=applySettingDouble();
        leeway4=applySettingDouble();
        leeway5=applySettingDouble();
        leeway6=applySettingDouble();
        leeway7=applySettingDouble();

        injiter=applySettingInt();
        if(injiter>iters)
            System.out.println("NOTE: injiter > iters, therefore no injection will occur.");
        injp=applySettingDouble();
        injsize=applySettingInt();
        desc=(settings[CI].equals(""))?"":settings[CI]; // final config param
    }



    /**
     * Initialises a lattice grid population of players with randomly generated strategies.
     */
    public void initRandomPop(){
        pop = new Player[N];
        int index = 0;
        if(space.equals("grid")){
            for(int y=0;y<length;y++){
                for(int x=0;x<width;x++){
                    Player new_player = null;
                    switch(game){
                        case"UG","DG"->{
                            double p = ThreadLocalRandom.current().nextDouble();
                            double q = 0;
                            switch(game){
                                case"UG"->q=ThreadLocalRandom.current().nextDouble();
                                case"DG"->q=0;
                            }
                            new_player=new Player(x,y,p,q,leeway2);
                        }
                    }
                    pop[index] = new_player;
                    index++;
                }
            }
        }else if(space.equals("hex")){
            // implement this...
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
                        "=======================================================" +
                        "%n");
    }



    // Create folders to store data.
    public static void createFolders(){
        if(game.equals("UG") || game.equals("DG")){
            p_data_filename = experiment_results_folder_path + "\\p_data";
            if(game.equals("UG")){
                q_data_filename = experiment_results_folder_path + "\\q_data";
            }
        }
        EW_data_filename = experiment_results_folder_path + "\\EW_data";
        NSI_data_filename = experiment_results_folder_path + "\\NSI_data";
        try{
            if(game.equals("UG") || game.equals("DG")){
                Files.createDirectories(Paths.get(p_data_filename));
                if(game.equals("UG")){
                    Files.createDirectories(Paths.get(q_data_filename));
                }
            }
            Files.createDirectories(Paths.get(EW_data_filename));
            Files.createDirectories(Paths.get(NSI_data_filename));
        }catch(IOException e){
            e.printStackTrace();
        }

    }



    // Collects initial settings into a string.
    public static void setInitialSettings(){
        if(experiment_series && experiment_num == 1){ // if at start of series
            initial_settings += "Experiment series ("+desc+")" +
                    " varying "+varying+
                    " by "+variation+
                    " between " + numexp+" experiments: ";
        } else if(!experiment_series){
            initial_settings += "Experiment: ";
        }
        initial_settings+=game;
        initial_settings+=", "+runs+" runs";
        initial_settings+=", "+iters+" iters";
        initial_settings+=", "+length+" length";
        initial_settings+=", "+width+" width";
        initial_settings+=", EWT="+EWT;
        initial_settings+=", EWLC="+EWLC;
        initial_settings+=", EWLF="+EWLF;
        initial_settings+=", ER="+ER;
        if(ROC != 0) initial_settings+=", ROC="+ROC;
        if(alpha != 0) initial_settings+=", alpha="+alpha;
        if(beta != 0) initial_settings+=", beta="+beta;
        if(leeway1 != 0) initial_settings+=", leeway1="+leeway1;
        if(leeway2 != 0) initial_settings+=", leeway2="+leeway2;
        if(leeway3 != 0) initial_settings+=", leeway3="+leeway3;
        if(leeway4 != 0) initial_settings+=", leeway4="+leeway4;
        if(leeway5 != 0) initial_settings+=", leeway5="+leeway5;
        if(leeway6 != 0) initial_settings+=", leeway6="+leeway6;
        if(leeway7 != 0) initial_settings+=", leeway7="+leeway7;
        initial_settings+=", "+neigh+" neigh";
        initial_settings+=", "+sel+" sel";
        if(sel.equals("Rand")) initial_settings+=", selnoise="+selnoise; // if sel func uses another param
        initial_settings+=", ASD="+ASD;
        initial_settings+=", "+evo+" evo";
        switch(evo){
            case"approach"->initial_settings+=", evonoise="+evonoise;
        }
        switch(mut){
            case"local","global"->{
                initial_settings+=", "+mut+" mut";
                initial_settings+=", mutrate="+DF4.format(mutrate);
                switch(mut){
                    case"local"->initial_settings+=", mutbound="+DF4.format(mutbound);
                }
            }
        }
        if(injiter > 0){
            initial_settings+=", injiter="+injiter;
            initial_settings+=", injp="+injp;
            initial_settings+=", injsize="+injsize;
        }
    }



    /**
     * Prints initial experimentation settings.
     */
    public static void printInitialSettings(){
        System.out.println(initial_settings);
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
//    public void assignAdjacentNeighbours(Player player, int y, int x, String type, int d){
    public void assignAdjacentNeighbours(Player player, String type, int d){
//        ArrayList<Player> neighbourhood = player.getNeighbourhood();
        ArrayList<Player> neighbourhood = new ArrayList<>();
//        double[] edge_weights = new double[]{};

        double y = player.getY();
        double x = player.getX();
        for(int i=1;i<=d;i++){
//            double y_plus = (((y + i) % length) + length) % length;
//            double y_minus = (((y - i) % length) + length) % length;
//            double x_plus = (((x + i) % width) + width) % width;
//            double x_minus = (((x - i) % width) + width) % width;

            double x_plus = adjustPosition(x, i, width);
            double x_minus = adjustPosition(x, -i, width);
            double y_plus = adjustPosition(y, i, length);
            double y_minus = adjustPosition(y, -i, length);

//            assignNeighbour(neighbourhood,edge_weights,i,y,x_plus);

            neighbourhood.add(findPlayerByPos(y,x_plus));
            neighbourhood.add(findPlayerByPos(y,x_minus));
            neighbourhood.add(findPlayerByPos(y_plus,x));
            neighbourhood.add(findPlayerByPos(y_minus,x));

            if(type.equals("dia")) {
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

            if(type.equals("Moore")){
                neighbourhood.add(findPlayerByPos(y_plus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_minus));
                neighbourhood.add(findPlayerByPos(y_plus,x_minus));
            }
        }

        // DO NOT REMOVE UNTIL FULL FUNCTIONALITY REPLICATED ABOVE!
//        for(int i = 1; i <= d; i++){
////            int x_plus = (((x + i) % rows) + rows) % rows;
////            int x_minus = (((x - i) % rows) + rows) % rows;
////            int y_plus = (((y + i) % columns) + columns) % columns;
////            int y_minus = (((y - i) % columns) + columns) % columns;
//            int x_plus = adjustPosition(x, i, rows);
//            int x_minus = adjustPosition(x, -i, rows);
//            int y_plus = adjustPosition(y, i, columns);
//            int y_minus = adjustPosition(y, -i, columns);
//
//
//            // add VN neighbours
//            neighbourhood.add(grid.get(y).get(x_plus));         // (x + i,  y)
//            neighbourhood.add(grid.get(y).get((x_minus)));      // (x - i,  y)
//            neighbourhood.add(grid.get(y_plus).get(x));         // (x,      y + i)
//            neighbourhood.add(grid.get(y_minus).get(x));        // (x,      y - i)
//
//            if(type.equals("dia")){
//                if(i > 1){
////                    int x_plus_minus = (((x_plus - 1) % rows) + rows) % rows;
////                    int x_minus_plus = (((x_minus + 1) % rows) + rows) % rows;
////                    int y_plus_minus = (((y_plus - 1) % rows) + rows) % rows;
////                    int y_minus_plus = (((y_minus + 1) % rows) + rows) % rows;
//                    int x_plus_minus = adjustPosition(x_plus, -1, rows);
//                    int x_minus_plus = adjustPosition(x_minus, 1, rows);
//                    int y_plus_minus = adjustPosition(y_plus, -1, rows);
//                    int y_minus_plus = adjustPosition(y_minus, 1, rows);
//
//
//                    neighbourhood.add(grid.get(y_plus_minus).get(x_plus_minus));
//                    neighbourhood.add(grid.get(y_minus_plus).get(x_plus_minus));
//                    neighbourhood.add(grid.get(y_minus_plus).get(x_minus_plus));
//                    neighbourhood.add(grid.get(y_plus_minus).get(x_minus_plus));
//                }
//            }
//
//            // add M neighbours
//            if(type.equals("M")){
//                neighbourhood.add(grid.get(y_plus).get(x_plus));    // (x + d,  y + i)
//                neighbourhood.add(grid.get(y_minus).get(x_plus));   // (x + d,  y - i)
//                neighbourhood.add(grid.get(y_minus).get(x_minus));  // (x - d,  y - i)
//                neighbourhood.add(grid.get(y_plus).get(x_minus));   // (x - d,  y + i)
//            }
//        }

        player.setNeighbourhood(neighbourhood);
    }



//    public void assignNeighbour(ArrayList <Player> neighbourhood, double[] edge_weights, int i, double y, double x){
//        neighbourhood.add(findPlayerByPos(y,x));
//        edge_weights[i]=1.0;
//    }



    // give player the IDs of its neighbours.
    public void assignNeighbourIDs(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int size = neighbourhood.size();
        int[] arr = new int[size];
        for(int i=0;i<size;i++){
            arr[i] = neighbourhood.get(i).getID();
        }
        player.setNeighbourIDs(arr);
    }



    /**
     * Randomly assigns either uni-directional or bi-directional edges to player.<br>
     * Assumes 2D square lattice grid population structure.
     * @param player
     * @param type
     * @param size
     */
    public void assignRandomNeighbours(Player player, String type, int size){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();

        // randomly generate IDs
        Set<Integer> IDs = new HashSet<>();
        while(IDs.size() < size){
            int ID = ThreadLocalRandom.current().nextInt(N);
            IDs.add(ID);
        }

        // assign edges to neighbours
//        for(int ID: IDs){
//            int column = ID / columns;
//            int row = ID % rows;
//            neighbourhood.add(grid.get(column).get(row));
//            if(type.equals("bi")){
//                grid.get(column).get(row).getNeighbourhood().add(player);
//            }
//        }


        for(int ID: IDs){
            neighbourhood.add(findPlayerByID(ID));
        }
    }



    // assign all other players to neighbourhood
    public void assignAllNeighbours(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int ID = player.getID();

//        for(int i = 0; i < grid.size(); i++){
//            for(int j = 0; j < grid.get(0).size(); j++){
//                Player neighbour = grid.get(i).get(j);
//                int neighbour_id = neighbour.getID();
//                if(id != neighbour_id){
//                    neighbourhood.add(neighbour);
//                }
//            }
//        }

        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID != ID2){
                neighbourhood.add(player2);
            }
        }
    }


    // initialise NSI per neighbour counters.
    public void resetNSIPerNeighbour(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int size = neighbourhood.size();
        player.setNSIPerNeighbour(new int[size]);
        for(int i = 0; i < size; i++){
            player.getNSIPerNeighbour()[i] = 0;
        }
    }



    /**
     * Calculate the average value of p and the standard deviation of p across the population
     * at the current generation.
     */
    public void calculateOverallStats(){
        // calculate average p
        avg_p = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                avg_p+=player.getP();
//            }
//        }
        for(int i=0;i<N;i++){
            Player player = pop[i];
            double p = player.getP();
            avg_p += p;
        }
        avg_p /= N;

        // calculate p SD
        p_SD = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                p_SD += Math.pow(player.getP() - avg_p, 2);
//            }
//        }
        for(int i=0;i<N;i++){
            Player player = pop[i];
            double p = player.getP();
            p_SD += Math.pow(p - avg_p, 2);
        }
        p_SD = Math.pow(p_SD / N, 0.5);

        // calculate average q
        avg_q = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                avg_q+=player.getQ();
//            }
//        }
        for(int i=0;i<N;i++){
            Player player = pop[i];
            double q = player.getQ();
            avg_q += q;
        }
        avg_q /= N;

        // calculate p SD
        q_SD = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                q_SD += Math.pow(player.getQ() - avg_q, 2);
//            }
//        }
        for(int i=0;i<N;i++){
            Player player = pop[i];
            double q = player.getQ();
            q_SD += Math.pow(q - avg_q, 2);
        }
        q_SD = Math.pow(q_SD / N, 0.5);
    }



    /**
     * Prepare program for the next generation.<br>
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void prepare(){
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){

        for(int i=0;i<N;i++){
            Player player = pop[i];

            player.setScore(0);
            player.setNI(0);
            player.setNSI(0);
            player.setNSP(0);
            player.setNSR(0);
            switch(game){
                case"UG","DG"->{
                    player.setOldP(player.getP());
                    switch(game){
                        case"UG"->player.setOldQ(player.getQ());
                        case"DG"->player.setQ(0);
                    }
                }
            }

//                player.setNSIPerNeighbour(new int[]{0,0,0,0});

//                for(int i = 0; i < N - 1; i++){
//                    player.getNSIPerNeighbour()[i] = 0;
//                }

            resetNSIPerNeighbour(player);
        }
    }





    public static void writeSeriesData(){
        // write experiment data, including results and settings, to a .csv data file.
        try{
//            series_data_filename = experiment_results_folder_path + "\\" + timestamp_string + "_series_data.csv"; // use this instead if you want to be able to open multiple series data files at once.
            series_data_filename = experiment_results_folder_path + "\\" + "series_data.csv";
            String s="";

            // write column headings
            if(experiment_num == 1){
                fw = new FileWriter(series_data_filename, false);
                s+="exp num";
                s+=",game";
                if(game.equals("UG") || game.equals("DG")){
                    s+=",mean avg p";
                    s+=",avg p SD";
                    if(game.equals("UG")){
                        s+=",mean avg q";
                        s+=",avg q SD";
                    }
                }
                s+=",runs";
                s+=",iters";
                s+=",neigh";
                s+=",length";
                s+=",width";
                s+=",N";
                s+=",EWT";
                s+=(EWLC.isEmpty())?"":",EWLC";
                s+=(EWLF.isEmpty())?"":",EWLF";
                s+=",ER";
                s+=(ROC==0)?"":",ROC";
                s+=(leeway1==0)?"":",leeway1";
                s+=(leeway2==0)?"":",leeway2";
                s+=(leeway3==0)?"":",leeway3";
                s+=(leeway4==0)?"":",leeway4";
                s+=(leeway5==0)?"":",leeway5";
                s+=(leeway6==0)?"":",leeway6";
                s+=(leeway7==0)?"":",leeway7";
                s+=",sel";
                s+=(selnoise==0)?"":",selnoise";
                s+=",evo";
                s+=(evonoise==0)?"":",evonoise";
                s+=(mut.isEmpty())?"":",mut";
                s+=(mutrate==0)?"":",mutrate";
                s+=(mutbound==0)?"":",mutbound";
                s+=(injiter==0)?"":",injiter";
                s+=(injp==0)?"":",injp";
                s+=(injsize==0)?"":",injsize";


            } else {
                fw = new FileWriter(series_data_filename, true);
            }

            // write row data
            s+="\n" + experiment_num;
            s+="," + game;
            if(game.equals("UG") || game.equals("DG")){
                s+="," + DF4.format(experiment_mean_avg_p);
                s+="," + DF4.format(experiment_SD_avg_p);
                if(game.equals("UG")){
                    s+="," + DF4.format(experiment_mean_avg_q);
                    s+="," + DF4.format(experiment_SD_avg_q);
                }
            }
            s+="," + runs;
            s+="," + iters;
            s+="," + neigh;
            s+="," + length;
            s+="," + width;
            s+="," + N;
            s+=","+EWT;
            s+=(EWLC.isEmpty())?"":","+EWLC;
            s+=(EWLF.isEmpty())?"":","+EWLF;
            s+=","+ER;
            s+=(ROC==0)?"":","+ROC;
            s+=(leeway1==0)?"":","+leeway1;
            s+=(leeway2==0)?"":","+leeway2;
            s+=(leeway3==0)?"":","+leeway3;
            s+=(leeway4==0)?"":","+leeway4;
            s+=(leeway5==0)?"":","+leeway5;
            s+=(leeway6==0)?"":","+leeway6;
            s+=(leeway7==0)?"":","+leeway7;
            s+=","+sel;
            s+=(selnoise==0)?"":","+selnoise;
            s+=","+evo;
            s+=(evonoise==0)?"":","+evonoise;
            s+=(mut.isEmpty())?"":","+mut;
            s+=(mutrate==0)?"":","+mutrate;
            s+=(mutbound==0)?"":","+mutbound;
            s+=(injiter==0)?"":","+injiter;
            s+=(injp==0)?"":","+injp;
            s+=(injsize==0)?"":","+injsize;
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
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
    public void writeAvgpData(){
        try{
//            String filename = experiment_results_folder_path + "\\" + timestamp_string + "_experiment_data.csv"; // use this instead if you want to be able to open multiple series data files at once.
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



    public void writeAvgqData(){
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



    /**
     * Writes proposal values of pop to .csv file.
     */
    public void writepData(){
        try{
            String filename = p_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
            String s = "";
//            for(int y = rows - 1; y >= 0; y--){
//                for(int x = 0; x < columns; x++){
//                    s += DF4.format(grid.get(y).get(x).getP());
//                    if(x + 1 < columns){
//                        s += ",";
//                    }
//                }
//                s += "\n";
//            }
//            for(int y=0;y<length;y++){
//            for(int y=0;y<length;y++){
            for(int y=length-1;y>=0;y--){
//                for(int x=0;x<width;x++){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    double p = player.getP();
                    s += DF4.format(p) + ",";
                }
                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    public void writeqData(){
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
     * Uses EW data to write a grid of 4x4 sub-grids into a .csv file.<br>
     * Assumes von Neumann neighbourhood type and rows=columns.
     */
    public void writeEWData(){
        try{
            String filename = EW_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename);
            String string = "";
//            String[] substrings = new String[(rows * 4)];
//            String[] substrings = new String[(width * 4)];
            String[] substrings = new String[(length * 4)];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
//            for(int y = rows - 1; y >= 0; y--) {
//                for (int x = 0; x < columns; x++) {
//                    Player current = grid.get(y).get(x);

//            for(int x=width-1;x>=0;x--){
//                for(int y=0;y<length;x++){
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player current = findPlayerByPos(y,x);


//                    double[] edge_weights = current.getEdgeWeights();
                    ArrayList <Double> weights = current.getEdgeWeights();
                    ArrayList<Player> neighbourhood = current.getNeighbourhood();


                    // EW health of player is equal to sum of edge weights divided by num edge weights.
                    // this is equivalent to mean self edge weight plus mean neighbour edge weight divided by 2.
                    double EW_health = (current.getMeanSelfEdgeWeight() + current.getMeanNeighbourEdgeWeight()) / 2;


                    substrings[a] += "0,"
//                            +edge_weights[2]+","
                            +weights.get(2)+","
//                            +neighbourhood.get(2).getEdgeWeights()[3]+","
                            +neighbourhood.get(2).getEdgeWeights().get(3)+","
                            +"0";
//                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights()[0]+","
                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights().get(0)+","


//                            +DF2.format(current.getP())+","
//                            +DF2.format(current.getAverageScore())+","


                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","


//                            +edge_weights[0];
                            +weights.get(0);
//                    substrings[a+2] += current.getEdgeWeights()[1]+","
                    substrings[a+2] += current.getEdgeWeights().get(1)+","


//                            +DF2.format(current.getMeanSelfEdgeWeight())+","
//                            +DF2.format(current.getMeanNeighbourEdgeWeight())+","


                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","


//                            +neighbourhood.get(0).getEdgeWeights()[1];
                            +neighbourhood.get(0).getEdgeWeights().get(1);
                    substrings[a+3] += "0,"
//                            +neighbourhood.get(3).getEdgeWeights()[2]+","
                            +neighbourhood.get(3).getEdgeWeights().get(2)+","
//                            +edge_weights[3]+","
                            +weights.get(3)+","
                            +"0";
//                    if(x + 1 < columns){
//                    if(x + 1 < length){
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
     * Assumes von Neumann neighbourhood type and rows=columns.
     */
    public void writeNSIData(){
        try{
            String filename = NSI_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename);
            String string = "";
//            String[] substrings = new String[(rows * 4)];
//            String[] substrings = new String[(width * 4)];
            String[] substrings = new String[(length * 4)];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
//            for(int y = rows - 1; y >= 0; y--) {
//                for (int x = 0; x < columns; x++) {
//                    Player current = grid.get(y).get(x);


            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player current = findPlayerByPos(y,x);


                    int[] NSI_per_neighbour = current.getNSIPerNeighbour();


//                    TESTING TO SEE IF ITS EVER 0
//                    for(int i: NSI_per_neighbour){
//                        if(i == 0.0){
//                            System.out.println("hello"); // can place breakpoint here
//                        }
//                    }




                    substrings[a] += "0,"
                            +NSI_per_neighbour[2]+","
                            +NSI_per_neighbour[2]+","
                            +"0";
                    substrings[a+1] += NSI_per_neighbour[1]+","
                            +"0,"
                            +"0,"
                            +NSI_per_neighbour[0];
                    substrings[a+2] += NSI_per_neighbour[1]+","
                            +"0,"
                            +"0,"
                            +NSI_per_neighbour[0];
                    substrings[a+3] += "0,"
                            +NSI_per_neighbour[3]+","
                            +NSI_per_neighbour[3]+","
                            +"0";
//                    if(x + 1 < columns){
//                    if(x + 1 < length){
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
     * Finds a player given the integer ID parameter.<br>
     * Currently not used.
     * @param ID of the player to find
     * @return player object with the given ID
     */
    public Player findPlayerByID(int ID){
        Player player = null;

//        for(int i=0;i<rows;i++){
//            for(int j=0;j<columns;j++){
//                if(ID == grid.get(i).get(j).getID()){
//                    player = grid.get(i).get(j);
//                    break;
//                }
//            }
//        }

        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player.getID();
            if(ID == ID2){
                player = player2;
                break;
            }
        }

        return player;
    }



    // find player by position
    public Player findPlayerByPos(double y, double x){
        Player player = null;
        boolean found = false;
        int i=0;

        do{
            if(i==15){
                System.out.println("insert BP here");
            }
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
     * Injects a cluster of players in the population with a given strategy.
     * Doesnt work with new pop structure...
     */
//    public void injectStrategyCluster(double new_strategy, int cluster_size){
////        for(int i=0;i<cluster_size;i++){
////            for(int j=0;j<cluster_size;j++){
////                grid.get(i).get(j).setP(new_strategy);
////            }
////        }
////        for(int i=0;i<cluster_size;i++){
////
////        }
//    }



    /**
     * applySettingDouble() and applySettingInt() apply the value obtained from the config file to the
     * parameter in question.
     */
    public static double applySettingDouble(){
        double x;
        if(settings[CI].equals("")){
            x=0;
            CI++;
        }else{
            x=Double.parseDouble(settings[CI++]);
        }
        return x;
    }



    public static int applySettingInt(){
        int x;
        if(settings[CI].equals("")){
            x=0;
            CI++;
        }else{
            x=Integer.parseInt(settings[CI++]);
        }
        return x;
    }



    /**
     * Assigns Margolus neighbourhood to players.<br>
     * Neighbourhood 1: (x-1,y), (x,y+1), (x-1,y+1).<br>
     * Neighbourhood 2: (x+1,y), (x,y-1), (x-1,y-1).
     */
//    public void assignMargolusNeighbourhood(Player player, int y, int x){
//        // setup #1
////        ArrayList <Player> neighbourhood = new ArrayList<>();
////
////        int x_plus = adjustPosition(x, 1, rows);
////        int x_minus = adjustPosition(x, -1, rows);
////        int y_plus = adjustPosition(y, 1, columns);
////        int y_minus = adjustPosition(y, -1, columns);
////
////        if(gen % 2 == 0){
////            neighbourhood.add(grid.get(y).get(x_minus));        // (x - 1,  y)
////            neighbourhood.add(grid.get(y_plus).get(x));         // (x,      y + 1)
////            neighbourhood.add(grid.get(y_plus).get(x_minus));   // (x - 1,  y + 1)
////        }else{
////            neighbourhood.add(grid.get(y).get(x_plus));         // (x + 1,  y)
////            neighbourhood.add(grid.get(y_minus).get(x));        // (x,      y - 1)
////            neighbourhood.add(grid.get(y_minus).get(x_plus));   // (x - 1,  y - 1)
////        }
////
////        player.setNeighbourhood(neighbourhood);
//
//
//        // setup #2 (note that rest of program does not know when to use margolus neighbourhoods instead of regular neighbourhood)
//        ArrayList<Player> neighbourhood = player.getNeighbourhood();
//        ArrayList<Player> mar_neigh1 = player.getMargolus_neighbourhood1();
//        ArrayList<Player> mar_neigh2 = player.getMargolus_neighbourhood2();
//
//        int x_plus = adjustPosition(x, 1, rows);
//        int x_minus = adjustPosition(x, -1, rows);
//        int y_plus = adjustPosition(y, 1, columns);
//        int y_minus = adjustPosition(y, -1, columns);
//
//        Player a;
//        a=grid.get(y).get(x_minus);
//        neighbourhood.add(a);
//        mar_neigh1.add(a);
//        a=grid.get(y_plus).get(x);
//        neighbourhood.add(a);
//        mar_neigh1.add(a);
//        a=grid.get(y_plus).get(x_minus);
//        neighbourhood.add(a);
//        mar_neigh1.add(a);
//        a=grid.get(y).get(x_plus);
//        neighbourhood.add(a);
//        mar_neigh2.add(a);
//        a=grid.get(y_minus).get(x);
//        neighbourhood.add(a);
//        mar_neigh2.add(a);
//        a=grid.get(y_minus).get(x_plus);
//        neighbourhood.add(a);
//        mar_neigh2.add(a);
//    }



//    public int adjustPosition(int position, int adjustment, int max){
//        int new_position = (((position + adjustment) % max) + max) % max;
//        return new_position;
//    }
    // adjust position with respect to periodic boundaries
    public double adjustPosition(double position, double adjustment, int max){
        double new_position = (((position + adjustment) % max) + max) % max;
        return new_position;
    }



    public void rewire(Player player){
//        ArrayList <Player> neighbourhood = player.getNeighbourhood();
//        ArrayList <Double> weights = player.getEdgeWeights();

        ArrayList <Player> neighbourhood = new ArrayList <Player> (player.getNeighbourhood());
        ArrayList <Double> weights = new ArrayList <Double> (player.getEdgeWeights());

        int size = weights.size();

//        int x=0;

        // doesnt work when we start at 0 because index exception.
        // what if we just got backwards thru the arraylist?
//        for(int i=0;i<size;i++){
        for(int i=size-1;i>=0;i--){

            // player must have >= 1 edges.
            // what about the case where both weights = 0: which do you rewire?
            // for now, whichever edge is encountered first by the loop gets rewired first.
            int size2 = weights.size();
            if(size > 1){
                if(weights.get(i) == 0.0){
                    // remove neighbour from neighbourhood.
//                neighbourhood.remove(i-x);
                    neighbourhood.remove(i);
                    // remove edge for both players! same with weights.


                    // remove edge weight from array.
//                weights.remove(i-x);
                    weights.remove(i);

                    // find new neighbour.





//                x++;
                }
            }
        }

        // enact changes to neighbourhood and weights.
        player.setNeighbourhood(neighbourhood);
        player.setEdgeWeights(weights);
    }
}

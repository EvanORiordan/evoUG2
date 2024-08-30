import java.io.*;
import java.lang.reflect.Array;
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
 *
 *
 * DG Algorithm.<br>
 */
public class Alg1 extends Thread{

    // fields related to the game environment
    static String game; // what game is being played
    static int rows; // how many rows in the square grid
    static int columns; // how many rows in the square grid
    static int N; // population size
    static int iters; // how many iterations occur per experiment run
    static int runs; // how many times this experiment will be run.
    static String neigh; // indicates the type of neighbourhood being enforced
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // 2D square lattice contains the population
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
    static double gross_prize_UG = 1.0; // default prize per UG interaction. as long as value != 0, value doesnt matter if fitness metric is avg score rather than score.
    static String ASD; // average score denominator: determines how average score is calculated


    // fields related to experiment series
    static boolean experiment_series; //indicates whether to run experiment or experiment series where a parameter is varied
    static String varying; //indicates which parameter will be varied in an experiment series
    static double variation; //indicates by how much parameter will vary between subsequent experiments. the double data is used because it works for varying integer parameters as well as doubles.
    static int numexp; //indicates the number of experiments to occur in the series
    static int experiment_num = 1; //tracks which experiment is taking place at any given time during a series
    static int run_num; // tracks which of the runs is currently executing
    static ArrayList<Double> various_amounts;


    // fields related to individual experiments
    static double experiment_mean_avg_p;
    static double experiment_SD_avg_p;


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
            case"rows"->various_amounts.add((double)rows);
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
                case "rows"->{
                    rows+=(int)variation;
                    columns+=(int)variation;
                    N += (int) (variation * variation);
                    various_amounts.add((double)rows);
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

        // display a summary of the series in the console
        String summary = "";
        try{
            br = new BufferedReader(new FileReader(series_data_filename));
            String line;
            br.readLine(); // purposely ignore the row of headings
            int i=0; // indicates which index of the row we are currently on
            while((line=br.readLine()) != null){
                String[] row_contents = line.split(",");
                summary += "experiment="+row_contents[0];
                summary += "\tmean avg p="+DF4.format(Double.parseDouble(row_contents[1]));
                summary += "\tavg p SD="+DF4.format(Double.parseDouble(row_contents[2]));
                summary += "\t";
                summary += varying+"=";
                summary += DF4.format(various_amounts.get(i));
                i++;
                summary += "\n";
            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("\nInitial settings: ");
        printInitialSettings(); // print initial experimentation settings for comparison
        System.out.println();
        System.out.println(summary);
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

        // perform/run the experiment multiple times if applicable
        for(run_num = 1; run_num <= runs; run_num++){
            Alg1 run = new Alg1(); // represents one run of the experiment
            run.start(); // start the run
            experiment_mean_avg_p += run.avg_p; // tally the mean avg p of the experiment
            experiment_avg_p_values[run_num] = run.avg_p;

            // display the avg p of the pop at the end of the run
            System.out.println("avg p of run "+run_num
                    +" of experiment "+experiment_num
                    +": "+DF4.format(run.avg_p));
        }

        if(!experiment_series){
            System.out.println();
            printInitialSettings();
        }

        // calculate stats of experiment
        experiment_mean_avg_p /= runs;
        for(int i=0;i<runs;i++){
            experiment_SD_avg_p +=
                    Math.pow(experiment_avg_p_values[i] - experiment_mean_avg_p, 2);
        }
        experiment_SD_avg_p = Math.pow(experiment_SD_avg_p / runs, 0.5);

        // display stats to user in console
        System.out.println("mean avg p="+DF4.format(experiment_mean_avg_p)
                +" avg p SD="+DF4.format(experiment_SD_avg_p));

        writeSeriesData();

        experiment_num++; // move on to the next experiment in the series
    }


    /**
     * Method for running the core algorithm at the heart of the program.
     */
    @Override
    public void start(){
        // no need to remove these comments since they act as a reminder as to things we might work on in later research.
//        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
//            initSavedDGPop();
//        } else { // user wants to randomly generate a population
//            initRandomPop();
//        }


        initRandomPop();


        // initialise neighbourhoods
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                Player player = grid.get(i).get(j);
                switch(neigh){
                    case"VN","M"->assignAdjacentNeighbours(player, i, j);
                    case"random"->assignRandomNeighbours(player, 3);
                    default -> {
                        System.out.println("[ERROR] Invalid neighbourhood type configured. Exiting...");
                        Runtime.getRuntime().exit(0);
                    }
                }
            }
        }

        // initialise edge weights
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                initialiseEdgeWeights(grid.get(i).get(j));
            }
        }

        // players begin playing the game
        while(iter <= iters) { // algorithm stops once this condition is reached

            // injection phase
            if(iter == injiter){
                injectStrategyCluster(injp, injsize);
            }

            // playing phase of generation
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    play(player);
                }
            }

            // edge weight learning phase of generation
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    EWL(player);
                }
            }

            // evolution phase of generation.
            // occurs every ER iterations.
            // consists of selection, evolution and mutation, if applicable.
            if(iter % ER == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player child : row) {

                        // select parent
                        Player parent = null;
                        switch(sel){
                            case "RW" -> parent = RWSelection(child);
                            case "best" -> parent = bestSelection(child);
                            case "Rand" -> parent = RandSelection(child);
                            default -> {
                                System.out.println("[ERROR] Invalid selection function configured. Exiting...");
                                Runtime.getRuntime().exit(0);
                            }
                        }

                        // evolve child
                        switch (evo) {
                            case "copy" -> copyEvolution(child, parent);
                            case "approach" -> approachEvolution(child, parent);
                            default -> {
                                System.out.println("[ERROR] Invalid evolution function configured. Exiting...");
                                Runtime.getRuntime().exit(0);
                            }
                        }

                        // mutate child
                        switch (mut){
                            case "global" -> {
                                if(mutationCheck()){
                                    globalMutation(child);
                                }
                            }
                            case "local" -> {
                                if(mutationCheck()){
                                    localMutation(child);
                                }
                            }
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
                        && iter % (ER * datarate) == 0 // record gen's data every x gens, where x=datarate
                ){
                    System.out.println("iter "+iter+", gen "+gen+": avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD));
                    writeAvgpData();
                    writepData();
                    writeEWData();
                    writeNSIData();
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

            // partner finds player
            ArrayList <Player> neighbour_neighbourhood = neighbour.getNeighbourhood();
            for (int j = 0; j < neighbour_neighbourhood.size(); j++) {
                Player neighbours_neighbour = neighbour_neighbourhood.get(j);
                int neighbours_neighbour_ID = neighbours_neighbour.getID();
                if (neighbours_neighbour_ID == ID) {
                    double[] neighbour_edge_weights = neighbour.getEdgeWeights();
                    double edge_weight = neighbour_edge_weights[j];

                    // play game
                    switch(EWT){
                        case"1"->{
                            double random_double = ThreadLocalRandom.current().nextDouble();
                            if(edge_weight > random_double){
                                switch (game){
                                    case"UG","DG"->{
                                        UG(gross_prize_UG, player, neighbour);
                                    }
                                    case"PD"->{
                                        PD(player, neighbour);
                                    }
                                }
                            } else{
                                int neighbour_ID = neighbour.getID();
                                updateStatsUG(player, 0, neighbour_ID, true);
                                updateStatsUG(neighbour, 0, ID, false);
                            }
                        }
                        case"2"->{
                            switch(game){
                                case"UG","DG"->{
                                    UG(neighbour_edge_weights[j] * gross_prize_UG, player, neighbour);
                                }
                                case"PD"->{
                                    PD(player, neighbour);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }





    public void UG(double net_prize, Player proposer, Player responder){
        double proposer_p = proposer.getP();
        double responder_q = responder.getQ();
        int proposer_ID = proposer.getID();
        int responder_ID = responder.getID();

        if(proposer_p >= responder_q){ // accept
            double proposer_payoff = net_prize - (net_prize * proposer_p);
            double responder_payoff = net_prize * proposer_p;
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





    public void PD(Player player, Player partner){
        String strategy_PD = player.getStrategyPD();
        String partner_strategy_PD = partner.getStrategyPD();
        double score = player.getScore();
        double partner_score = partner.getScore();

        if(strategy_PD.equals("C") && partner_strategy_PD.equals("C")){
//            score += R;
//            partner_score += R;
            updateStatsPD(player, R);
            updateStatsPD(partner, R);
        } else if(strategy_PD.equals("C") && partner_strategy_PD.equals("D")){
//            score += S;
//            partner_score += T;
            updateStatsPD(player, S);
            updateStatsPD(partner, T);
        }else if(strategy_PD.equals("D") && partner_strategy_PD.equals("C")){
//            score += T;
//            partner_score += S;
            updateStatsPD(player, T);
            updateStatsPD(partner, S);
        }else if(strategy_PD.equals("D") && partner_strategy_PD.equals("D")){
//            score += P;
//            partner_score += P;
            updateStatsPD(player, P);
            updateStatsPD(partner, P);
        }else if(strategy_PD.equals("A") || partner_strategy_PD.equals("A")){
//            score += l;
//            partner_score += l;
            updateStatsPD(player, l);
            updateStatsPD(partner, l);
        }
//        NI++;
//        partner.NI++;
//        avg_score = score / NI;
//        partner.avg_score = partner_score / partner.NI;
    }



    public void updateStatsPD(Player player, double payoff){
//        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        double score = player.getScore();
        int NI = player.getNI();
        int NSI = player.getNSI();

        player.setScore(score + payoff);
        player.setNI(NI + 1);

        // insert segment here for NSI per neighbour counting if you get EWT 2 working with PD.

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



    // generic update stats method for all games?
//    public void updateStats(Player player, double payoff){
//
//    }







    public void initialiseEdgeWeights(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        player.setEdgeWeights(new double[neighbourhood.size()]);
        double[] edge_weights = player.getEdgeWeights();
        for(int i = 0; i < neighbourhood.size(); i++){
            edge_weights[i] = 1.0;
        }
    }



    public void EWL(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        for(int i = 0; i < neighbourhood.size(); i++){
            Player neighbour = neighbourhood.get(i);
            double total_leeway = calculateTotalLeeway(player, neighbour, i);
            int option = checkEWLC(player, neighbour, total_leeway);
            double[] edge_weights = player.getEdgeWeights();
            if(option == 0){ // positive edge weight learning
                edge_weights[i] += calculateLearning(player, neighbour);
                if(edge_weights[i] > 1.0){ // ensure edge weight resides within [0,1.0]
                    edge_weights[i] = 1.0;
                }
            } else if (option == 1){ // negative edge weight learning
                edge_weights[i] -= calculateLearning(player, neighbour);
                if(edge_weights[i] < 0){ // ensure edge weight resides within [0,1.0]
                    edge_weights[i] = 0;
                }
            }
            // if option equals 2, no edge weight learning occurs
        }
    }


    /**
     * @param neighbour
     * @param i index pointing to edge weight of player corresponding to neighbour
     * @return total leeway to be given by the player to the neighbour during edge weight learning
     */
    public double calculateTotalLeeway(Player player, Player neighbour, int i) {
        double[] edge_weights = player.getEdgeWeights();
        double p = player.getP();
        double neighbour_p = neighbour.getP();
        double avg_score = player.getAvgScore();
        double neighbour_avg_score = player.getAvgScore();
        double local_leeway = player.getLocalLeeway();


        double global_leeway = leeway1;
        double edge_weight_leeway = edge_weights[i] * leeway3;
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


        int option = 2;
        switch(EWLC){

            case"p"->{ // compare proposal values
                if (neighbour_p + total_leeway > p){
                    option = 0;
                } else if (neighbour_p + total_leeway < p){
                    option = 1;
                }
            }

            case"avgscore"->{ // compare average scores
                if (neighbour_avg_score < avg_score + total_leeway){
                    option = 0;
                } else if (neighbour_avg_score > avg_score + total_leeway){
                    option = 1;
                }
            }

            case"AB"->{ // compare alpha-beta rating
                double AB_rating1 = ((alpha * p) + (beta * avg_score)) / (alpha + beta);
                double AB_rating2 = ((alpha * neighbour_p) + (beta * neighbour_avg_score)) / (alpha + beta);
                if(AB_rating1 < AB_rating2)
                    option = 0;
                else if(AB_rating1 > AB_rating2)
                    option = 1;
            }
//            default->System.out.println("[NOTE] No edge weight learning condition configured.");
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


        double learning = 0;
        switch(EWLF){
            case"ROC"->         learning = ROC;
            case"pAD"->         learning = Math.abs(neighbour_p - p);
            case"pEAD"->        learning = Math.exp(Math.abs(neighbour_p - p));
            case"avgscoreAD"->  learning = Math.abs(neighbour_avg_score - avg_score);
            case"avgscoreEAD"-> learning = Math.exp(Math.abs(neighbour_avg_score - avg_score));
            case"pAD2"->        learning=Math.pow(Math.abs(neighbour_p - p), 2);
            case"pAD3"->        learning=Math.pow(Math.abs(neighbour_p - p), 3);
            case"AB"->          learning=Math.abs((((alpha * p) + (beta * avg_score)) / (alpha + beta))
                    - (((alpha * neighbour_p) + (beta * neighbour_avg_score)) / (alpha + beta)));
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
    public Player RandSelection(Player child){
        double w = selnoise;
        double[] effective_payoffs = new double[N];
        Player parent = null;
        double total = 0.0;
        double tally = 0.0;

        // calculate effective payoffs
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                Player player = grid.get(i).get(j);
                double player_avg_score = player.getAvgScore();
                double player_effective_payoff = Math.exp(w * player_avg_score);
                effective_payoffs[(i * rows) + j] = player_effective_payoff;
                total += player_effective_payoff;
            }
        }

        // fitter player ==> more likely to be selected
        double random_double = ThreadLocalRandom.current().nextDouble();
        for(int i = 0; i < N; i++){
            tally += effective_payoffs[i];
            double percentile = tally / total;
            if(random_double < percentile){
                int parent_row = (int) Math.floor(i / rows);
                int parent_col = i % rows;
                parent = grid.get(parent_row).get(parent_col);
                break;
            }
        }

        return parent;
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
     * Inspired by Akdeniz and van Veelen (2023).
     * Child's attributes are randomly and independently generated.
     */
    public void globalMutation(Player child){
        double new_p = ThreadLocalRandom.current().nextDouble();
        double new_q = ThreadLocalRandom.current().nextDouble();

        setStrategy(child, new_p, new_q);
    }



    /**
     * Inspired by Akdeniz and van Veelen (2023).
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
        for(ArrayList<Player> row:grid){
            for(Player player: row){
                calculateMeanSelfEdgeWeight(player);
                calculateMeanNeighbourEdgeWeight(player);
            }
        }
    }


    public void calculateMeanSelfEdgeWeight(Player player){
        double[] edge_weights = player.getEdgeWeights();
        int length = edge_weights.length;
        double mean_self_edge_weight = 0;

        // calculate mean of edge weights from player to its neighbours
        for(int i = 0; i < length; i++){
            mean_self_edge_weight += edge_weights[i];
        }
        mean_self_edge_weight /= length;

        player.setMeanSelfEdgeWeight(mean_self_edge_weight);
    }



    public void calculateMeanNeighbourEdgeWeight(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int size = neighbourhood.size();
        double mean_neighbour_edge_weight = 0;

        // calculate mean of edge weights directed at player
        for(int i = 0; i < size; i++) {
            Player neighbour = neighbourhood.get(i);
            double[] edge_weights = neighbour.getEdgeWeights();
//            mean_neighbour_edge_weight += neighbour.edge_weights[findMeInMyNeighboursNeighbourhood(neighbour)];
//            int x = 0; // PLACEHOLDER
            int index = findPlayer(player, neighbour);
            mean_neighbour_edge_weight += edge_weights[index];
        }
        mean_neighbour_edge_weight /= size;

        player.setMeanNeighbourEdgeWeight(mean_neighbour_edge_weight);
    }



    /**
     * Assume num player neighbours = num neighbour neighbours.<br>
     * Return index corresponding to player within neighbours neighbourhood.
     */
    public int findPlayer(Player player, Player neighbour){
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
                        " %-4s |" +//rows
                        " %-3s |" +//EWT
                        " %-9s |" +//EWLC
                        " %-11s |" +//EWLF
                        " %-3s |" +//ER
                        " %-6s |" +//ROC
                        " %-6s |" +//alpha
                        " %-6s |" +//beta
                        " %-7s |" +//leeway1
                        " %-7s |" +//leeway2
                        " %-7s |" +//leeway3
                        " %-7s |" +//leeway4
                        " %-7s |" +//leeway5
                        " %-7s |" +//leeway6
                        " %-7s |" +//leeway7
                        " %-9s |" +//varying
                        " %-9s |" +//variation
                        " %-6s |" +//numexp
                        " %-5s |" +//neigh
                        " %-8s |" +//sel
                        " %-8s |" +//selnoise
                        " %-3s |" +//ASD
                        " %-8s |" +//evo
                        " %-8s |" +//evonoise
                        " %-6s |" +//mut
                        " %-7s |" +//mutrate
                        " %-9s |" +//mutbound
                        " %-8s |" +//datarate
                        " %-9s |" +//injiter
                        " %-6s |" +//injp
                        " %-7s |" +//injsize
                        " desc%n" // ensure desc is the last column
                ,"config"
                ,"game"
                ,"runs"
                ,"iters"
                ,"rows"
                ,"EWT"
                ,"EWLC"
                ,"EWLF"
                ,"ER"
                ,"ROC"
                ,"alpha"
                ,"beta"
                ,"leeway1"
                ,"leeway2"
                ,"leeway3"
                ,"leeway4"
                ,"leeway5"
                ,"leeway6"
                ,"leeway7"
                ,"varying"
                ,"variation"
                ,"numexp"
                ,"neigh"
                ,"sel"
                ,"selnoise"
                ,"ASD"
                ,"evo"
                ,"evonoise"
                ,"mut"
                ,"mutrate"
                ,"mutbound"
                ,"datarate"
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
            System.out.printf("| %-4s ", settings[CI++]); //rows
            System.out.printf("| %-3s ", settings[CI++]); //EWT
            System.out.printf("| %-9s ", settings[CI++]); //EWLC
            System.out.printf("| %-11s ", settings[CI++]); //EWLF
            System.out.printf("| %-3s ", settings[CI++]); //ER
            System.out.printf("| %-6s ", settings[CI++]); //ROC
            System.out.printf("| %-6s ", settings[CI++]); //alpha
            System.out.printf("| %-6s ", settings[CI++]); //beta
            System.out.printf("| %-7s ", settings[CI++]); //leeway1
            System.out.printf("| %-7s ", settings[CI++]); //leeway2
            System.out.printf("| %-7s ", settings[CI++]); //leeway3
            System.out.printf("| %-7s ", settings[CI++]); //leeway4
            System.out.printf("| %-7s ", settings[CI++]); //leeway5
            System.out.printf("| %-7s ", settings[CI++]); //leeway6
            System.out.printf("| %-7s ", settings[CI++]); //leeway7
            System.out.printf("| %-9s ", settings[CI++]); //varying
            System.out.printf("| %-9s ", settings[CI++]); //variation
            System.out.printf("| %-6s ", settings[CI++]); //numexp
            System.out.printf("| %-5s ", settings[CI++]); //neigh
            System.out.printf("| %-8s ", settings[CI++]); //sel
            System.out.printf("| %-8s ", settings[CI++]); //selnoise
            System.out.printf("| %-3s ", settings[CI++]); //ASD
            System.out.printf("| %-8s ", settings[CI++]); //evo
            System.out.printf("| %-8s ", settings[CI++]); //evonoise
            System.out.printf("| %-6s ", settings[CI++]); //mut
            System.out.printf("| %-7s ", settings[CI++]); //mutrate
            System.out.printf("| %-9s ", settings[CI++]); //mutbound
            System.out.printf("| %-8s ", settings[CI++]); //datarate
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
        runs = Integer.parseInt(settings[CI++]);
        if(runs < 1){
            System.out.println("[ERROR] Invalid number of runs configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
        iters = Integer.parseInt(settings[CI++]);
        if(iters < 1){
            System.out.println("[ERROR] Invalid number of generations configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
        rows = Integer.parseInt(settings[CI++]);
        if(rows < 1){
            System.out.println("[ERROR] Invalid number of rows configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
        columns = rows; // square lattice
        N = rows * columns;
        EWT = settings[CI++];
        EWLC = settings[CI++];
        EWLF = settings[CI++];
        ER=Integer.parseInt(settings[CI++]);
        if(ER < 1){
            System.out.println("[ERROR] Invalid evolution rate configured. Exiting...");
            Runtime.getRuntime().exit(0);
        }
        ROC=applySettingDouble();


        alpha=applySettingDouble();
        beta=applySettingDouble();


        leeway1=applySettingDouble();
        leeway2=applySettingDouble();
        leeway3=applySettingDouble();
        leeway4=applySettingDouble();
        leeway5=applySettingDouble();
        leeway6=applySettingDouble();
        leeway7=applySettingDouble();
        varying=settings[CI++];
        experiment_series=(varying.equals(""))?false:true;
        variation=applySettingDouble();
        numexp=applySettingInt();
        neigh=settings[CI++];
        sel=settings[CI++];
        selnoise=applySettingDouble();
        ASD=settings[CI++];
        evo=settings[CI++];
        evonoise=applySettingDouble();
        mut=settings[CI++];
        mutrate=applySettingDouble();
        mutbound=applySettingDouble();
        datarate=applySettingInt();
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
        for (int i = 0; i < rows; i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                Player new_player = null;
                switch(game){
                    case"UG"->new_player = new Player(
                            ThreadLocalRandom.current().nextDouble(),
                            ThreadLocalRandom.current().nextDouble(),
                            leeway2);
                    case"DG"->new_player = new Player(
                            ThreadLocalRandom.current().nextDouble(),
                            0,
                            leeway2);
                }
                row.add(new_player);
            }
            grid.add(row);
        }
    }



    // reminder of something we might work on in later research.
//    /**
//     * Initialises a lattice grid population of Dictator Game players with strategies loaded from a .csv file.<br>
//     * WARNING: DO NOT TRY TO USE THE SAVED POP IF THE GAME IS NOT DG! This is because the strategies .csv file currently only stores p values.
//     */
//    public void initSavedDGPop() {
//        try {
//            br = new BufferedReader(new FileReader(data_filename_prefix + "Strategies.csv"));
//            String line;
//            int i = 0;
//            while ((line = br.readLine()) != null) {
//                String[] row_contents = line.split(",");
//                ArrayList<Player> row = new ArrayList<>();
//                for (int j = 0; j < row_contents.length; j++) {
//                    row.add(new Player(
//                            Double.parseDouble(row_contents[i]),
//                            0,
//                            leeway2));
//                }
//                i++;
//                grid.add(row);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }



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



    /**
     * Create folders to store data.
     */
//    public void createFolders(){
    public static void createFolders(){
        p_data_filename = experiment_results_folder_path + "\\p_data";
        EW_data_filename = experiment_results_folder_path + "\\EW_data";
        NSI_data_filename = experiment_results_folder_path + "\\NSI_data";
        try{ // create folders
            Files.createDirectories(Paths.get(p_data_filename));
            Files.createDirectories(Paths.get(EW_data_filename));
            Files.createDirectories(Paths.get(NSI_data_filename));
        }catch(IOException e){
            e.printStackTrace();
        }

    }



    /**
     * Collects initial settings into a string.
     */
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
        initial_settings+=", "+rows+" rows";
        initial_settings+=", EWT="+EWT;
        initial_settings+=", EWLC="+EWLC;
        initial_settings+=", EWLF="+EWLF;
        initial_settings+=", ER="+ER;
        if(ROC != 0)
            initial_settings+=", ROC="+ROC;


        if(alpha != 0)
            initial_settings+=", alpha="+alpha;
        if(beta != 0)
            initial_settings+=", beta="+beta;


        if(leeway1 != 0)
            initial_settings+=", leeway1="+leeway1;
        if(leeway2 != 0)
            initial_settings+=", leeway2="+leeway2;
        if(leeway3 != 0)
            initial_settings+=", leeway3="+leeway3;
        if(leeway4 != 0)
            initial_settings+=", leeway4="+leeway4;
        if(leeway5 != 0)
            initial_settings+=", leeway5="+leeway5;
        if(leeway6 != 0)
            initial_settings+=", leeway6="+leeway6;
        if(leeway7 != 0)
            initial_settings+=", leeway7="+leeway7;
        initial_settings+=", "+neigh+" neigh";
        initial_settings+=", "+sel+" sel";
        switch(sel){
            case"Rand"->initial_settings+=", selnoise="+selnoise;
        }
//        initial_settings+=", ASD="+Player.getASD();
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
     * Assigns adjacent neighbours to the player in a 2D square lattice grid with respect
     * to the von Neumann or Moore neighbourhood type.
     * von Neumann neighbourhood order: [right player, left player, above player, below player]
     * @param player is the player having their neighbourhood assigned.
     * @param y is the y coordinate of the player which represents the row they are in.
     * @param x is the x coordinate of the player which represents the column they are in.
     */
    public void assignAdjacentNeighbours(Player player, int y, int x){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();
        int x_plus_one = (((x + 1) % rows) + rows) % rows;
        int x_minus_one = (((x - 1) % rows) + rows) % rows;
        int y_plus_one = (((y + 1) % rows) + rows) % rows;
        int y_minus_one = (((y - 1) % rows) + rows) % rows;

        // add von neumann neighbours
        neighbourhood.add(grid.get(y).get(x_plus_one)); // neighbour at x+1 i.e. to the right
        neighbourhood.add(grid.get(y).get((x_minus_one))); // neighbour at x-1 i.e. to the left
        neighbourhood.add(grid.get(y_plus_one).get(x)); // neighbour at y+1 i.e. above
        neighbourhood.add(grid.get(y_minus_one).get(x)); // neighbour at y-1 i.e. below

        // add moore neighbours
        if(neigh.equals("M")){
            neighbourhood.add(grid.get(y_plus_one).get(x_plus_one)); // neighbour at (x+1,y+1)
            neighbourhood.add(grid.get(y_minus_one).get(x_plus_one)); // neighbour at (x+1,y-1)
            neighbourhood.add(grid.get(y_minus_one).get(x_minus_one)); // neighbour at (x-1,y-1)
            neighbourhood.add(grid.get(y_plus_one).get(x_minus_one)); // neighbour at (x-1,y+1)
        }

        int[] arr = new int[4];
        for(int i=0;i<player.getNeighbourhood().size();i++){
            arr[i] = player.getNeighbourhood().get(i).getID();
        }

        player.setNeighbourIDs(arr);

    }


    /**
     * Randomly assigns neighbours to the player. Does not assign the player as a neighbour of the
     * neighbour i.e. the neighbour assignment is one-way i.e. the edges are directed.
     * @param player is the player being assigned neighbours.
     * @param size is the number of neighbours to be assigned to the player's neighbourhood.
     */
    public void assignRandomNeighbours(Player player, int size){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();
        Set<Integer> rand_ints = new HashSet<>();
        while(rand_ints.size() < size){
            rand_ints.add(ThreadLocalRandom.current().nextInt(N));
        }
        List<Integer> positions = new ArrayList<>(rand_ints); // position or id of new neighbours
        for(int position: positions){
            int col = position / rows;
            int row = position % rows;
            neighbourhood.add(grid.get(col).get(row));

            // assign the player as the neighbour's neighbour.
            grid.get(col).get(row).getNeighbourhood().add(player);
        }
    }





    /**
     * Calculate the average value of p and the standard deviation of p across the population
     * at the current generation.
     */
    public void calculateOverallStats(){
        // calculate average p
        avg_p = 0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;

        // calculate p SD
        p_SD = 0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                p_SD += Math.pow(player.getP() - avg_p, 2);
            }
        }
        p_SD = Math.pow(p_SD / N, 0.5);
    }



    /**
     * Prepare program for the next generation.<br>
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void prepare(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
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
                player.setNSIPerNeighbour(new int[]{0,0,0,0});
            }
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
                s+=",mean avg p";
                s+=",avg p SD";
                s+=",runs";
                s+=",iters";
                s+=",neigh";
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
            s+="," + DF4.format(experiment_mean_avg_p);
            s+="," + DF4.format(experiment_SD_avg_p);
            s+="," + runs;
            s+="," + iters;
            s+="," + neigh;
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
            String filename = experiment_results_folder_path + "\\" + "avg_p_data.csv";
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



    /**
     * Writes a grid of strategies, i.e. the p values, of the pop to a given .csv file.
     */
    public void writepData(){
        try{
            String filename = p_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
            String s = "";
            for(int y = rows - 1; y >= 0; y--){
                for(int x = 0; x < columns; x++){
                    s += DF4.format(grid.get(y).get(x).getP());
                    if(x + 1 < columns){
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



    /**
     * Uses EW data to write a grid of 4x4 sub-grids into a .csv file.<br>
     * Assumes von Neumann neighbourhood type and rows=columns.
     */
    public void writeEWData(){
        try{
            String filename = EW_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename);
            String string = "";
            String[] substrings = new String[(rows * 4)];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
            for(int y = rows - 1; y >= 0; y--) {
                for (int x = 0; x < columns; x++) {
                    Player current = grid.get(y).get(x);
                    double[] edge_weights = current.getEdgeWeights();
                    ArrayList<Player> neighbourhood = current.getNeighbourhood();


                    // EW health of player is equal to sum of edge weights divided by num edge weights.
                    // this is equivalent to mean self edge weight plus mean neighbour edge weight divided by 2.
                    double EW_health = (current.getMeanSelfEdgeWeight() + current.getMeanNeighbourEdgeWeight()) / 2;


                    substrings[a] += "0,"
                            +edge_weights[2]+","
                            +neighbourhood.get(2).getEdgeWeights()[3]+","
                            +"0";
                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights()[0]+","


//                            +DF2.format(current.getP())+","
//                            +DF2.format(current.getAverageScore())+","


                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","


                            +edge_weights[0];
                    substrings[a+2] += current.getEdgeWeights()[1]+","


//                            +DF2.format(current.getMeanSelfEdgeWeight())+","
//                            +DF2.format(current.getMeanNeighbourEdgeWeight())+","


                            +DF2.format(EW_health)+","
                            +DF2.format(EW_health)+","


                            +neighbourhood.get(0).getEdgeWeights()[1];
                    substrings[a+3] += "0,"
                            +neighbourhood.get(3).getEdgeWeights()[2]+","
                            +edge_weights[3]+","
                            +"0";
                    if(x + 1 < columns){
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
            String[] substrings = new String[(rows * 4)];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
            for(int y = rows - 1; y >= 0; y--) {
                for (int x = 0; x < columns; x++) {
                    Player current = grid.get(y).get(x);
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
                    if(x + 1 < columns){
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
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                if(ID == grid.get(i).get(j).getID()){
                    player = grid.get(i).get(j);
                    break;
                }
            }
        }

        return player;
    }




    /**
     * Injects a cluster of players in the population with a given strategy.
     * Assumes square lattice population.
     */
    public void injectStrategyCluster(double new_strategy, int cluster_size){
        for(int i=0;i<cluster_size;i++){
            for(int j=0;j<cluster_size;j++){
                grid.get(i).get(j).setP(new_strategy);
            }
        }
    }


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
}

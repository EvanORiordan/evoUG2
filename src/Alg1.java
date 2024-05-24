import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
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
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    static String neighbourhood_type; // indicates the type of neighbourhood being enforced
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // 2D square lattice grid containing the population.
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    int gen = 0; // indicates which generation is currently running.
    static double DCF = 0.0;// distance cost factor
    static String initial_settings = "";// stores initial experimentation settings

    // fields related to experiment series
    static boolean experiment_series;//indicates whether to run experiment or experiment series where a parameter is varied
    static String varying_parameter;//indicates which parameter will be varied in an experiment series
    static double variation;//indicates by how much parameter will vary between subsequent experiments. the double data is used because it works for varying integer parameters as well as doubles.
    static int num_experiments;//indicates the number of experiments to occur in the series
    static int experiment_num = 0;//tracks which experiment is taking place at any given time during a series
    static List<String> possible_varying_parameters //redundant but useful as it illustrates to me the indices of the parameters available to be varied
            = new ArrayList<>(List.of(
                    "runs" //0
                    , "gens" //1
                    , "rows" //2
                    , "EPR" //3
                    , "ROC" //4
                    , "leeway1" //5
                    , "leeway2" // 10
                    , "leeway3" // 11
                    , "leeway4" // 12
                    , "leeway5" // 13
                    , "leeway6" // 14
                    , "sel noise" //6
                    , "evo noise" //7
                    , "u" //8
                    , "delta" //9
            ));
    static int run_num; // tracks which of the runs is currently executing


    // fields related to I/O operations
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
//    static String data_filename_prefix = "csv_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();
//    static String interaction_data_filename_prefix="csv_data\\interactions_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();
//    static int interaction_data_record_rate = 0; // by default, do not collect interaction data
    static String config_filename = "config.csv";
    static boolean save_pop = false;
    static boolean use_saved_pop = false;
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static String desc;//description of experiment from config file
    static LocalDateTime timestamp;
    static String timestamp_string;
    static String project_path = "C:\\Users\\Evan O'Riordan\\IdeaProjects\\evoUG2";
    static String data_folder_path = project_path + "\\csv_data";
    static String experiment_results_folder_path;
    static String series_data_filename;
    static String gen_strategy_data_filename;
    static String gen_interaction_data_filename;
    static int data_rate; // determines how often generational data is recorded, if applicable.
    static String gen_detailed_grid_filename;


    // fields related to edge weight learning (EWL)
    static String EWT; // edge weight type
    static String EWLC; // edge weight learning condition
    static String EWLF; // edge weight learning formula
    static int EPR = 1; // evolution phase rate: how often evolution phases occur e.g. if 5, then evo phase occurs every 5 gens
    static double ROC = 0.0; // EW rate of change
    static double leeway1 = 0.0; // defines global leeway affecting all players.
    static double leeway2 = 0.0; // defines bounds of interval that local leeway is generated from.
    static double leeway3 = 0.0; // defines factor used in calculation of leeway given wrt weight of edge to neighbour.
    static double leeway4 = 0.0; // defines factor used in calculation of leeway given wrt comparison of p vs neighbour p.
    static double leeway5 = 0.0; // defines factor used in calculation of leeway given wrt neighbour p.
    static double leeway6 = 0.0; // defines bounds of interval that random leeway is generated from.
    static double leeway7 = 0.0; // defines avg score comparison leeway factor.


    // fields related to evolution, selection, mutation
    static String evolution_method; // indicates which evolution function to call
    static double approach_noise; // affected by evo noise
    static String selection_method; // indicates which selection function to call
    static double w; // affected by sel noise
    static String mutation_method; // indicates which mutation function to call
    static double u; // represents mutation rate
    static double delta; // represents mutation noise






    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        setupEnvironment();

        setInitialSettings();

        // mark the beginning of the algorithm's runtime
        timestamp = LocalDateTime.now();
        timestamp_string = timestamp.getYear()
                +"-"+timestamp.getMonthValue()
                +"-"+timestamp.getDayOfMonth()
                +"_"+timestamp.getHour()
                +"-"+timestamp.getMinute()
                +"-"+timestamp.getSecond();
        experiment_results_folder_path = data_folder_path+"\\"+timestamp_string;
        try { // set up results folder
            Files.createDirectories(Paths.get(experiment_results_folder_path));
        }catch(IOException e){
            e.printStackTrace();
        }
        Instant start = Instant.now();
        System.out.println("Starting timestamp: "+timestamp);

        if(experiment_series){
            experimentSeries(); // run an experiment series
        } else {
            experiment(); // run a single experiment
        }

        // mark the end of the algorithm's runtime
        timestamp = LocalDateTime.now();
        System.out.println("Ending timestamp: "+timestamp);
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
    }



    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(){
        System.out.println();
        printInitialSettings();

        // store initial value of varying parameter
        ArrayList<Double> various_amounts = new ArrayList<>();
        switch(varying_parameter){
            case"runs"->various_amounts.add((double)runs);
            case"gens"->various_amounts.add((double)gens);
            case"rows"->various_amounts.add((double)rows);
            case"EPR"->various_amounts.add((double)EPR);
            case"ROC"->various_amounts.add(ROC);
            case"leeway1"->various_amounts.add(leeway1);
            case"leeway2"->various_amounts.add(leeway2);
            case"leeway3"->various_amounts.add(leeway3);
            case"leeway4"->various_amounts.add(leeway4);
            case"leeway5"->various_amounts.add(leeway5);
            case"leeway6"->various_amounts.add(leeway6);
            case"leeway7"->various_amounts.add(leeway7);
//            case"w"->various_amounts.add(w);
//            case"approach_noise"->various_amounts.add(approach_noise);
            case"u"->various_amounts.add(u);
            case"delta"->various_amounts.add(delta);
        }

        // run experiment series
        for(int i=0;i<num_experiments;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("\n===================\n" +
                    "NOTE: Start of experiment "+i+": "+varying_parameter+"="+DF4.format(various_amounts.get(i))+".");

            experiment(); // run an experiment

            switch(varying_parameter){ // change the value of the varying parameter
                case "runs"->{
                    runs+=(int)variation;
                    various_amounts.add((double)runs);
                }
                case "gens"->{
                    gens+=(int)variation;
                    various_amounts.add((double)gens);
                }
                case "rows"->{
                    rows+=(int)variation;
                    columns+=(int)variation;
                    N += (int) (variation * variation);
                    various_amounts.add((double)rows);
                }
                case "EPR"->{
                    EPR+=(int)variation;
                    various_amounts.add((double)EPR);
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
                case "w"->{
                    w+=variation;
                    various_amounts.add(w);
                }
                case "approach noise"->{
                    approach_noise+=variation;
                    various_amounts.add(approach_noise);
                }
                case "u"->{
                    u+=variation;
                    various_amounts.add(u);
                }
                case "delta"->{
                    delta+=variation;
                    various_amounts.add(delta);
                }
            }



            // informs user what the varying parameter's value was
            System.out.println("NOTE: End of "+varying_parameter+"="+DF4.format(various_amounts.get(i))+"."
                    +"\n===================");
        }

        // display a summary of the series in the console
        String summary = "";
        try{
//            br = new BufferedReader(new FileReader(data_filename_prefix+"Data.csv"));
            br = new BufferedReader(new FileReader(series_data_filename));
            String line;
            line = br.readLine(); // purposely ignore the row of headings
            int i=0; // indicates which index of the row we are currently on
            while((line=br.readLine()) != null){
                String[] row_contents = line.split(",");
                summary += "experiment="+row_contents[0];
                summary += "\tmean avg p="+DF4.format(Double.parseDouble(row_contents[1]));
                summary += "\tavg p SD="+DF4.format(Double.parseDouble(row_contents[2]));
                summary += "\t";
                summary+=varying_parameter+"=";
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
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(run_num = 0; run_num < runs; run_num++){
            Alg1 run = new Alg1(); // one run of the algorithm
            run.start(); // start the run
            mean_avg_p_of_experiment += run.avg_p; // tally the mean avg p of the experiment
            avg_p_values_of_experiment[run_num] = run.avg_p;

            // display the avg p of the pop at the end of the run
            System.out.println("avg p of run "+run_num
                    +" of experiment "+experiment_num
                    +": "+DF4.format(run.avg_p));
        }

        if(!experiment_series){
            System.out.println();
            printInitialSettings();
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment +=
                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats to user in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                +" avg p SD="+DF4.format(sd_avg_p_of_experiment));

        // write experiment data (results and settings) to a .csv data file.
        try{
//            String filename = data_filename_prefix + "Data.csv";
//            series_data_filename = experiment_results_folder_path + "\\" + timestamp_string + "_series_data.csv"; // use this instead if you want to be able to open multiple series data files at once.
            series_data_filename = experiment_results_folder_path + "\\" + "series_data.csv";
            if(experiment_num == 0){ // write column headings
                fw = new FileWriter(series_data_filename, false);
                fw.append("exp num");
                fw.append(",mean avg p");
                fw.append(",avg p SD");
                fw.append(",runs");
                fw.append(",gens");
                fw.append(",neighbourhood");
                fw.append(",N");
                fw.append(",EWT");
                fw.append(",EWLC");
                fw.append(",EWLF");
                fw.append(",EPR");
                fw.append(",ROC");
                fw.append(",leeway1");
                fw.append(",leeway2");
                fw.append(",leeway3");
                fw.append(",leeway4");
                fw.append(",leeway5");
                fw.append(",leeway6");
                fw.append(",leeway7");
//                fw.append(",selection");
//                fw.append(",w");
//                fw.append(",evolution");
//                fw.append(",approach noise");
                fw.append(",mutation");
                fw.append(",u");
                fw.append(",delta");
            } else {
                fw = new FileWriter(series_data_filename, true);
            }
            fw.append("\n" + experiment_num); // write row data
            fw.append("," + DF4.format(mean_avg_p_of_experiment));
            fw.append("," + DF4.format(sd_avg_p_of_experiment));
            fw.append("," + runs);
            fw.append("," + gens);
//            fw.append("," + Player.getNeighbourhoodType());
            fw.append("," + neighbourhood_type);
            fw.append("," + N);
            fw.append("," + EWT);
            fw.append("," + EWLC);
            fw.append("," + EWLF);
            fw.append("," + EPR);
            fw.append("," + ROC);
            fw.append("," + DF4.format(leeway1));
            fw.append("," + DF4.format(leeway2));
            fw.append("," + DF4.format(leeway3));
            fw.append("," + DF4.format(leeway4));
            fw.append("," + DF4.format(leeway5));
            fw.append("," + DF4.format(leeway6));
            fw.append("," + DF4.format(leeway7));
//            fw.append("," + selection_method);
//            fw.append("," + DF4.format(w));
//            fw.append("," + evolution_method);
//            fw.append("," + DF4.format(approach_noise));
            fw.append("," + mutation_method);
            fw.append("," + DF4.format(u));
            fw.append("," + DF4.format(delta));
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        experiment_num++; // move on to the next experiment in the series
    }


    /**
     * Method for running the core algorithm at the heart of the program.
     */
    @Override
    public void start(){
//        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
//            initSavedDGPop();
//        } else { // user wants to randomly generate a population
//            initRandomPop();
//        }
        initRandomPop();


        // record extra data for the first gen of the first run of the first experiment
        if(run_num == 0 && experiment_num == 0 && gen == 0) {
            gen_strategy_data_filename = experiment_results_folder_path + "\\strategies";
//            gen_interaction_data_filename = experiment_results_folder_path + "\\interactions";
            gen_detailed_grid_filename = experiment_results_folder_path + "\\detailed_grid";
            try{ // create folders
                Files.createDirectories(Paths.get(gen_strategy_data_filename));
//                Files.createDirectories(Paths.get(gen_interaction_data_filename));
                Files.createDirectories(Paths.get(gen_detailed_grid_filename));
            }catch(IOException e){
                e.printStackTrace();
            }
            writeStrategies();
        }




            // initialise neighbourhoods
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                Player player = grid.get(i).get(j);
                switch(neighbourhood_type){
                    case"VN","M"->assignAdjacentNeighbours(player, i, j);
                    case"random"->assignRandomNeighbours(player, 3);
                }
            }
        }

        // initialise edge weights
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }

        // players begin playing the game
        while(gen != gens) { // algorithm stops once this condition is reached
            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(game){
                        case"UG","DG"->{
                            switch(EWT){
                                case"1"->player.playUG1(); // EW affects probability to interact
                                case"2"->player.playUG2(); // EW affects payoff calculation
                            }
                        }


//                        case"PD"->player.playPD();
//                        case"IG"->player.playIG();
//                        case"TG"->player.playTG();
//                        case"PGG"->player.playPGG();
                    }
                }
            }

            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.EWL(EWLC, EWLF, ROC, leeway1, leeway3, leeway4, leeway5, leeway6, leeway7);
                }
            }

            // selection and evolution occur every EPR gens. each player in the grid tries to evolve.
            if((gen + 1) % EPR == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {

                        // select parent
                        Player parent = null;
                        switch(selection_method){
                            case "WRW" -> parent = player.weightedRouletteWheelSelection();
                            case "best" -> parent = player.bestSelection();
                            case "variable" -> parent = player.variableSelection(w);
                        }

                        // evolve child
                        switch (evolution_method) {
                            case "copy" -> player.copyEvolution(parent);
                            case "approach" -> player.approachEvolution(parent, approach_noise);
                        }

                        // mutate child
                        switch (mutation_method){
                            case "global" -> {
                                if(player.mutationCheck(u)){
                                    player.globalMutation();
                                }
                            }
                            case "local" -> {
                                if(player.mutationCheck(u)){
                                    player.localMutation(delta);
                                }
                            }
                        }
                    }
                }
            }

            calculateOverallStats(); // calculate the avg p and p SD of the pop
            calculateAverageEdgeWeights();

            // record extra data for the first run of the first experiment
            if(run_num == 0 && experiment_num == 0 && gen % data_rate == 0){
//            if(!experiment_series && runs == 1){

                // record the avg p and p SD of the gen that just ended
                System.out.println("avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD));
//                writePerGenData(data_filename_prefix + "PerGenData.csv");
                writeExperimentData();
                writeStrategies();
//                writeInteractionData();
                writeDetailedGrid();



                // record interaction data according to the interaction data record rate
//                if(gen + 1 % interaction_data_record_rate == 0){
//                    writeInteractionData(interaction_data_filename_prefix + "Gen" + gen);
//                }

//                if(gen==data_gen){ // at end of data gen, record lots of extra info
//                    System.out.println("Recording detailed data for gen "+data_gen+"...");
////                    writeStrategies(data_filename_prefix + "Strategies.csv");
//                    writeOwnConnections(data_filename_prefix + "OwnConnections.csv");
//                    writeAllConnections(data_filename_prefix + "AllConnections.csv");
//                }
            }

            prepare(); // prepare for next generation
            gen++; // move on to the next generation
        }
    }


    public void calculateAverageEdgeWeights(){
        for(ArrayList<Player> row:grid){
            for(Player player: row){
                player.calculateMeanSelfEdgeWeight();
                player.calculateMeanNeighbourEdgeWeight();
            }
        }
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
                            0.0,
                            leeway2);
                }
                row.add(new_player);
            }
            grid.add(row);
        }
    }



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
     * This function loads in a configuration of settings from a .csv file, allowing the user to assign the values they want to the environmental parameters.
     */
    public static void setupEnvironment(){
        ArrayList<String> configurations = new ArrayList<>(); // stores configurations
        try{ // load the configs from the file
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialise String to store rows of data
            line = br.readLine(); // purposely ignore the row of headings
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
                        " %-9s |" +//gens
                        " %-4s |" +//rows
                        " %-3s |" +//EWT
                        " %-9s |" +//EWLC
                        " %-13s |" +//EWLF
                        " %-3s |" +//EPR
                        " %-6s |" +//ROC
                        " %-7s |" +//leeway1
                        " %-7s |" +//leeway2
                        " %-7s |" +//leeway3
                        " %-7s |" +//leeway4
                        " %-7s |" +//leeway5
                        " %-7s |" +//leeway6
                        " %-7s |" +//leeway7
                        " %-10s |" +//varying
                        " %-9s |" +//variation
                        " %-7s |" +//num exp
                        " %-6s |" +//neigh
                        " %-8s |" +//sel
                        " %-9s |" +//sel noise
                        " %-9s |" +//PPM
                        " %-3s |" +//ASD
                        " %-8s |" +//evo
                        " %-9s |" +//evo noise
                        " %-6s |" +//mut
                        " %-6s |" +//u
                        " %-6s |" +//delta
                        " %-9s |" +//data rate (same length as gens)
                        " desc%n" // ensure desc is the last column
                ,"config"
                ,"game"
                ,"runs"
                ,"gens"
                ,"rows"
                ,"EWT"
                ,"EWLC"
                ,"EWLF"
                ,"EPR"
                ,"ROC"
                ,"leeway1"
                ,"leeway2"
                ,"leeway3"
                ,"leeway4"
                ,"leeway5"
                ,"leeway6"
                ,"leeway7"
                ,"varying"
                ,"variation"
                ,"num exp"
                ,"neigh"
                ,"sel"
                ,"sel noise"
                ,"PPM"
                ,"ASD"
                ,"evo"
                ,"evo noise"
                ,"mut"
                ,"u"
                ,"data rate"
                ,"delta");
        printTableBorder();



        // display config table rows
        int config_index;
        for(int i=0;i<configurations.size();i++){
            String[] settings = configurations.get(i).split(",");
            config_index = 0;

            System.out.printf("%-6d ", i); //config
            System.out.printf("| %-4s ", settings[config_index++]); //game
            System.out.printf("| %-6s ", settings[config_index++]); //runs
            System.out.printf("| %-9s ", settings[config_index++]); //gens
            System.out.printf("| %-4s ", settings[config_index++]); //rows
            System.out.printf("| %-3s ", settings[config_index++]); //EWT
            System.out.printf("| %-9s ", settings[config_index++]); //EWLC
            System.out.printf("| %-13s ", settings[config_index++]); //EWLF
            System.out.printf("| %-3s ", settings[config_index++]); //EPR
            System.out.printf("| %-6s ", settings[config_index++]); //ROC
            System.out.printf("| %-7s ", settings[config_index++]); //leeway1
            System.out.printf("| %-7s ", settings[config_index++]); //leeway2
            System.out.printf("| %-7s ", settings[config_index++]); //leeway3
            System.out.printf("| %-7s ", settings[config_index++]); //leeway4
            System.out.printf("| %-7s ", settings[config_index++]); //leeway5
            System.out.printf("| %-7s ", settings[config_index++]); //leeway6
            System.out.printf("| %-7s ", settings[config_index++]); //leeway7
            System.out.printf("| %-10s ", settings[config_index++]); //varying
            System.out.printf("| %-9s ", settings[config_index++]); //variation
            System.out.printf("| %-7s ", settings[config_index++]); //num exp
            System.out.printf("| %-6s ", settings[config_index++]); //neigh
            System.out.printf("| %-8s ", settings[config_index++]); //sel
            System.out.printf("| %-9s ", settings[config_index++]); //sel noise
            System.out.printf("| %-9s ", settings[config_index++]); //PPM
            System.out.printf("| %-3s ", settings[config_index++]); //ASD
            System.out.printf("| %-8s ", settings[config_index++]); //evo
            System.out.printf("| %-9s ", settings[config_index++]); //evo noise
            System.out.printf("| %-6s ", settings[config_index++]); //mut
            System.out.printf("| %-6s ", settings[config_index++]); //u
            System.out.printf("| %-6s ", settings[config_index++]); //delta
            System.out.printf("| %-6s ", settings[config_index++]); //delta
            System.out.printf("| %s ", settings[config_index]); //desc


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

        // apply the config
        String[] settings = configurations.get(config_num).split(",");
        config_index = 0;

        // game
        game = settings[config_index++];
        Player.setGame(game);

        // runs
        runs = Integer.parseInt(settings[config_index++]);

        // gens
        gens = Integer.parseInt(settings[config_index++]);

        // rows
        rows = Integer.parseInt(settings[config_index++]);

        // square topology by default
        columns = rows;
        N = rows * columns;

        // EWT,EWLC,EWLF,EPR,ROC,leeway1,leeway2,leeway3,leeway4,leeway5,leeway6,leeway7
        EWT = settings[config_index++];
        EWLC = settings[config_index++];
        EWLF = settings[config_index++];
        EPR=Integer.parseInt(settings[config_index++]);
        ROC=Double.parseDouble(settings[config_index++]);
        leeway1=Double.parseDouble(settings[config_index++]);
        leeway2=Double.parseDouble(settings[config_index++]);
        leeway3=Double.parseDouble(settings[config_index++]);
        leeway4=Double.parseDouble(settings[config_index++]);
        leeway5=Double.parseDouble(settings[config_index++]);
        leeway6=Double.parseDouble(settings[config_index++]);
        leeway7=Double.parseDouble(settings[config_index++]);

        // varying,variation,num exp
        if(!settings[config_index].equals("")) {
            varying_parameter = settings[config_index];
            experiment_series = true;
            variation = Double.parseDouble(settings[config_index+1]);
            num_experiments = Integer.parseInt(settings[config_index+2]);
        }
        config_index+=3;

        // neigh
        neighbourhood_type = settings[config_index++];

        //sel,sel noise,PPM,ASD
        selection_method=settings[config_index];
        switch(settings[config_index]){
            case"variable"->w=Double.parseDouble(settings[config_index+1]);
        }
        Player.setPPM(settings[config_index+2]);
        switch(Player.getPPM()){
            case"avg score"->Player.setASD(settings[config_index+3]);
        }
        config_index+=4;

        // evo,evo noise
        evolution_method=settings[config_index];
        switch(settings[config_index]){
            case"approach"->approach_noise=Double.parseDouble(settings[config_index+1]);
        }
        config_index+=2;

        // mut,u,delta
        mutation_method = settings[config_index];
        switch(mutation_method){
            case"global","local"->{
                u=Double.parseDouble(settings[config_index+1]);
                switch(mutation_method){
                    case"local"->delta=Double.parseDouble(settings[config_index+2]);
                }
            }
        }
        config_index+=3;

        //data rate
        data_rate = Integer.parseInt(settings[config_index++]);

        //desc
        desc = settings[config_index];






        // enable these lines if you want to use these features.
        // ask user if they want to save the initial pop (so that it can be used again)
//        save_pop = binaryQuestion("save initial pop? (0: no, 1: yes)");
        // ask user if they want to use the pop saved in the strategies .csv file
//        use_saved_pop = binaryQuestion("use saved pop? (0: no, 1: yes)");


        Player.setGrossPrize(1.0); // set prize per game to 1.0; makes no diff if bigger or smaller





        // you could ask the user how often they want to record interaction data. alternatively,
        // it could be a column of the config file.
//        interaction_data_record_rate = gens; // by default, do not collect interaction data
    }

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


    /**
     * Method used in main().<br>
     * Asks the user a binary question, receiving a boolean answer.
     */
    public static boolean binaryQuestion(String question){
        boolean answer = false;
        boolean keep_looping = true;
        do{
            System.out.printf(question);
            switch(scanner.nextInt()){
                case 0 ->keep_looping = false; // 0 => no/false
                case 1 -> { // 1 => yes/true
                    answer = true;
                    keep_looping = false;
                }
                default ->System.out.println("ERROR: select a valid option");
            }
        } while(keep_looping);
        return answer;
    }















    /**
     * Collects initial settings into a string.
     */
    public static void setInitialSettings(){
        if(experiment_series && experiment_num == 0){ // if at start of series
            initial_settings += "Experiment series ("+desc+")" +
                    " varying "+varying_parameter+
                    " by "+variation+
                    " between " + num_experiments+" experiments. ";
        } else if(!experiment_series){
            initial_settings += "Experiment: ";
        }
        initial_settings+=game;
        initial_settings+=", "+runs+" runs";
        initial_settings+=", "+gens+" gens";
        initial_settings+=", "+rows+" rows";
        initial_settings+=", EWT="+EWT;
        initial_settings+=", EWLC="+EWLC;
        initial_settings+=", EWLF="+EWLF;
        initial_settings+=", EPR="+EPR;
        if(ROC != 0)
            initial_settings+=", ROC="+ROC;
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
        initial_settings+=", "+neighbourhood_type+" neigh";
//        s+=", "+selection_method+" sel";
//        switch(selection_method){
//            case"variable"->s+=", w="+w;
//        }
        initial_settings+=", PPM="+Player.getPPM();
        switch(Player.getPPM()){
            case"avg score"->initial_settings+=", ASD="+Player.getASD();
        }
//        s+=", "+evolution_method+" evo";
//        switch(evolution_method){
//            case"approach"->s+=", approach noise="+approach_noise;
//        }
        switch(mutation_method){
            case"local","global"->{
                initial_settings+=", "+mutation_method+" mut";
                initial_settings+=", u="+DF4.format(u);
                switch(mutation_method){
                    case"local"->initial_settings+=", delta="+DF4.format(delta);
                }
            }
        }
    }

    /**
     * Prints initial experimentation settings.
     */
    public static void printInitialSettings(){
        System.out.println(initial_settings);
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

        int row_col_length = grid.size();

        int x_plus_one = (((x + 1) % row_col_length) + row_col_length) % row_col_length;
        int x_minus_one = (((x - 1) % row_col_length) + row_col_length) % row_col_length;
        int y_plus_one = (((y + 1) % row_col_length) + row_col_length) % row_col_length;
        int y_minus_one = (((y - 1) % row_col_length) + row_col_length) % row_col_length;

        neighbourhood.add(grid.get(y).get(x_plus_one)); // neighbour at x+1 i.e. to the right
        neighbourhood.add(grid.get(y).get((x_minus_one))); // neighbour at x-1 i.e. to the left
        neighbourhood.add(grid.get(y_plus_one).get(x)); // neighbour at y+1 i.e. above
        neighbourhood.add(grid.get(y_minus_one).get(x)); // neighbour at y-1 i.e. below

        if(neighbourhood_type.equals("M")){
            neighbourhood.add(grid.get(y_plus_one).get(x_plus_one)); // neighbour at (x+1,y+1)
            neighbourhood.add(grid.get(y_minus_one).get(x_plus_one)); // neighbour at (x+1,y-1)
            neighbourhood.add(grid.get(y_minus_one).get(x_minus_one)); // neighbour at (x-1,y-1)
            neighbourhood.add(grid.get(y_plus_one).get(x_minus_one)); // neighbour at (x-1,y+1)
        }
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
        // calculate avg p
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;

        // calculate p SD
        p_SD = 0.0;
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
            }
        }
    }








    /**
     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
     * and avg p on y-axis. Now also collects standard deviation (SD) data.<br>
     * <br>Steps:<br>
     * - Export the data of a single run to a .csv file<br>
     * - Import the .csv data into an Excel sheet<br>
     * - Separate the data into columns: gen number, avg p and SD for that gen<br>
     * - Create a line chart with the data.<br>
     */
    public void writeExperimentData(){
        try{
            // setup file writer
//            String filename = experiment_results_folder_path + "\\" + timestamp_string + "_experiment_data.csv"; // use this instead if you want to be able to open multiple series data files at once.
            String filename = experiment_results_folder_path + "\\" + "experiment_data.csv";
            if(gen == 0){ // apply headings to file before writing data
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen"
                        + ",avg p"
                        + ",p SD"
                        + "\n"
                );
                fw.close();
            }

            // write the data
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "");
            fw.append("," + DF4.format(avg_p));
            fw.append("," + DF4.format(p_SD));
            fw.append("\n");
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }








    /**
     * Writes the population to a .csv file in the form of a square grid of values in [0.0, 4.0].
     * Each value in the grid represents the sum of connections (edge weights) belonging to the
     * player in that position.<br>
     * This value represents how trusting the player is of their neighbours. If a player x has a 0 on
     * this grid, x must have a higher p value than their neighbours and depending on the ROC, they
     * have had a greater value of p for some time. If x has a 4, x must have the lowest p value in
     * their neighbourhood.<br>
     * This method also calculates the average sum of a player's own weights.
     */
    public void writeOwnConnections(String filename){
        double avg_own_connections = 0;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);
                    double sum = x.calculateOwnConnections();
                    avg_own_connections += sum;
                    fw.append(DF4.format(sum));
                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }


        // print avg own connections
        avg_own_connections /= N;
        System.out.println("avg own connections="+DF4.format(avg_own_connections));
    }


    /**
     * Write sum of connections of a player and the weights associated with them.<br>
     *
     * This method also calculates the average sum of a player's associated weights.<br>
     */
    public void writeAllConnections(String filename){
        double avg_all_connections = 0;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);
                    double sum = x.calculateAllConnections();
                    avg_all_connections += sum;
                    fw.append(DF4.format(sum));
                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }


        // print avg all connections
        avg_all_connections /= N;
        System.out.println("avg all connections="+DF4.format(avg_all_connections));
    }


    /**
     * Writes a grid of strategies, i.e. the p values, of the pop to a given .csv file.
     */
    public void writeStrategies(){
        try{
            String filename = gen_strategy_data_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename, false);
            String s = "";
            for(int y = rows - 1; y >= 0; y--){
                for(int x = 0; x < columns; x++){
                    s += DF4.format(grid.get(y).get(x).getP());
                    if(x + 1 < columns){
                        s += ",";
                    }
                }


//                for(int j=row.size()-1;j>0;j--){
//                    Player player = row.get(j);
//                    s += DF4.format(player.getP());
//                    if(j - 1 > row.size()){
//                        s += ",";
//                    }
//                }

                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Writes how many successful interactions the players had (this gen).
     */
//    public void writeInteractionData(){
//        try{
//            String filename = gen_interaction_data_filename + "\\gen" + gen + ".csv";
//            fw = new FileWriter(filename, false);
//            for(ArrayList<Player> row:grid){
//                for(int j=0;j<row.size();j++){
//                    Player x = row.get(j);
//
//                    // write number of successful interactions this gen.
//                    fw.append(Integer.toString(x.getNSI()));
//
//
//                    if(j+1<row.size()){
//                        fw.append(",");
//                    }
//                }
//                fw.append("\n");
//            }
//            fw.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }


    /**
     * Writes a detailed grid of smaller grids representing a cluster of the population.<br>
     * Smaller grids are 4x4 each.<br>
     * Big grid consists of 3x3 of smaller grids. In total, big grid is 12x12.<br>
     * Assumes von Neumann neighbourhood type.
     */
    public void writeDetailedGrid(){
        try{
            String filename = gen_detailed_grid_filename + "\\gen" + gen + ".csv";
            fw = new FileWriter(filename);
            String string = "";

//            String[] substrings = new String[12];

//            String[][] substrings = new String[3][4];

//            int i = 0;

//            for(int y = 2; y >= 0; y--){
//                for(int x = 0; x <= 2; x++){
//
//                    Player current = grid.get(y).get(x);
//                    double[] edge_weights = current.getEdgeWeights();
//                    ArrayList<Player> neighbourhood = current.getNeighbourhood();
//                    substrings[i] += ","+edge_weights[2]+","+ neighbourhood.get(2).getEdgeWeights()[3]+",\n";
//                    substrings[i+1] += neighbourhood.get(1).getEdgeWeights()[0]+","+DF4.format(current.getP())+","+DF4.format(current.getAverageScore())+","+edge_weights[0]+"\n";
//                    substrings[i+2] += current.getEdgeWeights()[1]+","+DF4.format(current.getMeanSelfEdgeWeight())+","+DF4.format(current.getMeanNeighbourEdgeWeight())+","+neighbourhood.get(0).getEdgeWeights()[1]+"\n";
//                    substrings[i+3] += ","+neighbourhood.get(3).getEdgeWeights()[2]+","+ edge_weights[3]+",\n";
//
//
//
//                }
//
////                i += 4;
//
//            }


//            String[][] substrings = new String[3][4];
//            for(int i=0;i<3;i++){ // for each small grid in the big grid
//                for(int j=0;j<4;j++){ // for each row of this small grid
//                    for(int y = 2; y >= 0; y--) { // access y-1, y and y+1 of current player
//                        for (int x = 0; x <= 2; x++) { // access x-1, x and x+1 of current player
////                            substrings[i][j] += ","+
//
//                            Player current = grid.get(y).get(x);
//                            double[] edge_weights = current.getEdgeWeights();
//                            ArrayList<Player> neighbourhood = current.getNeighbourhood();
//                            substrings[i] += ","+edge_weights[2]+","+ neighbourhood.get(2).getEdgeWeights()[3]+",\n";
//                            substrings[i+1] += neighbourhood.get(1).getEdgeWeights()[0]+","+DF4.format(current.getP())+","+DF4.format(current.getAverageScore())+","+edge_weights[0]+"\n";
//                            substrings[i+2] += current.getEdgeWeights()[1]+","+DF4.format(current.getMeanSelfEdgeWeight())+","+DF4.format(current.getMeanNeighbourEdgeWeight())+","+neighbourhood.get(0).getEdgeWeights()[1]+"\n";
//                            substrings[i+3] += ","+neighbourhood.get(3).getEdgeWeights()[2]+","+ edge_weights[3]+",\n";
//                        }
//                    }
//                }
//            }



//            String[] substrings = new String[12];
//            int a=0;
//            int b=0;
//            for(int i=0;i<3;i++){
//                for(int j=0;j<3;j++){
//                    for(int y = 2; y >= 0; y--) { // access y-1, y and y+1 of current player
//                        for (int x = 0; x <= 2; x++) { // access x-1, x and x+1 of current player
//                            substrings[a][b] +=
//                        }
//                    }
//                }
//            }
//            for(int i=0;i<substrings.length;i++){
//                string += substrings[i];
//            }




            String[] substrings = new String[12];
            for(int i=0;i<substrings.length;i++){
                substrings[i] = "";
            }
            int a=0;
            for(int y = 2; y >= 0; y--) {
                for (int x = 0; x <= 2; x++) {
                    Player current = grid.get(y).get(x);
                    double[] edge_weights = current.getEdgeWeights();
                    ArrayList<Player> neighbourhood = current.getNeighbourhood();
                    substrings[a] += " ,"+edge_weights[2]+","+ neighbourhood.get(2).getEdgeWeights()[3]+", ";
                    substrings[a+1] += neighbourhood.get(1).getEdgeWeights()[0]+","+DF2.format(current.getP())+","+DF2.format(current.getAverageScore())+","+edge_weights[0];
                    substrings[a+2] += current.getEdgeWeights()[1]+","+DF2.format(current.getMeanSelfEdgeWeight())+","+DF2.format(current.getMeanNeighbourEdgeWeight())+","+neighbourhood.get(0).getEdgeWeights()[1];
                    substrings[a+3] += " ,"+neighbourhood.get(3).getEdgeWeights()[2]+","+ edge_weights[3]+", ";
                    if(x + 1 <= 2){
                        for(int b=a;b<a+4;b++){
                            substrings[b] += ",";
                        }
//                        substrings[a] += ",";
//                        substrings[a+1] += ",";
//                        substrings[a+2] += ",";
//                        substrings[a+3] += ",";
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
     * Finds a player given the integer ID parameter.
     * @param ID of the player to find
     * @return player object with the given ID
     */
    public Player findPlayerByID(int ID){
        Player player = null;
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                if(ID == grid.get(i).get(j).getId()){
                    player = grid.get(i).get(j);
                }
            }
        }

        return player;
    }
}

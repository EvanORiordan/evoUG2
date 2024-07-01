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
    static int gens; // how many generations occur per experiment run. (technically, the number of gens will be this + 1 since first gen is gen number 0)
    static int runs; // how many times this experiment will be run.
    static String neigh; // indicates the type of neighbourhood being enforced
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // 2D square lattice contains the population
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    int gen = 1; // indicates which generation is currently running.
    static double DCF = 0;// distance cost factor
    static String initial_settings = "";// stores initial experimentation settings
    static int injgen; // injection gen: indicates when strategy injection will occur
    static double injp = 0.0; // injection p: indicates p value to be injected
    static int injsize = 0; // injection cluster size: indicates size of cluster to be injected


    // fields related to experiment series
    static boolean experiment_series;//indicates whether to run experiment or experiment series where a parameter is varied
    static String varying;//indicates which parameter will be varied in an experiment series
    static double variation;//indicates by how much parameter will vary between subsequent experiments. the double data is used because it works for varying integer parameters as well as doubles.
    static int numexp;//indicates the number of experiments to occur in the series
    static int experiment_num = 1;//tracks which experiment is taking place at any given time during a series
    static int run_num; // tracks which of the runs is currently executing
    static ArrayList<Double> various_amounts;


    // fields related to I/O operations
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static String desc;//description of experiment from config file
    static String start_timestamp_string;
    static String project_path = Paths.get("").toAbsolutePath().toString();
    static String data_folder_path = project_path + "\\csv_data";
    static String experiment_results_folder_path;
    static String series_data_filename;
    static String gen_p_data_filename;
    static String gen_EW_data_filename;
    static String gen_NSI_data_filename;
    static int datarate; // determines how often generational data is recorded. if 0, do not record data.
    static String[] settings;
    static int CI; // config index: facilitates construction of config table


    // fields related to edge weight learning (EWL)
    static String EWT; // edge weight type
    static String EWLC; // edge weight learning condition
    static String EWLF; // edge weight learning formula
    static int EPR = 1; // evolution phase rate: how often evolution phases occur e.g. if 5, then evo phase occurs every 5 gens
    static double ROC = 0; // rate of edge weight change per edge weight learning call
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
    static double mutrate = 0;
    static double mutamount = 0;






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
        experiment_results_folder_path = data_folder_path+"\\"+start_timestamp_string;
        try {
            Files.createDirectories(Paths.get(experiment_results_folder_path)); // create results storage folder
        }catch(IOException e){
            e.printStackTrace();
        }
        printExperimentResultsFolderPath();
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
            case"selnoise"->various_amounts.add(selnoise);
            case"evonoise"->various_amounts.add(evonoise);
            case"mutrate"->various_amounts.add(mutrate);
            case"mutamount"->various_amounts.add(mutamount);
            case"injgen"->various_amounts.add((double)injgen);
            case"injp"->various_amounts.add(injp);
            case"injsize"->various_amounts.add((double)injsize);
        }

        // run experiment series
        for(int i=0;i<numexp;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("\n===================\n" +
                    "NOTE: Start of experiment "+i+": "+varying+"="+DF4.format(various_amounts.get(i))+".");

            experiment(); // run an experiment of the series

            switch(varying){ // after experiment, change the value of the varying parameter
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
                case "mutamount"->{
                    mutamount+=variation;
                    various_amounts.add(mutamount);
                }
                case "injgen"->{
                    injgen += (int) variation;
                    various_amounts.add((double) injgen);
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
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs + 1];
        double sd_avg_p_of_experiment = 0;

        // perform/run the experiment multiple times if applicable
        for(run_num = 1; run_num <= runs; run_num++){
            Alg1 run = new Alg1(); // represents one run of the experiment
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

        // calculate stats of experiment
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment +=
                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats to user in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                +" avg p SD="+DF4.format(sd_avg_p_of_experiment));

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
                s+=",gens";
                s+=",neigh";
                s+=",N";
                s+=",EWT";
                s+=(EWLC.isEmpty())?"":",EWLC";
                s+=(EWLF.isEmpty())?"":",EWLF";
                s+=",EPR";
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
                s+=(mutamount==0)?"":",mutamount";
                s+=(injgen==-1)?"":",injgen";
                s+=(injp==0)?"":",injp";
                s+=(injsize==0)?"":",injsize";


            } else {
                fw = new FileWriter(series_data_filename, true);
            }

            // write row data
            s+="\n" + experiment_num;
            s+="," + DF4.format(mean_avg_p_of_experiment);
            s+="," + DF4.format(sd_avg_p_of_experiment);
            s+="," + runs;
            s+="," + gens;
            s+="," + neigh;
            s+="," + N;
            s+=","+EWT;
            s+=(EWLC.isEmpty())?"":","+EWLC;
            s+=(EWLF.isEmpty())?"":","+EWLF;
            s+=","+EPR;
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
            s+=(mutamount==0)?"":","+mutamount;
            s+=(injsize==-1)?"":","+injgen;
            s+=(injp==0)?"":","+injp;
            s+=(injsize==0)?"":","+injsize;
            fw.append(s);
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
        // no need to remove these comments since they act as a reminder as to things we might work on in later research.
//        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
//            initSavedDGPop();
//        } else { // user wants to randomly generate a population
//            initRandomPop();
//        }
        initRandomPop();

        // at the first gen of the first run of the first experiment, create result storage folders and record strategies
        if(datarate != 0
                && run_num == 1
                && experiment_num == 1
                && gen == 1) {
            createFolders();
        }

        // initialise neighbourhoods
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                Player player = grid.get(i).get(j);
                switch(neigh){
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
        while(gen <= gens) { // algorithm stops once this condition is reached

            // injection phase
            if(gen == injgen){
                injectStrategyCluster(injp, injsize);
            }

            // playing phase of generation
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(game){
                        case"UG","DG"->{
                            switch(EWT){
                                case"1"->player.playUG1();
                                case"2"->player.playUG2();
                            }
                        }


                        // reminder of something we might work on in later research
//                        case"PD"->player.playPD();
//                        case"IG"->player.playIG();
//                        case"TG"->player.playTG();
//                        case"PGG"->player.playPGG();
                    }
                }
            }

            // edge weight learning phase of generation
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.EWL(EWLC, EWLF, ROC, leeway1, leeway3, leeway4, leeway5, leeway6, leeway7);
                }
            }

            // evolution phase of generation.
            // occurs every EPR generations.
            // consists of selection, evolution and mutation, if applicable.
            if(gen % EPR == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {

                        // select parent
                        Player parent = null;
                        switch(sel){
                            case "WRW" -> parent = player.weightedRouletteWheelSelection();
                            case "best" -> parent = player.bestSelection();
                            case "variable" -> parent = player.variableSelection(selnoise);
                        }

                        // evolve child
                        switch (evo) {
                            case "copy" -> player.copyEvolution(parent);
                            case "approach" -> player.approachEvolution(parent, evonoise);
                        }

                        // mutate child
                        switch (mut){
                            case "global" -> {
                                if(player.mutationCheck(mutrate)){
                                    player.globalMutation();
                                }
                            }
                            case "local" -> {
                                if(player.mutationCheck(mutrate)){
                                    player.localMutation(mutamount);
                                }
                            }
                        }
                    }
                }
            }

            calculateOverallStats();
            calculateAverageEdgeWeights();

            // record generational data every datarate generations of the end of the first run of the first experiment.
            if(datarate != 0){
                if(run_num == 1
                        && experiment_num == 1
                        && gen % datarate == 0){
                    System.out.println("gen "+gen+": avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD));
                    writeExperimentData();
                    writepData();
                    writeEWData();
                    writeNSIData();
                }
            }


            prepare();
            gen++; // move on to the next generation
        }
    }


    /**
     * Create folders to store generational data.
     */
    public void createFolders(){
        gen_p_data_filename = experiment_results_folder_path + "\\p_data";
        gen_EW_data_filename = experiment_results_folder_path + "\\EW_data";
        gen_NSI_data_filename = experiment_results_folder_path + "\\NSI_data";
        try{ // create folders
            Files.createDirectories(Paths.get(gen_p_data_filename));
            Files.createDirectories(Paths.get(gen_EW_data_filename));
            Files.createDirectories(Paths.get(gen_NSI_data_filename));
        }catch(IOException e){
            e.printStackTrace();
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
     * This function loads in a configuration of settings from a .csv file, allowing the user to assign the values they want to the environmental parameters.
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
                        " %-9s |" +//gens
                        " %-4s |" +//rows
                        " %-3s |" +//EWT
                        " %-9s |" +//EWLC
                        " %-11s |" +//EWLF
                        " %-3s |" +//EPR
                        " %-6s |" +//ROC
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
                        " %-9s |" +//mutamount
                        " %-8s |" +//datarate
                        " %-9s |" +//injgen
                        " %-6s |" +//injp
                        " %-7s |" +//injsize
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
                ,"numexp"
                ,"neigh"
                ,"sel"
                ,"selnoise"
                ,"ASD"
                ,"evo"
                ,"evonoise"
                ,"mut"
                ,"mutrate"
                ,"mutamount"
                ,"datarate"
                ,"injgen"
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
            System.out.printf("| %-9s ", settings[CI++]); //gens
            System.out.printf("| %-4s ", settings[CI++]); //rows
            System.out.printf("| %-3s ", settings[CI++]); //EWT
            System.out.printf("| %-9s ", settings[CI++]); //EWLC
            System.out.printf("| %-11s ", settings[CI++]); //EWLF
            System.out.printf("| %-3s ", settings[CI++]); //EPR
            System.out.printf("| %-6s ", settings[CI++]); //ROC
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
            System.out.printf("| %-9s ", settings[CI++]); //mutamount
            System.out.printf("| %-8s ", settings[CI++]); //datarate
            System.out.printf("| %-9s ", settings[CI++]); //injgen
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
        gens = Integer.parseInt(settings[CI++]);
        rows = Integer.parseInt(settings[CI++]);
        columns = rows; // square lattice
        N = rows * columns;
        EWT = settings[CI++];
        EWLC = settings[CI++];
        EWLF = settings[CI++];
        EPR=Integer.parseInt(settings[CI++]);
        ROC=applySettingDouble();
        leeway1=applySettingDouble();
        leeway2=applySettingDouble();
        leeway3=applySettingDouble();
        leeway4=applySettingDouble();
        leeway5=applySettingDouble();
        leeway6=applySettingDouble();
        leeway7=applySettingDouble();
        varying=settings[CI++];
        experiment_series=(varying.equals(""))?false:true; // varying param indicates whether series or not
        variation=applySettingDouble();
        numexp=applySettingInt();
        neigh=settings[CI++];
        sel=settings[CI++];
        selnoise=applySettingDouble();
        Player.setASD(settings[CI++]);
        evo=settings[CI++];
        evonoise=applySettingDouble();
        mut=settings[CI++];
        mutrate=applySettingDouble();
        mutamount=applySettingDouble();
        datarate=applySettingInt();
        injgen=applySettingInt(); // set to 0 to prevent injection
        if(injgen>gens)
            System.out.println("NOTE: injgen > gens so no injection");
        injp=applySettingDouble();
        injsize=applySettingInt();
        desc=(settings[CI].equals(""))?"":settings[CI]; // final config param
        Player.setGrossPrize(1.0); // default prize per game. this amount doesnt matter since fitness metric is AVG score rather than score.
    }





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
        initial_settings+=", "+neigh+" neigh";
        initial_settings+=", "+sel+" sel";
        switch(sel){
            case"variable"->initial_settings+=", selnoise="+selnoise;
        }
        initial_settings+=", ASD="+Player.getASD();
        initial_settings+=", "+evo+" evo";
        switch(evo){
            case"approach"->initial_settings+=", evonoise="+evonoise;
        }
        switch(mut){
            case"local","global"->{
                initial_settings+=", "+mut+" mut";
                initial_settings+=", mutrate="+DF4.format(mutrate);
                switch(mut){
                    case"local"->initial_settings+=", mutamount="+DF4.format(mutamount);
                }
            }
        }
        if(injgen>-1){
            initial_settings+=", injgen="+injgen;
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

        int row_col_length = grid.size();

        int x_plus_one = (((x + 1) % row_col_length) + row_col_length) % row_col_length;
        int x_minus_one = (((x - 1) % row_col_length) + row_col_length) % row_col_length;
        int y_plus_one = (((y + 1) % row_col_length) + row_col_length) % row_col_length;
        int y_minus_one = (((y - 1) % row_col_length) + row_col_length) % row_col_length;

        neighbourhood.add(grid.get(y).get(x_plus_one)); // neighbour at x+1 i.e. to the right
        neighbourhood.add(grid.get(y).get((x_minus_one))); // neighbour at x-1 i.e. to the left
        neighbourhood.add(grid.get(y_plus_one).get(x)); // neighbour at y+1 i.e. above
        neighbourhood.add(grid.get(y_minus_one).get(x)); // neighbour at y-1 i.e. below

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
//            String filename = experiment_results_folder_path + "\\" + timestamp_string + "_experiment_data.csv"; // use this instead if you want to be able to open multiple series data files at once.
            String filename = experiment_results_folder_path + "\\" + "experiment_data.csv";
            String s="";
            if(gen == 1){ // apply headings to file before writing data
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
            String filename = gen_p_data_filename + "\\gen" + gen + ".csv";
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
            String filename = gen_EW_data_filename + "\\gen" + gen + ".csv";
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
            String filename = gen_NSI_data_filename + "\\gen" + gen + ".csv";
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
//                            System.out.println("hello");
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

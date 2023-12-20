import java.io.*;
import java.sql.Array;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    static int rows; // how many rows in the square grid.
    static int columns; // how many rows in the square grid.
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    static int EPR; // how often evolutionary phases occur e.g if 5, then evo occurs every 5 gens
    static double ROC; // rate of change wrt EWL1
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // contains the population.
    double avg_p; // the average value of p across the population.
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    int gen = 0; // indicates which generation is currently running.

    /**
     * Indicate whether to run an experiment or an experiment series.<br>
     * A series should be used when you want to vary a parameter of the algorithm between experiments.
     * Otherwise, a single experiment should suffice.
     */
    static boolean experiment_series;

    /**
     * varying_parameter indicates which parameter will be varied in an experiment series.<br>
     *
     * variation indicates by how much the parameter will vary between subsequent experiments in the
     * series. the double type here also works for varying integer type params such as gens.<br>
     *
     * num_experiments indicates the number of experiments to occur in the series.<br>
     *
     * experiment_number tracks which experiment is taking place at any given time during a series.<br>
     *
     * possible_varying_parameters stores all the possible parameters that can be varied in a series.<br>
     */
    static String varying_parameter;
    static int varying_parameter_index;
    static double variation;
    static int num_experiments;
    static int experiment_number = 0;
    static List<String> possible_varying_parameters = new ArrayList<>(
            List.of("runs", //0
                    "gens", //1
                    "rows", //2
                    "EPR", //3
                    "ROC", //4
                    "imitation_noise", //5
                    "approach_noise", //6
                    "mutation_rate", //7
                    "mutation_noise", //8
                    "w" //9
            ));



    static FileWriter fw;
    static BufferedReader br;

    // Prefix for filenames of generic data files.
    static String data_filename_prefix = "csv_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    // Prefix for filenames of interaction data files.
    static String interaction_data_filename_prefix = "csv_data\\interactions_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    static int interaction_data_record_rate = gens;
    static int data_gen;

    // Prefix for filenames of player data files.
    static String player_data_filename_prefix = "csv_data\\player_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    static String config_filename = "config.csv"; // filename of config file

    static String selection_method;
    static String evolution_method;
    static String mutation_method;
    static String edge_weight_learning_method;

    static double w;
    static double approach_noise;
    static double imitation_noise;
    static double mutation_rate;
    static double mutation_noise;



    static Scanner scanner = new Scanner(System.in); // Scanner object for receiving input
    static int choice;
    static boolean save_pop = false;
    static boolean use_saved_pop = false;
    static boolean config = false;








    // main method for executing the program
    public static void main(String[] args) {

        ArrayList<String> configurations = new ArrayList<>(); // stores configurations
        try{ // load the configs from the file
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialise String to store rows of data
            line = br.readLine(); // ignore the row of headings
            while((line = br.readLine()) != null){
                configurations.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        // display configs to user
        for(int i=0;i<configurations.size();i++){
            String[] settings = configurations.get(i).split(",");
            System.out.print("config "+i+": ");
            System.out.print("runs="+settings[0]);
            System.out.print(", gens="+settings[1]);
            System.out.print(", rows="+settings[2]);
            System.out.print(", EPR="+settings[3]);
            if(Double.parseDouble(settings[4]) > 0.0){
                System.out.print(", EWL1 with ROC=" + settings[4]);
            } else{
                System.out.print(", EWL2");
            }
            if(!settings[5].equals("")){
                System.out.print(", varying parameter="+settings[5]);
                System.out.print(", variation="+settings[6]);
                System.out.print(", num experiments="+settings[7]);
            }
            if(!settings[8].equals("")){
                System.out.print(", data gen="+settings[8]);
            }
            if(!settings[9].equals("")){
                System.out.print(", neighbourhood="+settings[9]);
            }
            if(!settings[10].equals("")){
                System.out.print(", selection="+settings[10]);
            }
            if(!settings[11].equals("")){
                System.out.print(", evolution="+settings[11]);
                switch(settings[11]){
                    case"imitation"->System.out.print(" with imitation noise="+settings[12]);
                    case"approach"->System.out.print(" with approach noise="+settings[12]);
                }
            }
            if(!settings[13].equals("")){
                System.out.print(", mutation="+settings[13]);
                System.out.print(" with mutation rate="+settings[14]);
                if(settings[13].equals("noise")){
                    System.out.print(", mutation bound="+settings[15]);
                }
            } else{
                System.out.print(", no mutation");
            }
            if(!settings[15].equals("")) {
                System.out.print(", description=" + settings[16]);
            }
            System.out.println();
        }

        // ask user which config they want to use
        System.out.println("which config would you like to use? (int)");
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
        runs = Integer.parseInt(settings[0]);
        gens = Integer.parseInt(settings[1]);
        rows = Integer.parseInt(settings[2]);
        EPR = Integer.parseInt(settings[3]);
        ROC = Double.parseDouble(settings[4]);
        if(ROC > 0.0){
            edge_weight_learning_method = "1";
        } else {
            edge_weight_learning_method = "2";
        }
        if(!settings[5].equals("")) {
            varying_parameter = settings[5];
            switch(varying_parameter){
                case"runs"->varying_parameter_index=0;
                case"gens"->varying_parameter_index=1;
                case"rows"->varying_parameter_index=2;
                case"EPR"->varying_parameter_index=3;
                case"ROC"->varying_parameter_index=4;
                case"imitation_noise"->varying_parameter_index=5;
                case"approach_noise"->varying_parameter_index=6;
                case"mutation_rate"->varying_parameter_index=7;
                case"mutation_noise"->varying_parameter_index=8;
                case"w"->varying_parameter_index=9;
            }
            experiment_series = true;
            variation = Double.parseDouble(settings[6]);
            num_experiments = Integer.parseInt(settings[7]);
        }
        if(!settings[8].equals("")){
            data_gen = Integer.parseInt(settings[8]);
        } else{
            data_gen = -1; // if no data gen entry, assign -1 to nullify the variable's effect
        }
        if(settings[9].equals("")){ // default neighbourhood is VN
            Player.setNeighbourhoodType("VN");
        }else{
            Player.setNeighbourhoodType(settings[9]);
        }
        if(settings[10].equals("")){ // default selection is WRW
            selection_method = "WRW";
        }else{
            selection_method = settings[10];
        }
        if(settings[11].equals("")){ // default evolution is copy
            evolution_method = "copy";
        } else if(settings[11].equals("imitation")){
            evolution_method = settings[11];
            imitation_noise = Double.parseDouble(settings[12]);
        } else if(settings[11].equals("approach")){
            evolution_method = settings[11];
            approach_noise = Double.parseDouble(settings[12]);
        } else{
            evolution_method = settings[11];
        }
        mutation_method = settings[13];
        if(!mutation_method.equals("")){
            mutation_rate = Double.parseDouble(settings[14]);
            if(mutation_method.equals("noise")){
                mutation_noise = Double.parseDouble(settings[15]);
            }
        }

        // enable these lines if you want to use them.
        // ask user if they want to save the initial pop (so that it can be used again)
//        save_pop = binaryQuestion("save initial pop? (0: no, 1: yes)");
        // ask user if they want to use the pop saved in the strategies .csv file
//        use_saved_pop = binaryQuestion("use saved pop? (0: no, 1: yes)");


        Player.setPrize(1.0); // set prize per game to 1.0; makes no diff if bigger or smaller

        // square topology
        columns = rows;
        N = rows * columns;

        // mark the beginning of the algorithm's runtime
        Instant start = Instant.now();
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());

        if(experiment_series){
            experimentSeries(); // run an experiment series
        } else {
            experiment(); // run a single experiment
        }

        // mark the end of the algorithm's runtime
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
    }







    /**
     * Method for running the core algorithm at the heart of the program.
     */
    public void start(){

        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
            try{
                br = new BufferedReader(new FileReader(data_filename_prefix + "Strategies.csv"));
                String line;
                int i=0;
                while((line = br.readLine()) != null) {
                    String[] row_contents = line.split(",");
                    ArrayList<Player> row = new ArrayList<>();
                    for(int j=0;j<row_contents.length;j++){
                        row.add(new Player(Double.parseDouble(row_contents[i]),0));
                    }
                    i++;
                    grid.add(row);
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        } else { // user wants to randomly generate a population
            for (int i = 0; i < rows; i++) {
                ArrayList<Player> row = new ArrayList<>();
                for (int j = 0; j < columns; j++) {
                    row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0));
                }
                grid.add(row);
            }
        }


        if(save_pop){ // user wants to record initial pop into strategies .csv file
            writeStrategies(data_filename_prefix + "Strategies.csv");
        }





        // initialise neighbourhoods
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        // players begin playing the game
        while(gen != gens) { // algorithm stops once this condition is reached
            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.play();
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(edge_weight_learning_method){
                        case "1" ->{
                            player.edgeWeightLearning1(ROC);
                        }
                        case "2" ->{
                            player.edgeWeightLearning2();
                        }
                    }
                }
            }


            /**
             * Selection and evolution occur every EPR gens.
             * Each player in the grid tries to evolve.
             */
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
                            case "imitation" -> player.imitationEvolution(parent, imitation_noise);
                            case "approach" -> player.approachEvolution(parent, approach_noise);
                        }

                        // mutate child
                        switch (mutation_method){
                            case "new" -> {
                                if(player.mutationCheck(mutation_rate)){
                                    player.newMutation();
                                }
                            }
                            case "noise" -> {
                                if(player.mutationCheck(mutation_rate)){
                                    player.noiseMutation(mutation_noise);
                                }
                            }
                        }
                    }
                }
            }

            /**
             * Only collect this data if interested in the results (of this individual
             * run) of this individual experiment.
             */
            if(!experiment_series && runs == 1){
                getStats();
                writePerGenData(data_filename_prefix + "PerGenData.csv");

                // write interaction data every once in a while
//                if(gen % interaction_data_record_rate == 0){
//                    writeInteractionData(interaction_data_filename_prefix + "Gen" + gen);
//                }


                if(gen==data_gen){
                    System.out.println("Recording detailed data for gen "+data_gen+"...");
                    writeStrategies(data_filename_prefix + "Strategies.csv");
                    writeOwnConnections(data_filename_prefix + "OwnConnections.csv");
                    writeAllConnections(data_filename_prefix + "AllConnections.csv");
                    writeFairRelationships(data_filename_prefix + "FairRelationships.csv");
                    Player x = grid.get(1).get(1);
                    writeDetailedPlayer(player_data_filename_prefix+"Player"+x.getId()+".csv",x);
                }
            }

            reset(); // reset certain player attributes.
            gen++; // move on to the next generation
        }

        getStats(); // get stats at the end of the run
    }












    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){
//        displaySettings(); // display settings of experiment

        // stats to be tracked
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(int i=0;i<runs;i++){
            Alg1 run = new Alg1();
            run.start();
            mean_avg_p_of_experiment += run.avg_p;
            avg_p_values_of_experiment[i] = run.avg_p;

            // display the final avg p of the pop of the run that just concluded.
            // this is a different print statement than the one in a similar position in DG18.java!
            System.out.println("final avg p of run "+i+" of experiment "+experiment_number+
                    ": "+run.avg_p);
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment +=
                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                + ", avg p SD="+DF4.format(sd_avg_p_of_experiment)
        );




        // write results and settings to a .csv data file.
        try{
            String filename = data_filename_prefix + "Data.csv";

            // write headings
            if(experiment_number == 0){
                fw = new FileWriter(filename, false);
                fw.append("experiment");
                fw.append(",mean avg p");
                fw.append(",avg p SD");
                fw.append(",runs");
                fw.append(",gens");
                fw.append(",neighbourhood");
                fw.append(",N");
                if(edge_weight_learning_method.equals("1")) {
                    fw.append(",EWL1 ROC");
                }
                fw.append(",EPR");
                fw.append(",selection");
                fw.append(",evolution");
                if(!mutation_method.equals("")){
                    fw.append(",mutation");
                    fw.append(",mutation rate");
                }
            } else {
                fw = new FileWriter(filename, true);
            }

            // write data
            fw.append("\n" + experiment_number);
            fw.append("," + DF4.format(mean_avg_p_of_experiment));
            fw.append("," + DF4.format(sd_avg_p_of_experiment));
            fw.append("," + runs);
            fw.append("," + gens);
            fw.append("," + Player.getNeighbourhoodType());
            fw.append("," + N);
            if(edge_weight_learning_method.equals("1")){
                fw.append("," + ROC);
            }
            fw.append("," + EPR);

            switch(selection_method){ // write selection method
                case "WRW" -> fw.append(",WRW");
                case "best" -> fw.append(",best");
                case "variable" -> fw.append(",variable selection with w="+DF4.format(w));
            }

            switch (evolution_method) { // write evolution method
                case "copy" -> fw.append(",copy");
                case "imitation" -> fw.append(",imitation noise=" + DF4.format(imitation_noise));
                case "approach" -> fw.append(",approach noise=" + DF4.format(approach_noise));
            }

            switch (mutation_method){ // write mutation method
//                case "new" -> fw.append(",mutation rate=" + DF4.format(Player.getMutationRate()));
                case "new" -> fw.append(",mutation rate=" + DF4.format(mutation_rate));
                case "noise" -> {
                    fw.append(",noise mutation="+DF4.format(mutation_noise));
                    fw.append(",mutation rate=" + DF4.format(mutation_rate));
                }
                default -> fw.append(",no mutation");
            }

            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        experiment_number++; // indicates that we are moving onto the next experiment in series
    }







    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(){

        // store initial value of varying parameter
        ArrayList<Double> various_amounts = new ArrayList<>();
        switch(varying_parameter_index){
            case 0->various_amounts.add((double)runs);
            case 1->various_amounts.add((double)gens);
            case 2->various_amounts.add((double)rows);
            case 3->various_amounts.add((double)EPR);
            case 4->various_amounts.add(ROC);
            case 5->various_amounts.add(imitation_noise);
            case 6->various_amounts.add(approach_noise);
            case 7->various_amounts.add(mutation_rate);
            case 8->various_amounts.add(mutation_noise);
            case 9->various_amounts.add(w);
        }

        // run experiment series
        for(int i=0;i<num_experiments;i++){
            System.out.println("varying parameter is at "+various_amounts.get(i));
            experiment(); // run the experiment and store its final data
            switch(varying_parameter_index){ // change the value of the varying parameter
                case 0->{
                    runs+=(int)variation;
                    various_amounts.add((double)runs);
                }
                case 1->{
                    gens+=(int)variation;
                    various_amounts.add((double)gens);
                }
                case 2->{
                    rows+=(int)variation;
                    various_amounts.add((double)rows);
                }
                case 3->{
                    EPR+=(int)variation;
                    various_amounts.add((double)EPR);
                }
                case 4->{
                    ROC+=variation;
                    various_amounts.add(ROC);
                }
                case 5->{
                    imitation_noise+=variation;
                    various_amounts.add(imitation_noise);
                }
                case 6->{
                    approach_noise+=variation;
                    various_amounts.add(approach_noise);
                }
                case 7->{
                    mutation_rate+=variation;
                    various_amounts.add(mutation_rate);
                }
                case 8->{
                    mutation_noise+=variation;
                    various_amounts.add(mutation_noise);
                }
                case 9->{
                    w+=variation;
                    various_amounts.add(w);
                }
            }
        }

        // display a summary of the series in the console
        String summary = "";
        try{
            br = new BufferedReader(new FileReader(data_filename_prefix+"Data.csv"));
            String line;
            line = br.readLine();
            int i=0; // indicates which index of the row we are currently on
            while((line=br.readLine()) != null){
                String[] row_contents = line.split(",");
                summary += "experiment="+row_contents[0];
                summary += "\tmean avg p="+DF4.format(Double.parseDouble(row_contents[1]));
                summary += "\tp SD="+DF4.format(Double.parseDouble(row_contents[2]));
                summary += "\t";
                switch(varying_parameter_index){
                    case 0->summary+="runs=";
                    case 1->summary+="gens=";
                    case 2->summary+="rows=";
                    case 3->summary+="EPR=";
                    case 4->summary+="ROC=";
                    case 5->summary+="imitation noise=";
                    case 6->summary+="approach noise=";
                    case 7->summary+="mutation rate=";
                    case 8->summary+="mutation noise=";
                    case 9->summary+="w=";
                }
                summary += DF4.format(various_amounts.get(i));
                i++;
                summary += "\n";
            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println(summary);
    }






    /**
     * Calculate the average value of p across the population at the current gen.<br>
     * The most important avg p is that of the final gen. That particular value is what is being
     * used to calculate the avg p of the experiment as a whole.<br>
     */
    public void getStats(){
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;
    }


    /**
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setOldP(player.getP());
                player.setNumInteractions(0);
                player.setNumSuccessfulInteractions(0);
                player.setNumSuccessfulDictations(0);
                player.setNumSuccessfulReceptions(0);
            }
        }
    }



    /**
     * Displays experiment settings.
     */
//    public static void displaySettings(){
//        String s = "";
//
//        if(experiment_series && experiment_number == 0){ // if at start of series
//            s += "Experiment series: \nVarying "+varying_parameter+" by "+variation+ " between " +
//                    num_experiments+" experiments with settings: ";
//        } else {
//            s += "Experiment with settings: ";
//        }
//
//        s+="\n";
//
//        s += "runs="+runs;
//        s += ", gens="+gens;
//        s += ", neighbourhood="+Player.getNeighbourhoodType();
//        s += ", N="+N;
//        if(edge_weight_learning_method.equals("1")){
//            s += ", EWL1 with ROC="+DF4.format(ROC);
//        } else {
//            s += ", EWL2";
//        }
//        s += ", EPR="+EPR;
//
//        // state the selection method used
//        switch(selection_method){
//            case "WRW" -> s += ", WRW selection";
//            case "best" -> s += ", best selection";
//            case "variable" -> s += ", variable selection with w=" + DF4.format(w);
//        }
//
//        // state the evolution method used
//        switch (evolution_method) {
//            case "copy" -> s += ", copy evolution";
//            case "imitation" -> s += ", imitation evolution with noise="
//                    + DF4.format(imitation_noise);
//            case "approach" -> s += ", approach evolution with noise="
//                    + DF4.format(approach_noise);
//        }
//
//        // state the mutation method used
//        switch (mutation_method){
////            case "new" -> s += ", new mutation with mutation rate="+ DF4.format(Player.getMutationRate());
//            case "new" -> s += ", new mutation with mutation rate="+DF4.format(mutation_rate);
//            case "noise" -> s += ", noise mutation with mutation rate="+DF4.format(mutation_rate)+" and " +
//                    "noise="+DF4.format(mutation_noise);
//            default -> s += ", no mutation";
//        }
//
//        s += ":";
//        System.out.println(s);
//    }






    /**
     * Method for asking the user a binary question to receive a boolean answer.
     */
    public static boolean binaryQuestion(String question){
        boolean answer = false;
        boolean keep_looping = true;
        do{
            System.out.printf(question);
            switch(scanner.nextInt()){
                case 0 -> { // 0 => no/false
                    keep_looping = false;
                }
                case 1 -> { // 1 => yes/true
                    answer = true;
                    keep_looping = false;
                }
                default -> {
                    System.out.println("ERROR: select a valid option");
                }
            }
        } while(keep_looping);
        return answer;
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
    public void writeStrategies(String filename){
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player player = row.get(j);

                    // write strategy.
                    fw.append(DF4.format(player.getP()));


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
    }


    /**
     * Tracks how many successful interactions the players had (this gen).
     */
    public void writeInteractionData(String filename){
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);

                    // write number of successful interactions this gen.
                    fw.append(Integer.toString(x.getNumSuccessfulInteractions()));


                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
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
    public void writePerGenData(String filename){
        double SD = calculateSD();

        try{
            // reset the .csv file for this run.
            if(gen == 0){
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen"
                        + ",avg p"
                        + ",p SD"
                        + ",gens"
                        + ",neighbourhood"
                        + ",N"
                        + ",ROC"
                        + ",EPR"
                        + "\n"
                );
                fw.close();
            }

            // add the data to the .csv file.
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "");
            fw.append("," + DF4.format(avg_p));
            fw.append("," + DF4.format(SD));
            fw.append("," + gens);
            fw.append("," + Player.getNeighbourhoodType());
            fw.append("," + N);
//                    + "," + Player.getROC()
            fw.append("," + ROC);
            fw.append("," + EPR);
            fw.append("\n");
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Calculates SD of the pop wrt p.
     */
    public double calculateSD(){
        double SD = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                SD += Math.pow(player.getP() - avg_p, 2);
            }
        }
        SD = Math.pow(SD / N, 0.5);

        return SD;
    }







    /**
     * Writes the number of "fair" relationships per node.
     */
    public void writeFairRelationships(String filename){
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);

                    // write number of fair relationships of x this gen.
                    fw.append(Integer.toString(x.getNumFairRelationships()));

                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Writes a detailed account of player x to a .csv file.
     */
    public void writeDetailedPlayer(String filename, Player x){
        try{
            fw = new FileWriter(filename, false);

            // neighbours of x
            Player up = x.getNeighbourhood().get(0);
            Player down = x.getNeighbourhood().get(0);
            Player left = x.getNeighbourhood().get(0);
            Player right = x.getNeighbourhood().get(0);

            // weights of edges pointing from neighbours to x
            double neighbour_ew_up = up.getEdgeWeights()[x.findXInNeighboursNeighbourhood(up)]; // ew of neighbour above x
            double neighbour_ew_down = down.getEdgeWeights()[x.findXInNeighboursNeighbourhood(down)]; // ew of neighbour below x
            double neighbour_ew_left = left.getEdgeWeights()[x.findXInNeighboursNeighbourhood(left)]; // ew of neighbour to x's left
            double neighbour_ew_right = right.getEdgeWeights()[x.findXInNeighboursNeighbourhood(right)]; // ew of neighbour to x's right

            // weights of edges from x to x's neighbours
            double ew_up = x.getEdgeWeights()[0];
            double ew_down = x.getEdgeWeights()[1];
            double ew_left = x.getEdgeWeights()[2];
            double ew_right = x.getEdgeWeights()[3];

            // general statistics relating to x
            double strategy = x.getP();
            int num_fair_relationships = x.getNumFairRelationships();
            double own_connections = x.calculateOwnConnections();
            double all_connections = x.calculateAllConnections();

//            DF4.format()
            // write data in the form of a 4x4 grid
            fw.append(","+DF4.format(ew_up)+","+DF4.format(neighbour_ew_up)+"\n"
                    +DF4.format(neighbour_ew_left)+","+DF4.format(strategy)+","+DF4.format(num_fair_relationships)+","+DF4.format(ew_right)+"\n"
                    +DF4.format(ew_left)+","+DF4.format(own_connections)+","+DF4.format(all_connections)+","+DF4.format(neighbour_ew_right)+"\n"
                    +","+DF4.format(neighbour_ew_down)+","+DF4.format(ew_down)+"\n"
            );
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

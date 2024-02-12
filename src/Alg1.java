import java.io.*;
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
    static FileWriter fw;
    static BufferedReader br;
    static String game; // what game is being played
    static int rows; // how many rows in the square grid.
    static int columns; // how many rows in the square grid.
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // contains the population.
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    int gen = 0; // indicates which generation is currently running.


    /**
     * experiment_series indicates whether to run an experiment or an experiment series. A series should be
     * used when you want to vary a parameter of the algorithm between experiments. Otherwise, a single
     * experiment should suffice.<br>
     *
     * varying_parameter indicates which parameter will be varied in an experiment series.<br>
     *
     * variation indicates by how much the parameter will vary between subsequent experiments in the
     * series. the double type here also works for varying integer type params such as gens.<br>
     *
     * num_experiments indicates the number of experiments to occur in the series.<br>
     *
     * experiment_number tracks which experiment is taking place at any given time during a series.<br>
     *
     * possible_varying_parameters stores all the possible parameters that can be varied in a series.
     * Currently, the ArrayList just indicates the indices of the possible varying parameters.<br>
     */
    static boolean experiment_series;
    static String varying_parameter;
    static int varying_parameter_index;
    static double variation;
    static int num_experiments;
    static int experiment_number = 0;
    static List<String> possible_varying_parameters = new ArrayList<>(
            List.of("runs" //0
                    , "gens" //1
                    , "rows" //2
                    , "EPR" //3
                    , "ROC" //4
                    , "w" //5
                    , "approach_noise" //6
                    , "u" //7
                    , "delta" //8
            ));


    // Prefix for filenames of generic data files.
    static String data_filename_prefix = "csv_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();


    // Prefix for filenames of interaction data files.
    static String interaction_data_filename_prefix="csv_data\\interactions_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();
    static int interaction_data_record_rate;
    static int data_gen; // is used only during single experiments


    // Prefix for filenames of player data files.
    static String player_data_filename_prefix="csv_data\\player_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();


    static String config_filename = "config.csv"; // filename of config file


    static String evolution_method;


    static String EWLE; // edge weight learning equation
    static int EPR = 1; // how often evolution phases occur e.g. if 5, then evo phase occurs every 5 gens
    static double ROC = 0.0; // EW rate of change
    static double leeway = 0.0; // leeway affecting EWL


    static String selection_method;
    static double w;
    static double approach_noise;


    static String mutation_method;
    static double u; // represents mutation rate
    static double delta; // represents mutation noise


    static Scanner scanner = new Scanner(System.in); // Scanner object for receiving input
    static boolean save_pop = false;
    static boolean use_saved_pop = false;










    // main method for executing the program
    public static void main(String[] args) {

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

        // display configs to user
        int config_index;
        for(int i=0;i<configurations.size();i++){
            String[] settings = configurations.get(i).split(",");
            System.out.print(i+": "); // begin displaying the config
            config_index = 0;

            // game
            System.out.print(settings[config_index]);
            config_index++;

            // runs
            System.out.print(", runs="+settings[config_index]);
            config_index++;

            // gens
            System.out.print(", gens="+settings[config_index]);
            config_index++;

            // rows
            System.out.print(", rows="+settings[config_index]);
            config_index++;

            // EWLE,EPR,ROC,leeway
            switch(settings[config_index]){
                default->System.out.print(", no EWL");
                case"ROC","AD","EAD"->System.out.print(", EWL equation="+settings[config_index]
                        +", EPR="+settings[config_index+1]
                        +", ROC="+settings[config_index+2]
                        +", leeway="+settings[config_index+3]);
            }
            config_index+=4;

            // varying,variation,num exp
            if(!settings[config_index].equals("")){
                System.out.print(", varying parameter="+settings[config_index]+", variation="+settings[config_index+1]+
                        ", num experiments="+settings[config_index+2]);
            }
            config_index+=3;

            // data gen
            if(!settings[config_index].equals("")){
                System.out.print(", data gen="+settings[config_index]);
            }
            config_index++;

            // neigh
            System.out.print(", "+settings[config_index]+" neighbourhood");
            config_index++;

            //sel,sel noise,PPM,ASD
            switch(settings[config_index]){
                case"WRW"->System.out.print(", WRW selection");
                case"best"->System.out.print(", best selection");
                case"variable"->System.out.print(", variable selection with w="+settings[config_index+1]);
            }
            System.out.print(", PPM="+settings[config_index+2]);
            switch(settings[config_index]){
                case"avg score"->System.out.print(", ASD="+settings[config_index+3]);
            }
            config_index+=4;


            // evo,evo noise
            switch(settings[config_index]){
                case"copy"->System.out.print(", copy evolution");
                case"approach"->System.out.print(", approach evolution with noise="+settings[config_index+1]);
            }
            config_index+=2;

            //mut,u,delta
            switch(settings[config_index]){
                case""-> System.out.print(", no mutation");
                case"local"->System.out.print(", local mutation with u="+settings[config_index+1]+
                            " and delta="+settings[config_index+2]);
                case"global"-> System.out.print(", global mutation with u="+settings[config_index+1]);
            }
            config_index+=3;

            // desc
            if(!settings[config_index].equals("")) {
                System.out.print(", description: " + settings[config_index]);
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
        config_index = 0;

        // game
        game = settings[config_index];
        Player.setGame(game);
        config_index++;

        // runs
        runs = Integer.parseInt(settings[config_index]);
        config_index++;

        // gens
        gens = Integer.parseInt(settings[config_index]);
        config_index++;

        // rows
        rows = Integer.parseInt(settings[config_index]);
        config_index++;

        // EWLE,EPR,ROC,leeway
        EWLE = settings[config_index];
        switch(EWLE){
            case"ROC","AD","EAD"->{
                EPR=Integer.parseInt(settings[config_index+1]);
                ROC=Double.parseDouble(settings[config_index+2]);
                leeway=Double.parseDouble(settings[config_index+3]);
            }
        }
        config_index+=4;



        // varying,variation,num exp
        if(!settings[config_index].equals("")) {
            varying_parameter = settings[config_index];
            switch(varying_parameter){
                case"runs"->varying_parameter_index=0;
                case"gens"->varying_parameter_index=1;
                case"rows"->varying_parameter_index=2;
                case"EPR"->varying_parameter_index=3;
                case"ROC"->varying_parameter_index=4;
                case"w"->varying_parameter_index=5;
                case"approach_noise"->varying_parameter_index=6;
                case"u"->varying_parameter_index=7;
                case"delta"->varying_parameter_index=8;
            }
            experiment_series = true;
            variation = Double.parseDouble(settings[config_index+1]);
            num_experiments = Integer.parseInt(settings[config_index+2]);
        }
        config_index+=3;

        // data gen
        if(!settings[config_index].equals("")){
            data_gen = Integer.parseInt(settings[config_index]);
        } else{
            data_gen = -1; // if no data gen entry, assign -1 to nullify the variable's effect
        }
        config_index++;

        // neigh
        Player.setNeighbourhoodType(settings[config_index]);
        config_index++;

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






        // enable these lines if you want to use these features.
        // ask user if they want to save the initial pop (so that it can be used again)
//        save_pop = binaryQuestion("save initial pop? (0: no, 1: yes)");
        // ask user if they want to use the pop saved in the strategies .csv file
//        use_saved_pop = binaryQuestion("use saved pop? (0: no, 1: yes)");


        Player.setPrize(1.0); // set prize per game to 1.0; makes no diff if bigger or smaller


        // square topology by default
        columns = rows;
        N = rows * columns;


        // you could ask the user how often they want to record interaction data. alternatively,
        // it could be a column of the config file.
        interaction_data_record_rate = gens; // by default, do not collect interaction data



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
        // WARNING: DO NOT TRY TO USE THE SAVED POP IF THE GAME IS NOT DG! This is because the
        // strategies file currently only stores p values.
        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
            try{
                br = new BufferedReader(new FileReader(data_filename_prefix + "Strategies.csv"));
                String line;
                int i=0;
                while((line = br.readLine()) != null) {
                    String[] row_contents = line.split(",");
                    ArrayList<Player> row = new ArrayList<>();
                    for(int j=0;j<row_contents.length;j++){
                        row.add(new Player(Double.parseDouble(row_contents[i]), 0));
                    }
                    i++;
                    grid.add(row);
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        else { // user wants to randomly generate a population
            for (int i = 0; i < rows; i++) {
                ArrayList<Player> row = new ArrayList<>();
                for (int j = 0; j < columns; j++) {
                    switch(game){
                        case"UG"->row.add(new Player(
                                ThreadLocalRandom.current().nextDouble(),
                                ThreadLocalRandom.current().nextDouble()));
                        case"DG"->row.add(new Player(
                                ThreadLocalRandom.current().nextDouble(),
                                0.0));
                    }
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
                    switch(game){
                        case"UG","DG"->player.playUG();
//                        case"PD"->player.playPD();
//                        case"IG"->player.playIG();
//                        case"TG"->player.playTG();
                    }
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(EWLE){
                        case"ROC","AD","EAD"->player.EWL(EWLE, ROC,leeway);
                    }
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


            // opportunity to collect intricate data
            if(!experiment_series && runs == 1){

                // record the avg p and p SD of the gen that just ended
                calculateStats();
                System.out.println("avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD));
                writePerGenData(data_filename_prefix + "PerGenData.csv");

                // record interaction data according to the interaction data record rate
                if(gen % interaction_data_record_rate == 0){
                    writeInteractionData(interaction_data_filename_prefix + "Gen" + gen);
                }

                if(gen==data_gen){ // at end of data gen, record lots of extra info
                    System.out.println("Recording detailed data for gen "+data_gen+"...");
                    writeStrategies(data_filename_prefix + "Strategies.csv");
                    writeOwnConnections(data_filename_prefix + "OwnConnections.csv");
                    writeAllConnections(data_filename_prefix + "AllConnections.csv");
                }
            }

            reset(); // reset certain player attributes
            gen++; // move on to the next generation
        }

        calculateStats(); // record the avg p and p SD of the pop at the end of the run
    }












    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){

        // stats to be tracked
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(int i=0;i<runs;i++){
            Alg1 run = new Alg1(); // one run of the algorithm
            run.start(); // start the run
            mean_avg_p_of_experiment += run.avg_p; // tally the mean avg p of the experiment
            avg_p_values_of_experiment[i] = run.avg_p;

            // display the final avg p of the pop of the run
            System.out.println("avg p of run "+i+" of experiment "+experiment_number+": "+run.avg_p);
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment+=Math.pow(avg_p_values_of_experiment[i]-mean_avg_p_of_experiment,2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats to user in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                +" avg p SD="+DF4.format(sd_avg_p_of_experiment));



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
                switch(EWLE){
                    case"ROC","AD","EAD"->{
                        fw.append(",EPR");
                        fw.append(",ROC");
                        fw.append(",leeway");
                    }
                }
                fw.append(",selection");
                fw.append(",evolution");
                if(!mutation_method.equals("")){
                    fw.append(",mutation");
                    fw.append(",u");
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
            switch(EWLE){
                case"ROC","AD","EAD"->{
                    fw.append("," + EPR);
                    fw.append("," + ROC);
                    fw.append("," + leeway);
                }
            }
            switch(selection_method){ // write selection parameters
                case "WRW" -> fw.append(",WRW");
                case "best" -> fw.append(",best");
                case "variable" -> fw.append(",w="+DF4.format(w));
            }
            switch (evolution_method) { // write evolution parameters
                case "copy" -> fw.append(",copy");
                case "approach" -> fw.append(",approach noise=" + DF4.format(approach_noise));
            }
            switch (mutation_method){ // write mutation parameters
                case "global" -> fw.append(",u=" + DF4.format(u));
                case "local" -> {
                    fw.append(",delta="+DF4.format(delta));
                    fw.append(",u=" + DF4.format(u));
                }
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        experiment_number++; // move on to the next experiment in the series
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
            case 5->various_amounts.add(w);
            case 6->various_amounts.add(approach_noise);
            case 7->various_amounts.add(u);
            case 8->various_amounts.add(delta);
        }

        // run experiment series
        for(int i=0;i<num_experiments;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("start of "+varying_parameter+"="+various_amounts.get(i)+":");

            experiment(); // run an experiment
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
                    w+=variation;
                    various_amounts.add(w);
                }
                case 6->{
                    approach_noise+=variation;
                    various_amounts.add(approach_noise);
                }
                case 7->{
                    u+=variation;
                    various_amounts.add(u);
                }
                case 8->{
                    delta+=variation;
                    various_amounts.add(delta);
                }
            }

            // informs user what the varying parameter's value was
            System.out.println("end of "+varying_parameter+"="+various_amounts.get(i)+".");
        }

        // display a summary of the series in the console
        String summary = "";
        try{
            br = new BufferedReader(new FileReader(data_filename_prefix+"Data.csv"));
            String line;
            line = br.readLine(); // purposely ignore the row of headings
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
                    case 7->summary+="u=";
                    case 8->summary+="delta=";
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
     * Calculate the average value of p and the standard deviation of p across the population
     * at the current generation.
     */
    public void calculateStats(){
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
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setNumInteractions(0);
                player.setNumSuccessfulInteractions(0);
                player.setNumSuccessfulDictations(0);
                player.setNumSuccessfulReceptions(0);
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
     * Method for asking the user a binary question to receive a boolean answer.
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
     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
     * and avg p on y-axis. Now also collects standard deviation (SD) data.<br>
     * <br>Steps:<br>
     * - Export the data of a single run to a .csv file<br>
     * - Import the .csv data into an Excel sheet<br>
     * - Separate the data into columns: gen number, avg p and SD for that gen<br>
     * - Create a line chart with the data.<br>
     */
    public void writePerGenData(String filename){
        try{
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


}

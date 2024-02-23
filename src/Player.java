import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * 02/10/2023<br>
 * School of Computer Science<br>
 * University of Galway<br>
 *
 *
 * Player class for instantiating player objects.<br>
 */
public class Player {

    private static int count = 0; // class-wide attribute that helps assign player id
    private final int id; // each player has a unique id
    public int getId(){
        return id;
    }

    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    public static DecimalFormat getDF1() { return DF1; }

    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");
    public static DecimalFormat getDF4(){
        return DF4;
    }

    private static String game;
    public static void setGame(String s){game=s;}





    private double p; // proposal value; real num within [0,1]
    public double getP(){
        return p;
    }
    public void setP(double p){ // recall that p must lie within the range [0,1]
        this.p=p;
        if(this.p>1){
            this.p=1;
        } else if(this.p<0){
            this.p=0;
        }
    }
    private double old_p; // p value held at beginning of gen to be inherited by children
    public void setOldP(double old_p){
        this.old_p=old_p;
    }
    private double q; // acceptance threshold value; real num within [0,1]
    public double getQ(){return q;}
    public void setQ(double q){
        this.q=q;
        if(this.q>1){
            this.q=1;
        } else if(this.q<0){
            this.q=0;
        }
    }
    private double old_q; // q value held at beginning of gen to be inherited by children
    public void setOldQ(double old_q){
        this.old_q=old_q;
    }
    public void setStrategy(double p, double q){
        setP(p);
        setQ(q);
    }


    /**
     * Constructor method for instantiating a Player object.<br>
     * Since this is the DG, make sure to pass 0.0 to q.
     */
    public Player(double p, double q){
        id=count++; // assign this player's id
        this.p=p; // assign p value
        this.q=q; // assign q value
        old_p=p;
        old_q=q;
    }





    private static double prize; // the prize amount being split in an interaction
    public static void setPrize(double d){
        prize=d;
    }

    private ArrayList<Player> neighbourhood; // this player's neighbourhood
    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    private static String neighbourhood_type; // neighbourhood type of this player
    public static String getNeighbourhoodType(){
        return neighbourhood_type;
    }
    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
    }

    /**
     * Stores edge weights belonging to the player.
     * For each neighbour y of player x, e_xy denotes the edge from x to y.
     * w_e_xy denotes the weight of e_xy probability of y dictating to x.
     * w_e_xy lies within the interval [0,1].
     * x controls w_e_xy i.e. the probability of x's neighbours getting to dictate to x.
     */
    private double[] edge_weights;
    public double[] getEdgeWeights(){
        return edge_weights;
    }

    /**
     * Play the UG with neighbours.<br>
     * Game is played with respect to edge weights if EWL parameter is set.<br>
     * UG with EWL works as follows:
     * For each neighbour y in x's neighbourhood, x proposes/dictates to them if the weight of y's
     * edge to x, which represents y's likelihood of receiving from x, is greater than a
     * randomly generated double between 0 and 1.<br>
     * Note: When the EW is 1.0, the game always occurs. Since EWs initialise at 1.0,
     * if EWL is not occurring, games should and do always take place.
     */
    public void playUG(){
        for(int i=0;i<neighbourhood.size();i++){ // loop through x's neighbourhood
            Player neighbour = neighbourhood.get(i);
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // loop through y's neighbourhood
                Player neighbours_neighbour = neighbour.neighbourhood.get(j);
                if(neighbours_neighbour.id == id){ // find EW of y associated with x
                    double edge_weight = neighbour.edge_weights[j];
                    double random_double = ThreadLocalRandom.current().nextDouble();
                    if(edge_weight > random_double){ // x has EW% probability of success
                        if(p >= neighbour.q){
                            updateStats(prize - (prize * p), true);
                            neighbour.updateStats(prize * p, false);
                        }
//                        else {
//                            System.out.println("proposal rejected!");
//                        }
                    }
                    else{
                        updateStats(0,true);
                        neighbour.updateStats(0,false);

                    }
                    break;
                }
            }
        }
    }





    /**
     * NI: how many interactions the player had (within some timeframe e.g. within a gen).<br>
     * NSI: how many successful interactions the player had. A successful interaction is defined as an
     * interaction where payoff was received.<br>
     * NSD: how many successful interaction the player had as dictator.<br>
     * NSR: how many successful interaction the player had as recipient.<br>
     */
    private int num_interactions = 0;               // NI
    private int num_successful_interactions = 0;    // NSI
    private int num_successful_dictations = 0;      // NSD
    private int num_successful_receptions = 0;      // NSR
    public void setNumInteractions(int i){
        num_interactions=i;
    }
    public int getNumSuccessfulInteractions(){
        return num_successful_interactions;
    }
    public void setNumSuccessfulInteractions(int i){
        num_successful_interactions=i;
    }
    public void setNumSuccessfulDictations(int i){
        num_successful_dictations=i;
    }
    public void setNumSuccessfulReceptions(int i){
        num_successful_receptions=i;
    }
    private double score; // total accumulated payoff; fitness
    public void setScore(double score){
        this.score=score;
    }
    private double average_score; // average score of this player this gen
    private static String ASD = ""; // average score denominator
    public static String getASD(){return ASD;}
    public static void setASD(String s){ASD=s;}
    private static String PPM; // player performance metric
    public static String getPPM(){return PPM;}
    public static void setPPM(String s){PPM=s;}


    /**
     * Update the status of the player after having played, including score and average score.
     */
    public void updateStats(double payoff, boolean dictator){
        score+=payoff;
        num_interactions++;
        if(payoff > 0){ // check if the interaction was successful i.e. if any payoff was received.
            num_successful_interactions++;
            if(dictator){
                num_successful_dictations++;
            } else{
                num_successful_receptions++;
            }
        }

        switch(ASD){
            case"NI"->average_score=score/num_interactions;
            case"NSI"->average_score=score/num_successful_interactions;
        }
    }






    /**
     * Assigns neighbours to the player in a 2D square lattice grid with respect
     * to the von Neumann or the Moore neighbourhood type.
     * @param grid is the grid/population of players.
     * @param row_position is the x coordinate of the player.
     * @param column_position is the y coordinate of the player.
     */
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid, int row_position, int column_position){
        neighbourhood = new ArrayList<>();
        int a=row_position;
        int b=column_position;
        int c=grid.size();
        int d=grid.get(0).size();
        int up=((a-1)%c+c)%c; // go up one node (on the square grid)
        int down=((a+1)%c+c)%c; // down
        int left=((b-1)%d+d)%d; // left
        int right=((b+1)%d+d)%d; // right
        neighbourhood.add(grid.get(up).get((b%d+d)%d));
        neighbourhood.add(grid.get(down).get((b%d+d)%d));
        neighbourhood.add(grid.get((a%c+c)%c).get(left));
        neighbourhood.add(grid.get((a%c+c)%c).get(right));
        if(neighbourhood_type.equals("M")){
            neighbourhood.add(grid.get(up).get(left)); // up-left
            neighbourhood.add(grid.get(up).get(right)); // up-right
            neighbourhood.add(grid.get(down).get(left)); // down-left
            neighbourhood.add(grid.get(down).get(right)); // down-right
        }
    }



    /**
     * Initialise the player's edge weights.
     */
    public void initialiseEdgeWeights() {
        edge_weights = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            edge_weights[i] = 1.0; // initialise edge weight at 1.0
        }
    }


    /**
     * Edge weight learning method.
     * @param EWAE is the edge weight learning equation used to calculate the amount of edge weight adjustment.
     * @param ROC is the rate of change of edge weights
     * @param leeway is the leeway allowed by the player to their neighbours
     */
    public void EWL(String EWAE, double ROC, double leeway){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            if(neighbour.p + leeway > p){ // EWL condition with leeway
                switch(EWAE){
                    case"ROC"->edge_weights[i]+=ROC; // rate of change
                    case"AD"->edge_weights[i]+=Math.abs(neighbour.p-p); // absolute difference
                    case"EAD"->edge_weights[i]+=Math.exp(Math.abs(neighbour.p-p)); // exponential absolute difference
                }
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p + leeway < p){
                switch(EWAE){
                    case"ROC"->edge_weights[i]-=ROC; // rate of change
                    case"AD"->edge_weights[i]-=Math.abs(neighbour.p-p); // absolute difference
                    case"EAD"->edge_weights[i]-=Math.exp(Math.abs(neighbour.p-p)); // exponential absolute difference
                }
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
                }
            }
        }
    }






    @Override
    public String toString(){ // document details relating to the player

        // document key player attributes
        String player_desc = "";
        player_desc += "id="+id;
        switch(game){
            case"UG","DG"->{
                player_desc += " p=" + DF4.format(p) + " ("+DF4.format(old_p) + ")"; // document p and old p
                switch(game){
                    case"UG"-> {
                        player_desc += " q=" + DF4.format(q) + " (" + DF4.format(old_q) + ")"; // document q and old q
                    }
                }
            }
        }
        player_desc+=" score="+DF4.format(score); // score
        switch(PPM){
            case"avg score"->player_desc+="("+DF4.format(average_score)+")"; // avg score
        }

        // document neighbourhood and EWs
        player_desc += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            player_desc += neighbour.id + " (" + DF4.format(edge_weights[i]) + ")";
            if((i+1) < neighbourhood.size()){ // check if there are any neighbours left to document
                player_desc +=", ";
            }
        }
        player_desc +="]";

        // document interaction stats
        player_desc += " NI="+ num_interactions;
        player_desc += " NSI="+ num_successful_interactions;
        player_desc += " NSD="+ num_successful_dictations;
        player_desc += " NSR="+ num_successful_receptions;

        return player_desc;
    }


    /**
     * Returns the index of the player x in the neighbourhood of a neighbour.
     */
    public int findXInNeighboursNeighbourhood(Player neighbour){
        int index = 0; // by default, assign index to 0.
        for (int l = 0; l < neighbour.neighbourhood.size(); l++) {
            Player y = neighbour.neighbourhood.get(l);
            if (id == y.getId()) {
                index = l;
            }
        }

        return index;
    }


    /**
     * Returns the sum of own connections of the player
      */
    public double calculateOwnConnections(){
        double sum = 0.0;
        for(int i=0;i<edge_weights.length;i++){
            sum += edge_weights[i];
        }

        return sum;
    }


    /**
     * Returns the sum of all associated connections of the player.<br>
     * How to find weights of neighbour edges associated with player x:<br>
     * - Loop through x's neighbourhood;<br>
     * - For each neighbour y of x, find x's index within y's neighbourhood;<br>
     * - Use the index to find the weight of the edge from y to x;<br>
     * - Add the weight to the sum.<br>
     */
    public double calculateAllConnections(){
        double sum = calculateOwnConnections();
        for(int k=0;k<neighbourhood.size();k++) {
            Player y = neighbourhood.get(k);
            sum += y.edge_weights[findXInNeighboursNeighbourhood(y)];
        }

        return sum;
    }









    /**
     * Evolution method where child wholly copies parent's strategy.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     */
    public void copyEvolution(Player parent){
        // i dont think the if is necessary
//        if(parent.id != id){
//            setP(parent.old_p);
//        }

//        setP(parent.old_p);
        setStrategy(parent.old_p, parent.old_q);
    }



    /**
     * Evolution method where child's strategy gets closer to, i.e. approaches, parent's strategy.
     * The amount by which the child's strategy approaches the parent's is a randomly generated
     * double between 0.0 and the approach limit.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The greater the noise, the greater the approach is.<br>
     */
    public void approachEvolution(Player parent, double approach_noise){
        if(parent.id != id){
            double approach = ThreadLocalRandom.current().nextDouble(approach_noise);
            if(parent.old_p < p){ // if parent's p is lower, reduce p; else, increase p
                approach *= -1;
            }
            double new_p = p + approach;

            if(parent.old_q < q){
                approach *= -1;
            }
            double new_q = q + approach;

//            setP(new_p);
            setStrategy(new_p, new_q);
        }
    }





    /**
     * Selection method where the child compares their score with their neighbours'. The
     * greater the difference in score, the neighbour's probability of being selected as child's
     * parent is exponentially affected. If a parent is not selected, the child is selected as parent
     * by default.<br>
     */
    public Player weightedRouletteWheelSelection(){
        Player parent = this; // by default, the child is the parent
        double[] parent_scores = new double[neighbourhood.size()];
        double total_parent_score = 0.0; // track the sum of the "parent scores" of the neighbourhood
        for(int i=0;i<neighbourhood.size();i++){ // calculate the parent scores of the neighbourhood
            switch(PPM){
                case"score"-> parent_scores[i]=Math.exp(neighbourhood.get(i).score - score);
                case"avg score"-> parent_scores[i]=Math.exp(neighbourhood.get(i).average_score-average_score);
            }
            total_parent_score += parent_scores[i];
        }
        total_parent_score += 1.0; // effectively this gives the child a slot on their own roulette
        double parent_score_tally = 0.0; // helps us count through the parent scores up to the total

        // the first parent score to be greater than this double is the winner.
        // if no neighbour's parent score wins, the child wins.
        double random_double = ThreadLocalRandom.current().nextDouble();
        for(int j=0;j<neighbourhood.size();j++){
            parent_score_tally += parent_scores[j];
            double percentile = parent_score_tally / total_parent_score;
            if (random_double < percentile) {
                parent = neighbourhood.get(j);
                break;
            }
        }

        return parent;
    }



    /**
     * Selection method where child selects the highest scoring neighbour this gen as parent if
     * that neighbour scored higher than the child.<br>
     * Should the score comparison be between scores or avg scores?<br>
     */
    public Player bestSelection(){
        Player parent;
        int best_index = 0;
        for(int i=1;i<neighbourhood.size();i++){ // find the highest scoring neighbour
            Player neighbour = neighbourhood.get(i);
            Player best = neighbourhood.get(best_index);
            switch(PPM){
                case"score"->{
                    if(neighbour.score > best.score){
                        best_index=i;
                    }
                }
                case"avg score"->{
                    if(neighbour.average_score > best.average_score){
                        best_index=i;
                    }
                }
            }
        }

        // did the highest scoring neighbour score higher than child? if not, select child as parent
        parent = neighbourhood.get(best_index);
        switch(PPM){
            case"score"->{
                if(parent.score <= score){
                    parent = this;
                }
            }
            case"avg score"->{
                if(parent.average_score <= average_score){
                    parent = this;
                }
            }
        }

        return parent;
    }



    /**
     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
     * Calculate effective payoffs of neighbours. Select highest as parent.<br>
     * w represents the intensity of selection.
     */
    public Player variableSelection(double w){
        Player parent;
        double[] effective_payoffs = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            switch(PPM){
                case"score"->effective_payoffs[i] = Math.exp(w * neighbour.score);
                case"avg score"->effective_payoffs[i] = Math.exp(w * neighbour.average_score);
            }
        }
        double best_effective_payoff = effective_payoffs[0];
        int index = 0;
        for(int j=1;j<effective_payoffs.length;j++){
            if(effective_payoffs[j] > best_effective_payoff){
                index = j;
            }
        }
        parent = neighbourhood.get(index);

        return parent;
    }











    /**
     * Mutation rate parameter determines the probability for mutation to occur.<br>
     * Returns a boolean indicating whether mutation should occur. If true is returned, mutation will
     * occur. If false is returned, mutation will not occur.<br>
     */
    public boolean mutationCheck(double mutation_rate){
        double random_double = ThreadLocalRandom.current().nextDouble();

        return random_double < mutation_rate;
    }



    /**
     * Mutation method where the player's strategy is randomly generated. Mutation is independent.
     * Inspired by Akdeniz and van Veelen (2023).
     */
    public void globalMutation(){
        setStrategy(ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble());
    }



    /**
     * Mutation method where attributes are modified by a random double within an interval
     * determined by delta. The mutation is independent as the mutation applied to
     * one attribute is independent of the mutation of another.
     * Inspired by Akdeniz and van Veelen (2023).
     */
    public void localMutation(double delta){
        double new_p = ThreadLocalRandom.current().nextDouble(p - delta, p + delta);
        double new_q = ThreadLocalRandom.current().nextDouble(q - delta, q + delta);
        setStrategy(new_p,new_q);
    }
}

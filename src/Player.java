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
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    public void setOldP(double old_p){
        this.old_p=old_p;
    }
    private double q; // acceptance threshold value; real num within [0,1]

    /**
     * Constructor method for instantiating a Player object.<br>
     * Since this is the DG, make sure to pass 0.0 to q.
     */
    public Player(double p, double q){
        id=count++; // assign this player's id
        this.p=p; // assign p value
        this.q=q; // assign q value
        old_p=p;
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
     * Play the UG with respect to space and edge weights.<br>
     * For each neighbour y in x's neighbourhood, x proposes/dictates to them if the weight of y's
     * edge to x, which represents y's likelihood of receiving from x, is greater than a
     * randomly generated double between 0 and 1.<br>
     */
    public void play(){
        for(int i=0;i<neighbourhood.size();i++){ // loop through x's neighbourhood
            Player neighbour = neighbourhood.get(i);
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // loop through y's neighbourhood
                Player neighbours_neighbour = neighbour.neighbourhood.get(j);
                if(neighbours_neighbour.id == id){ // find EW of y associated with x
                    double edge_weight = neighbour.edge_weights[j];
                    double random_double = ThreadLocalRandom.current().nextDouble();
                    if(edge_weight > random_double){ // x has EW% probability of success
                        if(p >= neighbour.q){ // accept offer
                            updateStats(prize * (1 - p), true);
                            neighbour.updateStats(prize * p, false);
                        } else { // reject offer
                            updateStats(0, true);
                            neighbour.updateStats(0, false);
                        }
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
    private double score; // amount of reward player has received from playing; i.e. this player's fitness
    public void setScore(double score){
        this.score=score;
    }
    private double average_score; // average score of this player this gen

    /**
     * Update the status of the player after having played, including score and average score.<br>
     */
    public void updateStats(double payoff, boolean dictator){
        score+=payoff;
        num_interactions++;
        if(payoff != 0){ // check if the interaction was successful i.e. if any payoff was received.
            num_successful_interactions++;
            if(dictator){
                num_successful_dictations++;
            } else{
                num_successful_receptions++;
            }
        }

        average_score = score / num_successful_interactions; // this has been the default setting.
//        average_score = score / num_interactions; // alternative setting

    }





    /**
     * Method for assigning a position of the player x on a 2D space and finding x's neighbours in
     * the space.<br>
     * Possible neighbourhood_type values: VN, M.<br>
     * Neighbourhood order (with VN): up, down, left, right.<br>
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
     * Initialise edge_weights with respect to neighbourhood size.
     */
    public void initialiseEdgeWeights() {
        edge_weights = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            edge_weights[i] = 1.0; // initialise edge weight at 1.0
        }
    }




    
    
    
    
    
    
    



    /**
     * Method that allows players to perform a form of edge weight learning.
     * Here, a player x's edge weights is supposed to represent x's neighbours' relationship towards x.
     * If a neighbour y has a higher value of p than x, x raises the weight of their edge to y.
     * If y has a lower value of p than x, x reduces the weight of their edge to y.
     * The amount by which an edge is modified is determined by the ROC parameter.
     */
    public void edgeWeightLearning1(double ROC){
        for (int i = 0; i < neighbourhood.size(); i++) {
            Player neighbour = neighbourhood.get(i);
            if (neighbour.p > p) { // if neighbour is more generous than you, increase EW
                edge_weights[i] += ROC;
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p < p){ // if neighbour is less generous, decrease EW
                edge_weights[i] -= ROC;
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
                }
            }
        }
    }




    /**
     * EWL method where the amount of EW modification is affected by the difference
     * between the player's p and the neighbour's p.
     */
    public void edgeWeightLearning2(){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double diff = Math.abs(neighbour.p - p);
            if(neighbour.p > p){
                edge_weights[i] += diff;
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p < p){
                edge_weights[i] -= diff;
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
                }
            }
        }
    }





    /**
     * EWL method where the amount of EW modification is exponentially affected by the difference
     * between the player's p and the neighbour's p.
     */
    public void edgeWeightLearning3(){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double diff = Math.abs(neighbour.p - p);
            double exp_diff = Math.exp(diff);
            if(neighbour.p > p){
                edge_weights[i] += exp_diff;
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p < p){
                edge_weights[i] -= exp_diff;
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
                }
            }
        }
    }







    /**
     *  Identifies if the player and the given neighbour have a fair relationship under the given
     *  fairness_interval.<br>
     *  A fair relationship here is loosely defined as one where the p values of the two players
     *  are within the fairness interval from each other. The "fairness interval" indicates how
     *  much leeway is being given.<br>
     *  Nodes x and y have a fair relationship if p_y lies within [p_x - fairness_interval, p_y +
     *  fairness_interval].<br>
     *  E.g. p_1=0.4, p_2=0.37 and fairness_interval=0.05 is a fair relationship because p_2 lies within
     *  the interval [p_1 - fairness_interval, p_1 + fairness_interval] = [0.4 - 0.05, 0.4 + 0.05] =
     *  [0.35, 0.45] so here node 1 and 2 have a fair relationship.<br>
     *  E.g. With p_3=0.2, p_4=0.05 and fairness_interval=0.1, node 3 and 4 do not have a fair
     *  relationship.<br>
     */
    private static double fairness_interval; // the leeway given when determining fair relationships
    public static void setFairnessInterval(double d){
        fairness_interval=d;
    }
    public boolean identifyFairRelationship(Player neighbour){
        double p_y = neighbour.getP();
        double lower_bound = p - fairness_interval;
        double upper_bound = p + fairness_interval;

        return lower_bound <= p_y && upper_bound >= p_y;
    }






    /**
     * Identifies the number of fair relationships the player has with their neighbours with
     * respect to the given fairness interval.
     */
    public int getNumFairRelationships(){
        int num_fair_relationships = 0;
        for(Player neighbour: neighbourhood){
            if(identifyFairRelationship(neighbour)){
                num_fair_relationships++;
            }
        }
        return num_fair_relationships;
    }





    @Override
    public String toString(){ // document details relating to the player
        String description = "";
        description += "id="+id;
        description += " p=" + DF4.format(p) + " ("+DF4.format(old_p) + ")"; // document p and old p
        description += " score="+DF4.format(score)+" ("+DF4.format(average_score)+")"; // score and avg score

        // document neighbourhood and EWs
        description += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            description += neighbour.id + " (" + DF4.format(edge_weights[i]) + ")";
            if((i+1) < neighbourhood.size()){ // check if there are any neighbours left to document
                description +=", ";
            }
        }
        description +="]";

        description += " NI="+ num_interactions;
        description += " NSI="+ num_successful_interactions;
        description += " NSD="+ num_successful_dictations;
        description += " NSR="+ num_successful_receptions;
        return description;
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
        if(parent.id != id){
            setP(parent.old_p);
        }
    }



    /**
     * Evolution method where evolver imitates parent's strategy with respect to noise i.e.
     * new strategy lies within interval centred around parent's strategy.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The lower the noise, the more accurate the imitations will generally be.<br>
     * If noise is set to 0, the result of the evolution is identical to copy evolution.<br>
     */
    private static double imitation_noise; // the amount of noise that may affect imitation evolution
    public static double getImitationNoise(){
        return imitation_noise;
    }
    public static void setImitationNoise(double d){
        imitation_noise = d;
    }
    public void imitationEvolution(Player parent){
        if(parent.id != id){
            double new_p = ThreadLocalRandom.current().nextDouble(
                    parent.old_p - imitation_noise, parent.old_p + imitation_noise);
            setP(new_p);
        }
    }


    public double getAverageScore(){
        return average_score;
    }



    /**
     * Evolution method where child's strategy gets closer to, i.e. approaches, parent's strategy.
     * The amount by which the child's strategy approaches the parent's is a randomly generated
     * double between 0.0 and the approach limit.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The greater the noise, the greater the approach is.<br>
     */
    private static double approach_noise; // states by how much a player can change via approach evolution
    public static double getApproachNoise(){
        return approach_noise;
    }
    public static void setApproachNoise(double d){
        approach_noise=d;
    }
    public void approachEvolution(Player parent){
        if(parent.id != id){
            double approach = ThreadLocalRandom.current().nextDouble(approach_noise);
            if(parent.old_p < p){ // if parent's p is lower, reduce p; else, increase p
                approach *= -1;
            }
            double new_p = p + approach;
            setP(new_p);
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
            double neighbour_average_score = neighbourhood.get(i).average_score;
            parent_scores[i] = Math.exp(neighbour_average_score - average_score);
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
            if(neighbour.average_score > best.average_score){
                best_index = i;
            }
        }

        // did the highest scoring neighbour score higher than child? if not, select child as parent
        parent = neighbourhood.get(best_index);
        if(parent.average_score <= average_score){
            parent = this;
        }

        return parent;
    }




    /**
     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
     * Calculate effective payoffs of neighbours. Select highest as parent.<br>
     */
    private static double w; // intensity of selection
    public static double getW(){
        return w;
    }
    public static void setW(double d){
        w=d;
    }
    public Player variableSelection(){
        Player parent;
        double[] effective_payoffs = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            effective_payoffs[i] = Math.exp(w * neighbour.average_score);
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
    private static double mutation_rate;
    public static double getMutationRate(){
        return mutation_rate;
    }
    public static void setMutationRate(double d){
        mutation_rate=d;
    }
    public boolean mutationCheck(){
        double random_double = ThreadLocalRandom.current().nextDouble();
        if(random_double < mutation_rate){ // the greater the mutation rate, the more likely mutation occurs
            return true; // mutation will occur
        } else {
            return false; // mutation will not occur
        }
    }



    /**
     * Mutation method where the player's p is randomly generated.
     */
    public void newMutation(){
        setP(ThreadLocalRandom.current().nextDouble());
    }
}

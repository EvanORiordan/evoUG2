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
 * Class for instantiating player objects with player information.<br>
 */
public class Player {

    // ===================================== Attributes =====================================
    private static int count = 0; // class-wide attribute that helps assign player ID
    private final int ID; // each player has a unique ID i.e. position
    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");
    private static String game; // indicates what game player is playing
    private double p; // proposal value residing within [0,1]
    private double old_p; // p value held at beginning of gen to be inherited by children
    private double q; // acceptance threshold value residing within [0,1]
    private double old_q; // q value held at beginning of gen to be inherited by children
//    private ArrayList<Player> neighbourhood = new ArrayList<>(); // neighbours of player; for each neighbour, there exists an edge between player and neighbour.
    private ArrayList<Player> neighbourhood; // neighbours of player; for each neighbour, there exists an edge between player and neighbour.
    private int[] neighbour_IDs; // array of the IDs of the player's neighbour
    private double[] edge_weights; // edge weights in [0,1] connecting player to neighbours
    private int NI = 0;  // num interactions (NI) player had
    private int NSI = 0; // num successful interactions (NSI) player had i.e. num interactions earned payoff
    private int[] NSI_per_neighbour;
    private int NSP = 0; // num successful proposals (NSP)
    private int NSR = 0; // num successful receptions (NSR)
    private double score; // score of player ($\Pi$)
    private double avg_score; // average score of player ($\overline{\Pi}$)
    private double mean_self_edge_weight; // mean of edge weights from player to its neighbours
    private double mean_neighbour_edge_weight; // mean of edge weights directed at player
    private String[] strategies_PD = {"C","D","A","TFT"};
    private String strategy_PD = "";
    private double local_leeway; // inherent leeway of the player
    private ArrayList<Player> margolus_neighbourhood1 = new ArrayList<>();
    private ArrayList<Player> margolus_neighbourhood2 = new ArrayList<>();
    private double[] margolus_edge_weights1;
    private double[] margolus_edge_weights2;
    private double x; // x position in space
    private double y; // y position in space




    // ===================================== Unique Functions =====================================
    /**
     * Constructor method for instantiating a Player object.<br>
     * Since this is the DG, make sure to pass 0 to q.<br>
     * @param p is the proposal value of the player
     * @param q is the acceptance threshold value of the player
     * @param leeway2 is a factor used to calculate the player's local leeway
     */
//    public Player(double p, double q, double leeway2){
//        ID=count++; // assign this player's ID
//        setP(p); // assign p value
//        setQ(q); // assign q value
//        old_p=p;
//        old_q=q;
//
//        // assign local_leeway value
//        if(leeway2 == 0){
//            local_leeway = 0;
//        } else {
//            local_leeway = ThreadLocalRandom.current().nextDouble(-leeway2,leeway2);
//        }
//    }



    public Player(double x, double y, double p, double q, double leeway2){
        ID=count++;
        this.x=x;
        this.y=y;
        setP(p);
        setQ(q);
        old_p=p;
        old_q=q;
        if(leeway2==0)
            local_leeway=0;
        else
            local_leeway=ThreadLocalRandom.current().nextDouble(-leeway2,leeway2);
    }






    /**
     * Augmented setter.
     * p must reside within [0,1].
      */
    public void setP(double p){
        this.p=p;
        if(this.p>1){
            this.p=1;
        } else if(this.p<0){
            this.p=0;
        }
    }



    /**
     * Augmented setter.
     * q must reside within [0,1].
     */
    public void setQ(double q){
        this.q=q;
        if(this.q>1){
            this.q=1;
        } else if(this.q<0){
            this.q=0;
        }
    }



    @Override
    public String toString(){ // document details relating to the player

        // document key player attributes
        String player_desc = "";
        player_desc += "ID="+ID;
        player_desc += " pos=("+x+","+y+")";
        switch(game){
            case"UG","DG"->{
                player_desc += " p=" + DF4.format(p) // p
                        + " ("+DF4.format(old_p) + ")"; // old p
                switch(game){
                    case"UG"-> {
                        player_desc += " q=" + DF4.format(q) // q
                                + " (" + DF4.format(old_q) + ")"; // old q
                    }
                }
            }
        }
        player_desc+=" score="+DF4.format(score); // score
        player_desc+=" ("+DF4.format(avg_score)+")"; // avg score


        // document EW and NSI per neighbour
        player_desc += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            player_desc += "("
                    + neighbour.ID + ", " // neighbour ID
                    + DF2.format(edge_weights[i]) + ", " // EW with neighbour
                    + NSI_per_neighbour[i] + ")"; // NSI with neighbour
            if((i+1) < neighbourhood.size()){ // check if there are any neighbours left to document
                player_desc +=", ";
            }
        }
        player_desc +="]";


        // document interaction stats
        player_desc += " NI="+ NI;
        player_desc += " NSI="+ NSI;
        player_desc += " NSP="+ NSP;
        player_desc += " NSR="+ NSR;

        return player_desc;
    }



    // ===================================== Generic Functions =====================================
    public int getID(){
        return ID;
    }
    public static DecimalFormat getDF1() { return DF1; }
    public static DecimalFormat getDF2(){return DF2;}
    public static DecimalFormat getDF4(){
        return DF4;
    }
    public static void setGame(String s){game=s;}
    public double getP(){
        return p;
    }
    public double getOldP(){return old_p;}
    public void setOldP(double old_p){
        this.old_p=old_p;
    }
    public double getQ(){return q;}
    public double getOldQ(){return old_q;}
    public void setOldQ(double old_q){
        this.old_q=old_q;
    }
    public double getLocalLeeway(){return local_leeway;}
    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }
    public void setNeighbourhood(ArrayList <Player> x){neighbourhood=x;}
    public void setNeighbourIDs(int[] arr){
        neighbour_IDs=arr;
    }
    public double[] getEdgeWeights(){
        return edge_weights;
    }
    public void setEdgeWeights(double[] d){edge_weights=d;}
    public int getNI(){return NI;}
    public void setNI(int i){
        NI=i;
    }
    public int getNSI(){return NSI;}
    public void setNSI(int i){
        NSI=i;
    }
    public int[] getNSIPerNeighbour(){return NSI_per_neighbour;}
    public void setNSIPerNeighbour(int[] arr){NSI_per_neighbour=arr;}
    public int getNSP(){return NSP;}
    public void setNSP(int i){
        NSP=i;
    }
    public int getNSR(){return NSR;}
    public void setNSR(int i){
        NSR=i;
    }
    public double getScore(){return score;}
    public void setScore(double score){
        this.score=score;
    }
    public double getAvgScore(){return avg_score;}
    public void setAvgScore(double d){avg_score=d;}
    public double getMeanSelfEdgeWeight(){return mean_self_edge_weight;}
    public void setMeanSelfEdgeWeight(double d){mean_self_edge_weight=d;}
    public double getMeanNeighbourEdgeWeight(){return mean_neighbour_edge_weight;}
    public void setMeanNeighbourEdgeWeight(double d){mean_neighbour_edge_weight=d;}
    public String getStrategyPD(){return strategy_PD;}
    public ArrayList<Player>getMargolus_neighbourhood1(){return margolus_neighbourhood1;}
    public ArrayList<Player>getMargolus_neighbourhood2(){return margolus_neighbourhood2;}
    public void setMargolus_neighbourhood1(ArrayList<Player> x){margolus_neighbourhood1=x;}
    public void setMargolus_neighbourhood2(ArrayList<Player> x){margolus_neighbourhood2=x;}
    public double getY(){return y;}
    public void setY(double d){y=d;}
    public double getX(){return x;}
    public void setX(double d){x=d;}

}

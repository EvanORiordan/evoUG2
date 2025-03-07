import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * School of Computer Science<br>
 * University of Galway<br>
 */
public class Player {

    // ===================================== Attributes =====================================
//    private static int count = 0; // class-wide attribute that helps assign player ID
    private static int count; // class-wide attribute that helps assign player ID
    private final int ID; // each player has a unique ID i.e. position
    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");
    private static String game; // indicates what game player is playing
    private double p; // proposal value residing within [0,1]
    private double oldP; // p value held at beginning of gen to be inherited by children
    private double q; // acceptance threshold value residing within [0,1]
    private double oldQ; // q value held at beginning of gen to be inherited by children
//    private ArrayList<Player> neighbourhood = new ArrayList<>(); // neighbours of player; for each neighbour, there exists an edge between player and neighbour.
    private ArrayList<Player> neighbourhood; // neighbours of player; for each neighbour, there exists an edge between player and neighbour.
//    private int[] neighbour_IDs; // array of the IDs of the player's neighbour; mainly useful for quickly identifying players while debugging
//    private double[] edgeWeights; // edge weights in [0,1] connecting player to neighbours
    private ArrayList <Double> edgeWeights; // using Double arraylist rather than double array allows us to use ArrayList.add() to add things to the collection without an index.
//    private int NIP = 0; // num of interactions possible (NIP) that the player could have had.
    private int MNI = 0; // maximum number of interactions (MNI) that the player could have had.
//    private int NSI = 0; // num of successful interactions (NSI) player had i.e. num interactions earned payoff.
//    private int[] NSI_per_neighbour;
    private int NSP = 0; // num successful proposals (NSP)
    private int NSR = 0; // num successful receptions (NSR)
//    private double score; // score of player ($\Pi$)
    private double pi; // score of player ($\Pi$)
    private double mean_self_edge_weight; // mean of edge weights from player to its neighbours
    private double mean_neighbour_edge_weight; // mean of edge weights directed at player
    private String[] strategies_PD = {"C","D","A","TFT"};
    private String strategy_PD = "";
    private ArrayList<Player> margolus_neighbourhood1 = new ArrayList<>();
    private ArrayList<Player> margolus_neighbourhood2 = new ArrayList<>();
    private double[] margolus_edgeWeights1;
    private double[] margolus_edgeWeights2;
    private double x; // x position in space
    private double y; // y position in space
    private double u; // utility
    private int degree;




    /**
     * Constructor method for instantiating a UG/DG Player object.
     * @param p is the proposal value of the player
     * @param q is the acceptance threshold value of the player
     */
    public Player(double x, double y, double p, double q){
        ID=count++;
        this.x=x;
        this.y=y;
        this.p=p;
        this.q=q;
        oldP=p;
        oldQ=q;
    }



    // PD player constructor method.
    // public Player(double x, double y, String strategy){}



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
//
//
//
//    /**
//     * Augmented setter.
//     * q must reside within [0,1].
//     */
//    public void setQ(double q){
//        this.q=q;
//        if(this.q>1){
//            this.q=1;
//        } else if(this.q<0){
//            this.q=0;
//        }
//    }



    @Override
    public String toString(){ // document details relating to the player
        String player_desc = "";
        player_desc += "ID="+ID;
//        player_desc += " pos=("+x+","+y+")";
        switch(game){
            case"UG","DG"->{
                player_desc += " p=" + DF4.format(p) // p
                        + " ("+DF4.format(oldP) + ")"; // old p
                switch(game){
                    case"UG"-> {
                        player_desc += " q=" + DF4.format(q) // q
                                + " (" + DF4.format(oldQ) + ")"; // old q
                    }
                }
            }
        }
        player_desc+=" pi="+DF4.format(pi); // score
        player_desc += " u=" + DF4.format(u); // utility

        player_desc += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            player_desc += neighbourhood.get(i).ID;
            if((i + 1) < neighbourhood.size()){
                player_desc += ", ";
            }
        }
        player_desc += "]";

        player_desc += " weights=[";
        for(int i=0;i<edgeWeights.size();i++){
            player_desc += DF2.format(edgeWeights.get(i));
            if((i + 1) < edgeWeights.size()){
                player_desc += ", ";
            }
        }
        player_desc += "]";

        // interaction stats
//        player_desc += " NI="+ NI;
//        player_desc += " MNI=" + MNI;
//        player_desc += " NSI="+ NSI;
//        player_desc += " NSP="+ NSP;
//        player_desc += " NSR="+ NSR;

        return player_desc;
    }


    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(!(obj instanceof Player)){
            return false;
        }

        Player obj2 = (Player) obj;

//        return Integer.compare(ID, obj2.ID) == 0;
        return ID == obj2.ID;
    }


    // degree is equal to number of edges i.e. number of neighbours i.e. size of neighbourhood
    public void calculateDegree(){
        degree = neighbourhood.size();
    }



    // generic functions
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
//    public void setP(double d){p=d;}
    public double getOldP(){return oldP;}
    public void setOldP(double oldP){
        this.oldP=oldP;
    }
    public double getQ(){return q;}
    public void setQ(double d){q=d;}
    public double getOldQ(){return oldQ;}
    public void setOldQ(double oldQ){
        this.oldQ=oldQ;
    }
    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }
    public void setNeighbourhood(ArrayList <Player> x){neighbourhood=x;}
//    public int[] getNeighbourIDs(){return neighbour_IDs;}
//    public void setNeighbourIDs(int[] arr){
//        neighbour_IDs=arr;
//    }
//    public double[] getEdgeWeights(){
//        return edgeWeights;
//    }
//    public void setEdgeWeights(double[] d){edgeWeights=d;}
    public ArrayList <Double> getEdgeWeights(){return edgeWeights;}
    public void setEdgeWeights(ArrayList <Double> x){edgeWeights=x;}
    public int getMNI(){return MNI;}
    public void setMNI(int i){
        MNI=i;
    }
//    public int getNSI(){return NSI;}
//    public void setNSI(int i){
//        NSI=i;
//    }
//    public int[] getNSIPerNeighbour(){return NSI_per_neighbour;}
//    public void setNSIPerNeighbour(int[] arr){NSI_per_neighbour=arr;}
    public int getNSP(){return NSP;}
    public void setNSP(int i){
        NSP=i;
    }
    public int getNSR(){return NSR;}
    public void setNSR(int i){
        NSR=i;
    }
    public double getPi(){return pi;}
    public void setPi(double pi){
        this.pi=pi;
    }
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
    public static int getCount(){return count;}
    public static void setCount(int i){count=i;}
    public double getU(){return u;}
    public void setU(double d){u=d;}
    public int getDegree(){return degree;}
    public void setDegree(int i){degree=i;}
}

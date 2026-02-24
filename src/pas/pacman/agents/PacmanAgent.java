package src.pas.pacman.agents;


// SYSTEM IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.game.entity.Entity;
import edu.bu.pas.pacman.agents.SearchAgent;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.routing.BoardRouter;
import edu.bu.pas.pacman.routing.PelletRouter;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;
import edu.bu.pas.pacman.utils.DistanceMetric;

import java.util.Random;
import java.util.Set;
import java.util.Stack;


// JAVA PROJECT IMPORTS
import src.pas.pacman.routing.ThriftyBoardRouter;  // responsible for how to get somewhere
import src.pas.pacman.routing.ThriftyPelletRouter; // responsible for pellet order


public class PacmanAgent
    extends SearchAgent
{

    private final Random random;
    private BoardRouter  boardRouter;
    private PelletRouter pelletRouter;

    public PacmanAgent(int myUnitId,
                       int pacmanId,
                       int ghostChaseRadius)
    {
        super(myUnitId, pacmanId, ghostChaseRadius);
        this.random = new Random();

        this.boardRouter = new ThriftyBoardRouter(myUnitId, pacmanId, ghostChaseRadius);
        this.pelletRouter = new ThriftyPelletRouter(myUnitId, pacmanId, ghostChaseRadius);
    }

    public final Random getRandom() { return this.random; }
    public final BoardRouter getBoardRouter() { return this.boardRouter; }
    public final PelletRouter getPelletRouter() { return this.pelletRouter; }

    @Override
    public void makePlan(final GameView game)
    {
        // TODO: implement me! This method is responsible for calculating
        // the "plan" of Coordinates you should visit in order to get from a starting
        // location and another ending location. I recommend you use
        // this.getBoardRouter().graphSearch(...) to get a path and convert it into
        // a Stack of Coordinates (see the documentation for SearchAgent)
        // which your makeMove can do something with!

        //System.out.println("Pacman ID: " + game.getEntity(getPacmanId()));

        PelletVertex vertex = new PelletVertex(game);
        Coordinate start = vertex.getPacmanCoordinate();
        Set<Coordinate> pellets = vertex.getRemainingPelletCoordinates();

        Stack<Coordinate> plan = new Stack<>();

        if (pellets.isEmpty()) {
            setPlanToGetToTarget(new Stack<>());
            return;
        }

        Coordinate target = null;
        float best = Float.MAX_VALUE;

        for (Coordinate p : pellets) {
            float dist = DistanceMetric.manhattanDistance(start, p);
            if (dist < best) {
                best = dist;
                target = p;
            }
        }

        if (target == null) {
            setPlanToGetToTarget(new Stack<>());
            return;
        }

        Path<Coordinate> path = this.getBoardRouter().graphSearch(start, target, game);
        Stack<Coordinate> plan = new Stack<>();
        while (path != null) {
            Coordinate c = path.getDestination();
            plan.push(c);
            path = path.getParentPath();
        }

        if (!plan.isEmpty()) {
            plan.pop();
        }

        setPlanToGetToTarget(plan);
    }

    @Override
    public Action makeMove(final GameView game)
    {
        // TODO: change me!
        PelletVertex vertex = new PelletVertex(game);
        Coordinate pacman = vertex.getPacmanCoordinate();

        if (this.getPlanToGetToTarget().isEmpty()) {
            makePlan(game);
        }

        if (this.getPlanToGetToTarget().isEmpty()) {
            return Action.values()[this.getRandom().nextInt(Action.values().length)];
        }

        Coordinate nextMove = this.getPlanToGetToTarget().pop();
        System.out.println("Next Move: " + nextMove);
        for (Action action : Action.values()) {
            System.out.println("Pacman: " + pacman);
            System.out.println("Neighbor: " + pacman.getNeighbor(action) + " Next Move: " + nextMove);
            if (pacman.getNeighbor(action).equals(nextMove)) {
                System.out.println("Action: " + action);
                return action;
            }
        }

        return Action.values()[this.getRandom().nextInt(Action.values().length)];
    }

    @Override
    public void afterGameEnds(final GameView game)
    {
        // if you want to log stuff after a game ends implement me!
    }
}

package src.pas.pacman.routing;


// SYSTEM IMPORTS
import java.util.Collection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.game.Tile;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.routing.BoardRouter;
import edu.bu.pas.pacman.routing.BoardRouter.ExtraParams;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;
import edu.bu.pas.pacman.utils.DistanceMetric;


// This class is responsible for calculating routes between two Coordinates on the Map.
// Use this in your PacmanAgent to calculate routes that (if followed) will lead
// Pacman from some Coordinate to some other Coordinate on the map.
public class ThriftyBoardRouter
    extends BoardRouter
{

    // If you want to encode other information you think is useful for Coordinate routing
    // besides Coordinates and data available in GameView you can do so here.
    public static class BoardExtraParams
        extends ExtraParams
    {

    }

    // feel free to add other fields here!

    public ThriftyBoardRouter(int myUnitId,
                              int pacmanId,
                              int ghostChaseRadius)
    {
        super(myUnitId, pacmanId, ghostChaseRadius);

        // if you add fields don't forget to initialize them here!
    }


    @Override
    public Collection<Coordinate> getOutgoingNeighbors(final Coordinate src,
                                                       final GameView game,
                                                       final ExtraParams params)
    {
        // TODO: implement me!
        Collection<Coordinate> neighbors = new ArrayList<Coordinate>();

        for (Action action : Action.values()) {
            Coordinate neighbor = src.getNeighbor(action);
            if ((game.isInBounds(neighbor)) && (game.getTile(neighbor).getState() != Tile.State.WALL)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    @Override
    public Path<Coordinate> graphSearch(final Coordinate src,
                                        final Coordinate tgt,
                                        final GameView game)
    {
        PriorityQueue<Path<Coordinate>> open = new PriorityQueue<>(Comparator.comparingDouble(p -> p.getTrueCost() + p.getEstimatedPathCostToGoal()));

        Map<Coordinate, Double> bestCost = new HashMap<>();
        bestCost.put(src, 0.0);

        Path<Coordinate> start = new Path<Coordinate>(src, (float)1.0, DistanceMetric.manhattanDistance(src, tgt), null);
        open.add(start);

        while (!open.isEmpty()) {
            Path<Coordinate> currentPath = open.poll();
            Coordinate current = currentPath.getDestination();
            double currentCost = currentPath.getTrueCost();

            double bestKnown = bestCost.getOrDefault(current, Double.POSITIVE_INFINITY);
            if (currentCost > bestKnown) {
                continue;
            }
            if (current.equals(tgt)) {
                return currentPath;
            }
            for (Coordinate nxt : getOutgoingNeighbors(current, game, null)) {
                double stepCost = 1.0;
                double tempCost = currentCost + stepCost;
                
                double prevBest = bestCost.getOrDefault(nxt, Double.POSITIVE_INFINITY);
                if (tempCost < prevBest) {
                    bestCost.put(nxt, tempCost);

                    Path<Coordinate> nxtPath = new Path<Coordinate>(nxt, (float)tempCost, DistanceMetric.manhattanDistance(nxt, tgt), currentPath);
                    open.add(nxtPath);
                }
            }
        }

        return null;
    }

}


package src.pas.pacman.routing;


// SYSTEM IMPORTS
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayList;


// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Tile;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.routing.PelletRouter;
import edu.bu.pas.pacman.routing.PelletRouter.ExtraParams;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.DistanceMetric;
import edu.bu.pas.pacman.utils.Pair;


public class ThriftyPelletRouter
    extends PelletRouter
{

    // If you want to encode other information you think is useful for planning the order
    // of pellets ot eat besides Coordinates and data available in GameView
    // you can do so here.
    public static class PelletExtraParams
        extends ExtraParams
    {

    }

    // feel free to add other fields here!

    public ThriftyPelletRouter(int myUnitId,
                               int pacmanId,
                               int ghostChaseRadius)
    {
        super(myUnitId, pacmanId, ghostChaseRadius);

        // if you add fields don't forget to initialize them here!
    }

    @Override
    public Collection<PelletVertex> getOutgoingNeighbors(final PelletVertex src,
                                                         final GameView game,
                                                         final ExtraParams params)
    {
        // TODO: implement me!
        Collection<PelletVertex> collection = new ArrayList<PelletVertex>();

        for (Coordinate c : src.getRemainingPelletCoordinates()) {
            collection.add(src.removePellet(c));
        }

        return collection;
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
                               final PelletVertex dst,
                               final ExtraParams params)
    {
        // TODO: implement me!
        Coordinate c1 = src.getPacmanCoordinate();
        Coordinate c2 = dst.getPacmanCoordinate();

        return DistanceMetric.manhattanDistance(c1, c2);

    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game,
                              final ExtraParams params)
    {
        // TODO: implement me!
        float total = 0f;
        PelletVertex currVertex = src;
        PelletVertex next = new PelletVertex(game);
        while (true) {
            if (currVertex.getRemainingPelletCoordinates().isEmpty()) {
                return total;
            }
            float minDistance = Float.POSITIVE_INFINITY;
            Coordinate current = currVertex.getPacmanCoordinate();
            for (PelletVertex p : getOutgoingNeighbors(currVertex, game, params)) {
                minDistance = Math.min(minDistance, DistanceMetric.manhattanDistance(current, p.getPacmanCoordinate()));
                next = p;
            }
            total += minDistance;  
            currVertex = next; 
        }
    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) 
    {
        // TODO: implement me!
        return null;
    }

}


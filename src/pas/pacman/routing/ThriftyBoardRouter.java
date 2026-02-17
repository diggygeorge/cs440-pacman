package src.pas.pacman.routing;


// SYSTEM IMPORTS
import java.util.Collection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.game.Tile;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
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
        PelletVertex vertex = new PelletVertex(game);
        Set<Coordinate> pellets = vertex.getRemainingPelletCoordinates();
        System.out.println("Pellets Remaining: " + pellets.size());
        Queue<Path<Coordinate>> queue = new LinkedList<Path<Coordinate>>();
        Path<Coordinate> srcPath = new Path<Coordinate>(src);
        queue.add(srcPath);

        while (!queue.isEmpty()) {
            Path<Coordinate> p = queue.poll();
            Coordinate current = p.getDestination();
            if (pellets.contains(current)) {
                return p;
            }

            for (Coordinate c : getOutgoingNeighbors(current, game, null)) {
                Path<Coordinate> newPath = new Path<Coordinate>(c, (float)1.0, p);
                queue.add(newPath);
            }
        }
        return null;

    }

}


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
        Coordinate current = src.getPacmanCoordinate();
        Collection<PelletVertex> collection = new ArrayList<PelletVertex>();

        dfs(collection, src, current, game, params);

        return collection;
    }

    private void dfs(final Collection<PelletVertex> collection,
                                         final PelletVertex src,
                                         final Coordinate current, 
                                         final GameView game, 
                                         final ExtraParams params) {

        if (game.getTile(current).getState() == Tile.State.PELLET) {
            collection.add(src.removePellet(current));
            return;
        }

        for (Action action : Action.values()) {
            Coordinate neighbor = current.getNeighbor(action);
            dfs(collection, src, neighbor, game, params);
        }
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
                               final PelletVertex dst,
                               final ExtraParams params)
    {
        // TODO: implement me!
        Coordinate c1 = src.getPacmanCoordinate();
        Coordinate c2 = dst.getPacmanCoordinate();

        Pair<Float, Float> p1 = new Pair<Float, Float>((float)c1.x(), (float)c1.y());
        Pair<Float, Float> p2 = new Pair<Float, Float>((float)c2.x(), (float)c2.y());

        return (float)(Math.pow((double)(p2.getFirst() - p1.getFirst()), 2.0) + Math.pow((double)(p2.getSecond() - p1.getSecond()), 2.0));

    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game,
                              final ExtraParams params)
    {
        // TODO: implement me!
        return 0f;
    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) 
    {
        // TODO: implement me!
        return null;
    }

}


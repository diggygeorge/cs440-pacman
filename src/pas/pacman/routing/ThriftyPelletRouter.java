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
import edu.bu.pas.pacman.routing.BoardRouter;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;


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
    private final BoardRouter boardRouter;

    public ThriftyPelletRouter(int myUnitId,
                               int pacmanId,
                               int ghostChaseRadius)
    {
        super(myUnitId, pacmanId, ghostChaseRadius);

        // if you add fields don't forget to initialize them here!
        this.boardRouter = new ThriftyBoardRouter(myUnitId, pacmanId, ghostChaseRadius);
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
        Set<Coordinate> pellets = src.getRemainingPelletCoordinates();
        if (pellets.isEmpty()) {
            return 0f;
        }

        Coordinate pos = src.getPacmanCoordinate();
        float best = Float.MAX_VALUE;

        for (Coordinate p : pellets) {
            float dist = DistanceMetric.manhattanDistance(pos, p);
            if (dist < best) {
                best = dist;
            }
            return best;
        }
    }

    private staic float boardPathLength(Path<Coordinate> path) {
        float steps = 0f;
        while (p != null && p.getParentPath() != null) {
            steps += 1f;
            p = p.getParentPath();
        }
        return steps;
    }

    private float edgeWeight(final PelletVertex src, final PelletVertex dst, final GameView game) {
        Coordiinate c1 = src.getPacmanCoordinate();
        Coordinate c2 = dst.getPacmanCoordinate();
        Path<Coordinate> path = this.boardRouter.graphSearch(c1, c2, game);

        if (path == null) {
            return Float.POSITIVE_INFINITY;
        }
        return boardPathLength(path);
    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) 
    {
        // TODO: implement me!
        final PelletVertex start = new PelletVertex(game);

        if (start.getRemainingPelletCoordinates().isEmprty()) {
            return new Path<>(start);
        }

        class Node {
            final Path<PelletVertex> path;
            final float cost;
            final float total;

            Node(Path<PelletVertex> path, float cost, float total) {
                this.path = path;
                this.cost = cost;
                this.total = total;
            }
        }

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.total));
        Map<PelletVertex, Float> bestCost = new HashMap<>();

        Path<PelletVertex> startPath = new Path<>(start);
        float heuristic = getHeuristic(start, game, null);
        queue.add(new Node(startPath, 0f, heuristic));
        bestCost.put(start, 0f);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            PelletVertex currentVertex = current.path.getDestination();

            Float recCost = bestCost.get(currentVertex);
            if (recCost != null && current.cost > recCost) {
                continue;
            }

            if (currentVertex.getRemainingPelletCoordinates().isEmpty()) {
                return current.path;
            }

            for (PelletVertex neighbor : getOutgoingNeighbors(currentVertex, game, null)) {
                float edgeCost = edgeWeight(currentVertex, neighbor, game);
                if (Float.isInfinite(edgeCost)) {
                    continue;
                }

                float newCost = current.cost + edgeCost;
                float oldCost = bestCost.get(neighbor);

                if (oldCost == null || newCost < oldCost) {
                    bestCost.put(neighbor, newCost);
                    float heuristicCost = getHeuristic(neighbor, game, null);
                    float newTotal = newCost + heuristicCost;
                    Path<PelletVertex> newPath = new Path<>(neighbor, edgeCost, current.path);
                    queue.add(new Node(newPath, newCost, newTotal));
                }
            }
        }
        return null;
    }
}


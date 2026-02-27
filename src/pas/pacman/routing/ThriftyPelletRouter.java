package src.pas.pacman.routing;


// SYSTEM IMPORTS
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
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
    private final Map<Pair<Coordinate, Coordinate>, Integer> distCache = new HashMap<>();
    private GameView currentGameView;

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
        if (src == null || dst == null) {
            return Float.MAX_VALUE;
        }
        Coordinate c1 = src.getPacmanCoordinate();
        Coordinate c2 = dst.getPacmanCoordinate();

        if (this.currentGameView != null) {
            int d = getMazeDistance(c1, c2, this.currentGameView);
            if (d == Integer.MAX_VALUE) {
                return Float.POSITIVE_INFINITY;
            }
            else {
                return (float) d;
            }
        }

        return DistanceMetric.manhattanDistance(c1, c2);
    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game,
                              final ExtraParams params)
    {
        Collection<Coordinate> remaining = src.getRemainingPelletCoordinates();
        if (remaining.isEmpty()) {
            return 0f;
        }

        Coordinate pacmanCoord = src.getPacmanCoordinate();
        int best = 0;
        for (Coordinate pellet : remaining) {
            int d = getMazeDistance(pacmanCoord, pellet, game);
            if (d != Integer.MAX_VALUE && d > best) {
                best = d;
            }
        }
        return (float) best;
    }

    private int getMazeDistance(final Coordinate a, final Coordinate b, final GameView game) {
        if (a.equals(b)) {
            return 0;
        }

        Pair<Coordinate, Coordinate> key = new Pair<>(a, b);
        Integer cached = this.distCache.get(key);
        if (cached != null) {
            return cached;
        }

        Queue<Coordinate> queue = new LinkedList<>();
        Map<Coordinate, Integer> distances = new HashMap<>();

        queue.add(a);
        distances.put(a, 0);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();
            int currentDist = distances.get(current);

            for (Coordinate next : this.boardRouter.getOutgoingNeighbors(current, game, null)) {
                if (distances.containsKey(next)) {
                    continue;
                }

                int nextDist = currentDist + 1;

                if (next.equals(b)) {
                    this.distCache.put(key, nextDist);
                    this.distCache.put(new Pair<>(b, a), nextDist);
                    return nextDist;
                }

                distances.put(next, nextDist);
                queue.add(next);
            }
        }

        this.distCache.put(key, Integer.MAX_VALUE);
        this.distCache.put(new Pair<>(b, a), Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }

    private float edgeWeight(final PelletVertex src, final PelletVertex dst, final GameView game) {
        Coordinate c1 = src.getPacmanCoordinate();
        Coordinate c2 = dst.getPacmanCoordinate();
        int d = getMazeDistance(c1, c2, game);
        if (d == Integer.MAX_VALUE) {
            return Float.POSITIVE_INFINITY;
        }
        else {
            return (float) d;
        }
    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) {

        // TODO: implement me!
        this.distCache.clear();
        this.currentGameView = game;
        final PelletVertex start = new PelletVertex(game);

        if (start.getRemainingPelletCoordinates().isEmpty()) {
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

        PriorityQueue<Node> queue = new PriorityQueue<Node>(Comparator.comparingDouble(n -> n.total));
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
                //System.out.println("PelletGraph Path: " + current.path.toString());
                return current.path;
            }
            //System.out.println("Start: " + currentVertex.getPacmanCoordinate());
            for (PelletVertex neighbor : getOutgoingNeighbors(currentVertex, game, null)) {
                float edgeCost = edgeWeight(currentVertex, neighbor, game);
                if (Float.isInfinite(edgeCost)) {
                    continue;
                }

                float newCost = current.cost + edgeCost;
                float oldCost = bestCost.getOrDefault(neighbor, Float.MAX_VALUE);

                if (newCost < oldCost) {
                    bestCost.put(neighbor, newCost);
                    float heuristicCost = getHeuristic(neighbor, game, null);
                    float newTotal = newCost + heuristicCost;
                    Path<PelletVertex> newPath = new Path<>(neighbor, edgeCost, current.path);
                    queue.add(new Node(newPath, newCost, newTotal));
                    //System.out.println("Destination: " + newPath.getDestination().getPacmanCoordinate() + " Value: " + newTotal);
                }
            }
        }
        return null;
    }
}


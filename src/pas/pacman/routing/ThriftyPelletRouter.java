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
import java.util.Stack;
import java.util.HashSet;
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
    private Stack<PelletVertex> pathStack;
    private Map<Coordinate, Map<Coordinate, Float>> distanceCache = new HashMap<>();

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
        Coordinate p1 = src.getPacmanCoordinate();
        Coordinate p2 = dst.getPacmanCoordinate();

        return DistanceMetric.manhattanDistance(p1, p2);

    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game,
                              final ExtraParams params)
    {
        Collection<Coordinate> pellets = src.getRemainingPelletCoordinates();
        if (pellets.isEmpty()) {
            return 0f;
        }

        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> unvisited = new HashSet<>();

        for (Coordinate c : pellets) {
            unvisited.add(c);
        }

        visited.add(src.getPacmanCoordinate());

        float result = 0f;

        while (!unvisited.isEmpty()) {
            float minWeight = Float.MAX_VALUE;
            Coordinate next = null;

            for (Coordinate v : visited) {
                for (Coordinate u : unvisited) {
                    float distance = getCachedDistance(v, u, game);
                    if (distance < minWeight) {
                        minWeight = distance;
                        next = u;
                    }
                }
            }

            result += minWeight;
            visited.add(next);
            unvisited.remove(next);
        }

        return result;
    }

    private static float boardPathLength(Path<Coordinate> path) {
        float steps = 0f;
        Path<Coordinate> p = path;
        while (p != null && p.getParentPath() != null) {
            steps += 1f;
            p = p.getParentPath();
        }
        return steps;
    }

    private float getCachedDistance(Coordinate c1, Coordinate c2, final GameView game) {

        if (c1.equals(c2)) {
            return 0f;
        }

        if (distanceCache.containsKey(c1) && distanceCache.get(c1).containsKey(c2)) {
            return distanceCache.get(c1).get(c2);
        }

        Path<Coordinate> path = this.boardRouter.graphSearch(c1, c2, game);
        float dist = boardPathLength(path);

        distanceCache.putIfAbsent(c1, new HashMap<>());
        distanceCache.get(c1).put(c2, dist);

        distanceCache.putIfAbsent(c2, new HashMap<>());
        distanceCache.get(c2).put(c1, dist);

        return dist;
    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) {

        // TODO: implement me!
        // distanceCache.clear();

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
            //System.out.println("Pellets Remaining: " + currentVertex.getRemainingPelletCoordinates().size());
            for (PelletVertex neighbor : getOutgoingNeighbors(currentVertex, game, null)) {

                float edgeCost = getCachedDistance(currentVertex.getPacmanCoordinate(), neighbor.getPacmanCoordinate(), game);
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


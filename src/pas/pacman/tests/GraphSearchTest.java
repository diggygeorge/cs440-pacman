package src.pas.pacman.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Random;

import static org.junit.Assert.assertEquals;

import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.game.Tile;
import edu.bu.pas.pacman.game.Tile.State;
import edu.bu.pas.pacman.game.Board;
import src.pas.pacman.routing.ThriftyBoardRouter;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.game.Game;
import edu.bu.pas.pacman.game.Game.GameView;

public class GraphSearchTest {

    private ThriftyBoardRouter router;
    private GameView game;

    @Before
    public void setUp() {
        // Initialize the router
        this.router = new ThriftyBoardRouter(0, 0, 5);
        // 1. Create a 7x7 board filled entirely with WALLs by default
        Tile.State[][] states = new Tile.State[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                states[x][y] = Tile.State.WALL;
            }
        }

        // 2. Carve out the EMPTY paths to match the autograder's maze
        // Column 1
        states[1][1] = Tile.State.EMPTY;
        states[1][2] = Tile.State.EMPTY;
        states[1][3] = Tile.State.EMPTY;
        states[1][4] = Tile.State.EMPTY;
        states[1][5] = Tile.State.EMPTY;

        // Column 2
        states[2][1] = Tile.State.EMPTY;
        states[2][3] = Tile.State.EMPTY;
        states[2][5] = Tile.State.EMPTY;

        // Column 3
        states[3][1] = Tile.State.EMPTY;
        states[3][2] = Tile.State.EMPTY;
        states[3][3] = Tile.State.EMPTY;
        states[3][5] = Tile.State.EMPTY;

        // Column 4
        states[4][1] = Tile.State.EMPTY;
        states[4][5] = Tile.State.EMPTY;

        // Column 5
        states[5][1] = Tile.State.EMPTY;
        states[5][2] = Tile.State.EMPTY;
        states[5][3] = Tile.State.EMPTY;
        states[5][4] = Tile.State.EMPTY;
        states[5][5] = Tile.State.EMPTY;

        // We need a ghost pen to satisfy the Game constructor, let's put it in the wall at (3,4)
        states[3][4] = Tile.State.GHOST_PEN;

        // 3. Instantiate the Board
        Random random = new Random(12345); // Seeded so it behaves predictably
        Board board = new Board(random, states);

        // 4. Instantiate the Game
        Coordinate pacmanStart = new Coordinate(5, 4); // Match the test's src coordinate
        Coordinate ghostPenCoord = new Coordinate(3, 4);
        int numGhosts = 0; // Keep it 0 so ghosts don't interfere with your routing test
        int numPacmanLives = 3;
        int maxNumTurns = 100;

        Game gameObj = new Game(
            pacmanStart, 
            ghostPenCoord, 
            numGhosts, 
            numPacmanLives, 
            random, 
            board, 
            maxNumTurns
        );

    // 5. Finally, create the GameView!
    this.game = new GameView(gameObj);
    }

    @Test
    public void testGraphSearch_SameSourceAndTarget() {
        Coordinate src = new Coordinate(5, 5);
        Coordinate tgt = new Coordinate(5, 5);

        // Since src equals tgt, the code never uses GameView, so null is safe!
        Path<Coordinate> result = router.graphSearch(src, tgt, null);

        assertNotNull("Path should not be null when src and tgt are the same", result);
        assertEquals("The path destination should match the target", src, result.getDestination());
        assertNull("The path should have no parents", result.getParentPath());
    }

    @Test
    public void testGraphSearch_PathFrom5_4to3_2() {
        Coordinate src = new Coordinate(5, 4);
        Coordinate tgt = new Coordinate(3, 2);

        // TODO: You CANNOT pass null here for a real search. 
        // You must initialize the GameView with the map used in the autograder.
        // Example: GameView game = new Game(new Board("test_map.txt")).getGameView();

        Path<Coordinate> result = router.graphSearch(src, tgt, this.game);

        // 1. Verify a path was actually found
        assertNotNull("Path should not be null; a valid route should exist", result);
        
        // 2. Verify the final destination is exactly the target
        assertEquals("The path destination should match the target", tgt, result.getDestination());

        // 3. Count the steps to verify it takes exactly 6 actions to route around the walls
        int pathLength = 0;
        Path<Coordinate> current = result;
        
        while (current.getParentPath() != null) {
            pathLength++;
            current = current.getParentPath();
        }

        assertEquals("The path should take exactly 6 actions based on the map layout", 6, pathLength);
        
        // 4. Verify the path traced all the way back to the starting point
        assertEquals("The origin of the path should trace back to the source", src, current.getDestination());
    }
}
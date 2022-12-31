/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxglgames.othello;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.net.*;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.getNetService;
import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * A simple animated game of Othello
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class OthelloApp extends GameApplication implements MessageHandler<String> {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setHeight(600);
        settings.setWidth(800);
        settings.setTitle("Othello");
        settings.setVersion("1.0");
        settings.setFontUI("othello.ttf");
        settings.setApplicationMode(ApplicationMode.DEBUG);
    }

    // Declare the constants
    /** A constant representing an empty square on the board. */
    private static final char NO_CHIP = ' ';
    /** A constant representing a black peg on the board. */
    private static final char BLACK_UP = 'b';
    /** A constant representing a white peg on the board. */
    private static final char WHITE_UP = 'w';
    /** A constant indicating the size of the game board. */
    private static final int BOARD_SIZE = 8;

    // Declare the instance variables
    /** This array keeps track of the logical state of the game. */
    private Entity[][] grid = new Entity[BOARD_SIZE][BOARD_SIZE];
    double CELL_WIDTH = 100;
    double CELL_HEIGHT = 75;
    /** This board contains the physical state of the game. */

    private Server<String> server;

//    @Override
//    protected void initInput() {
//        getInput().addAction(new UserAction("Up1") {
//            @Override
//            protected void onAction() {
//                player1Bat.up();
//            }
//
//            @Override
//            protected void onActionEnd() {
//                player1Bat.stop();
//            }
//        }, KeyCode.W);
//
//        getInput().addAction(new UserAction("Down1") {
//            @Override
//            protected void onAction() {
//                player1Bat.down();
//            }
//
//            @Override
//            protected void onActionEnd() {
//                player1Bat.stop();
//            }
//        }, KeyCode.S);
//
//        getInput().addAction(new UserAction("Up2") {
//            @Override
//            protected void onAction() {
//                player2Bat.up();
//            }
//
//            @Override
//            protected void onActionEnd() {
//                player2Bat.stop();
//            }
//        }, KeyCode.I);
//
//        getInput().addAction(new UserAction("Down2") {
//            @Override
//            protected void onAction() {
//                player2Bat.down();
//            }
//
//            @Override
//            protected void onActionEnd() {
//                player2Bat.stop();
//            }
//        }, KeyCode.K);
//    }


    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("player1score", 0);
        vars.put("player2score", 0);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new OthelloFactory());

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                grid[x][y] = spawn("gridcell",x * CELL_WIDTH , y * CELL_HEIGHT);
                System.out.println("Box No. " + ((x+1) + (8*y)) + " is at " + grid[x][y].getX() + " , "+grid[x][y].getY());
            }
        }
        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        server.setOnConnected(connection -> {
            connection.addMessageHandlerFX(this);
        });

//        for (int i=0; i<grid.length; i++)
//        {
//            for (int j=0; j<grid[i].length; j++)
//            {
//                grid[i][j]=NO_CHIP;
//            }
//        }
//        grid[3][3]=WHITE_UP;
//        grid[3][4]=BLACK_UP;
//        grid[4][3]=BLACK_UP;
//        grid[4][4]=WHITE_UP;




//
//        // This method must be called to refresh the board after the logic of the game has changed
//        this.updateView();
        var t = new Thread(server.startTask()::run);
        t.setDaemon(true);
        t.start();
    }
    public void cellClicked(Entity gridCell) {
       System.out.println(gridCell.getComponents());;
    }
//    @Override
//    protected void initPhysics() {
//        getPhysicsWorld().setGravity(0, 0);
//
//        getPhysicsWorld().addCollisionHandler(new CollisionHandler(TileValue.BALL, TileValue.WALL) {
//            @Override
//            protected void onHitBoxTrigger(Entity a, Entity b, HitBox boxA, HitBox boxB) {
//                if (boxB.getName().equals("LEFT")) {
//                    inc("player2score", +1);
//
//                    server.broadcast("SCORES," + geti("player1score") + "," + geti("player2score"));
//
//                    server.broadcast(HIT_WALL_LEFT);
//                } else if (boxB.getName().equals("RIGHT")) {
//                    inc("player1score", +1);
//
//                    server.broadcast("SCORES," + geti("player1score") + "," + geti("player2score"));
//
//                    server.broadcast(HIT_WALL_RIGHT);
//                } else if (boxB.getName().equals("TOP")) {
//                    server.broadcast(HIT_WALL_UP);
//                } else if (boxB.getName().equals("BOT")) {
//                    server.broadcast(HIT_WALL_DOWN);
//                }
//
//                getGameScene().getViewport().shakeTranslational(5);
//            }
//        });
//
//        CollisionHandler ballBatHandler = new CollisionHandler(TileValue.BALL, TileValue.PLAYER_BAT) {
//            @Override
//            protected void onCollisionBegin(Entity a, Entity bat) {
//                playHitAnimation(bat);
//
//                server.broadcast(bat == player1 ? BALL_HIT_BAT1 : BALL_HIT_BAT2);
//            }
//        };
//
//        getPhysicsWorld().addCollisionHandler(ballBatHandler);
//        getPhysicsWorld().addCollisionHandler(ballBatHandler.copyFor(TileValue.BALL, TileValue.ENEMY_BAT));
//    }


//    public void play()
//    {
//        // DO NOT MODIFY THIS CODE
//        // First move is made by black
//        char move = BLACK_UP;
//        while (!this.gameOver())
//        {
//            this.displayStatus(move);
//            int row = 0;
//            int col = 0;
//            boolean valid = false;
//
//            // Get a click from the player until he/she chooses a valid location
//            while (!valid)
//            {
//                Coordinate c = this.gameBoard.getClick();
//                row = c.getRow();
//                col = c.getCol();
//                valid = this.validMove(move, row, col);
//            }
//
//            this.takeTurn(move, row, col);
//            // After the turn, switch the players
//            if (move == BLACK_UP)
//            {
//                move = WHITE_UP;
//            } else
//            {
//                move = BLACK_UP;
//            }
//        }
//        this.endGame();
//    }

    @Override
    protected void onUpdate(double tpf) {
        if (!server.getConnections().isEmpty()) {
            var message = ("GAME_DATA," +   "," + ",") ;

            server.broadcast(message);
        }
    }

//    private void initScreenBounds() {
//        Entity walls = entityBuilder()
//                .type(TileValue.WALL)
//                .collidable()
//                .buildScreenBounds(150);
//
//        getGameWorld().addEntity(walls);
//    }

//    private void initGameObjects() {
//        ball = spawn("ball", getAppWidth() / 2 - 5, getAppHeight() / 2 - 5);
//        player1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isPlayer", true));
//        player2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isPlayer", false));
//
//        player1Bat = player1.getComponent(BatComponent.class);
//        player2Bat = player2.getComponent(BatComponent.class);
//    }

//    public boolean isWin(int player)
//    {
//        return ((board[0][0] + board[0][1] + board[0][2] == player*3) ||
//                (board[1][0] + board[1][1] + board[1][2] == player*3) ||
//                (board[2][0] + board[2][1] + board[2][2] == player*3) ||
//                (board[0][0] + board[1][0] + board[2][0] == player*3) ||
//                (board[0][1] + board[1][1] + board[2][1] == player*3) ||
//                (board[0][2] + board[1][2] + board[2][2] == player*3) ||
//                (board[0][0] + board[1][1] + board[2][2] == player*3) ||
//                (board[2][0] + board[1][1] + board[0][2] == player*3));
//    }
//private void takeTurn(char turn, int row, int col)
//{
//    // ADD CODE HERE
//    //Check Above
//    grid[row][col]=turn;
//    //check above & below
//    direction(row, col, turn, 0, -1);
//    direction(row, col, turn, 0, 1);
//    //check right & right
//    direction(row, col, turn, 1,0);
//    direction(row, col, turn, -1, 0);
//    //check corners
//    direction(row, col, turn, 1,1);
//    direction(row, col, turn, 1,-1);
//    direction(row, col, turn, -1,1);
//    direction(row, col, turn, -1,-1);
//
//    // This method must be called to refresh the board after the logic of the game has changed
//    this.updateView();
//}

//    private void direction(int row, int column, char colour, int colDir, int rowDir)
//    {
//        int currentRow= row + rowDir;
//        int currentCol = column + colDir;
//        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
//        {
//            return;
//        }
//        while (grid[currentRow][currentCol]==BLACK_UP || grid[currentRow][currentCol]==WHITE_UP)
//        {
//            if (grid[currentRow][currentCol]==colour)
//            {
//                while(!(row==currentRow && column==currentCol))
//                {
//                    grid[currentRow][currentCol]=colour;
//                    currentRow=currentRow-rowDir;
//                    currentCol=currentCol-colDir;
//                }
//                break;
//            }else
//            {
//                currentRow=currentRow + rowDir;
//                currentCol=currentCol + colDir;
//            }
//            if (currentRow<0 || currentCol<0 || currentRow==8 || currentCol==8)
//            {
//                break;
//            }
//        }
//    }

//    private boolean validMove(char turn, int row, int col)
//    {
//        // ADD CODE HERE
//        boolean result=false;
//        char oppCol=BLACK_UP;
//        if (turn==BLACK_UP)
//        {
//            oppCol=WHITE_UP;
//        }
//
//        //current
//        if (grid[row][col]==NO_CHIP)
//        {
//            if (row+1<8 && col+1<8 && grid[row+1][col+1]==oppCol)
//            {
//                result=true;
//            }else if(row+1<8 && grid[row+1][col]==oppCol)
//            {
//                result=true;
//            }else if(col+1<8 && grid[row][col+1]==oppCol)
//            {
//                result=true;
//            }else if (col-1>-1 && grid[row][col-1]==oppCol)
//            {
//                result=true;
//            }else if (row-1>-1 && col-1>-1 && grid[row-1][col-1]==oppCol)
//            {
//                result=true;
//            }else if (row-1>-1 && grid[row-1][col]==oppCol)
//            {
//                result=true;
//            }else if(row-1>-1 && col+1<8 && grid[row-1][col+1]==oppCol)
//            {
//                result=true;
//            }else if (row+1<8 && col-1>-1 && grid[row+1][col-1]==oppCol)
//            {
//                result = true;
//            }
//        }
//        return result;
//    }

    /**
     * This method will display the current status of the game.
     * The message will appear as
     * **black** has x pieces up --- white has y pieces up OR
     * black has x pieces up --- **white** has y pieces up
     * will appear at the bottom of the board, where x and y are numbers
     * indicating how many pegs of that colour are showing. The ** **
     * surrounding one of the colours indicates whose turn it is.
     * PRE: turn == BLACK_UP || WHITE_UP
     * POST: a message is displayed on the board that shows the number
     *       of pieces each player has. There are **  ** surrounding
     *       the colour of the player whose turn is next.
     */
//    private void displayStatus(char turn)
//    {
//        // ADD CODE HERE
//        int countb=0;
//        int countw=0;
//        for (int i=0; i<8; i++)
//        {
//            for (int j=0; j<8; j++)
//            {
//                if (grid[i][j]==BLACK_UP)
//                {
//                    countb++;
//                }else if(grid[i][j]==WHITE_UP)
//                {
//                    countw++;
//                }
//            }
//        }
//        if (turn==BLACK_UP)
//        {
//            this.gameBoard.displayMessage("**black** has " +countb+ " pieces up --- white has " +countw+ " pieces up");
//        }else if(turn==WHITE_UP)
//        {
//            this.gameBoard.displayMessage("black has " +countb+ " pieces up --- **white** has " +countw+ " pieces up");
//        }
//    }

    /**
     * This method will determine when the game is over.
     * The game is over when the board is filled with pegs.
     *
     * PRE: none
     * POST: Returns true when there are no valid moves left, false otherwise.
     */
//    private boolean gameOver()
//    {
//        // ADD CODE HERE
//        boolean full=false;
//        int countTot=0;
//        for (int i=0; i<8; i++)
//        {
//            for (int j=0; j<8; j++)
//            {
//                if (grid[i][j]==BLACK_UP || grid[i][j]==WHITE_UP)
//                {
//                    countTot++;
//                }
//            }
//        }
//        if (countTot==64)
//        {
//            full=true;
//        }
//        return full;
//    }

    /**
     * This method will display the message "Game over. The winner is x.",
     * where x is the colour of the player that has the most pegs up
     * at the end of the game. If the player's have the same number of
     * pegs up then the message "Game over. It is a tie game." should appear.
     *
     * PRE: the game is over
     * POST: a message indicating the winner of the game will appear
     */
//    private void endGame()
//    {
//        int countw=0;
//        int countb=0;
//        for (int i=0; i<grid.length; i++)
//        {
//            for (int j=0; j<grid[i].length; j++)
//            {
//                if (grid[i][j]==BLACK_UP)
//                {
//                    countb++;
//                }else if (grid[i][j]==WHITE_UP)
//                {
//                    countw++;
//                }
//            }
//        } if (countb>countw)
//    {
//        this.gameBoard.displayMessage("Game over. The winner is black.");
//    }else if (countw>countb)
//    {
//        this.gameBoard.displayMessage("Game over. The winner is white.");
//    }else
//    {
//        this.gameBoard.displayMessage("Game over. It is a tie game.");
//    }
//
//    }

    /**
     * This method will reflect the current state of the pegs on the board.
     * It should be called at the end of the constructor and the end of the
     * takeTurn method and any other time the game has logically changed.
     *
     * NOTE: This is the only method that requires calls to putPeg and removePeg.
     *       All other methods should manipulate the logical state of the game in the
     *       grid array and then call this method to refresh the gameBoard.
     *
     * PRE: none
     * POST: Where the array holds a value of BLACK_UP, a black peg is put in that spot.
     *       Where the array holds a value of WHITE_UP, a white peg is put in that spot.
     *       Where the array holds a value of NO_CHIP, a peg should be removed from that spot.
     */
//    private void updateView()
//    {
//        for (int i=0; i<grid.length; i++)
//        {
//            for (int j=0; j<grid[i].length; j++)
//            {
//                if (grid[i][j]==WHITE_UP)
//                {
//                    gameBoard.putPeg("white",i,j);
//                }else if(grid[i][j]==BLACK_UP)
//                {
//                    gameBoard.putPeg("black",i,j);
//                }
//            }
//        }
//
//    }

    @Override
    public void onReceive(Connection<String> connection, String message) {
        var tokens = message.split(",");

        Arrays.stream(tokens).skip(1).forEach(key -> {
            if (key.endsWith("_DOWN")) {
                getInput().mockKeyPress(KeyCode.valueOf(key.substring(0, 1)));
            } else if (key.endsWith("_UP")) {
                getInput().mockKeyRelease(KeyCode.valueOf(key.substring(0, 1)));
            }
        });
    }

    static class MessageWriterS implements TCPMessageWriter<String> {

        private OutputStream os;
        private PrintWriter out;

        MessageWriterS(OutputStream os) {
            this.os = os;
            out = new PrintWriter(os, true);
        }

        @Override
        public void write(String s) throws Exception {
            out.print(s.toCharArray());
            out.flush();
        }
    }

    static class MessageReaderS implements TCPMessageReader<String> {

        private BlockingQueue<String> messages = new ArrayBlockingQueue<>(50);

        private InputStreamReader in;

        MessageReaderS(InputStream is) {
            in =  new InputStreamReader(is);

            var t = new Thread(() -> {
                try {

                    char[] buf = new char[36];

                    int len;

                    while ((len = in.read(buf)) > 0) {
                        var message = new String(Arrays.copyOf(buf, len));

                        System.out.println("Recv message: " + message);

                        messages.put(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            t.setDaemon(true);
            t.start();
        }

        @Override
        public String read() throws Exception {
            return messages.take();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

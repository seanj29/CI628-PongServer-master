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

package com.almasb.fxglgames.tictactoe;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.net.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * A simple animated game of TicTacToe
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class TicTacToeApp extends GameApplication implements MessageHandler<String> {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setHeight(600);
        settings.setWidth(800);
        settings.setTitle("TicTacToe");
        settings.setVersion("1.0");
        settings.setFontUI("pong.ttf");
        settings.setApplicationMode(ApplicationMode.DEBUG);
    }

    private Entity[][] board = new Entity[3][3];
    private List<TileCombo> combos = new ArrayList<>();

    private Boolean playerXTurn = true;

    private Boolean clientisX = true;

    private Boolean symbolChosen = false;
    public List<TileCombo> getCombos() {
        return combos;
    }
    private Server<String> server;


    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new TicTacToe());

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                board[x][y] = spawn("tile", x * getAppWidth() / 3, y * getAppHeight() / 3);
            }
        }

        combos.clear();

        // horizontal
        for (int y = 0; y < 3; y++) {
            combos.add(new TileCombo(board[0][y], board[1][y], board[2][y]));
        }

        // vertical
        for (int x = 0; x < 3; x++) {
            combos.add(new TileCombo(board[x][0], board[x][1], board[x][2]));
        }

        // diagonals
        combos.add(new TileCombo(board[0][0], board[1][1], board[2][2]));
        combos.add(new TileCombo(board[2][0], board[1][1], board[0][2]));

        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        server.setOnConnected(connection -> {
            connection.addMessageHandlerFX(this);

        });
        server.startAsync();
    }


        public String BoardToString()
        {
            StringBuilder s = new StringBuilder();
            for(int j=0;j<3;j++) {
                for (int i = 0; i < 3; i++) {
                    switch (board[i][j].getComponent(GridCellComponent.class).getValue()) {
                        case X:
                            s.append("X");
                            break;
                        case O:
                            s.append("O");
                            break;
                        case NONE:
                            s.append("E");
                            break;

                    }
                }
            }
            return s.toString();
        }


    @Override
    protected void onUpdate(double tpf) {
        List<Connection<String>> connections = server.getConnections();
        var message = ("GAME_DATA," + BoardToString() + "," + checkGameFinished() + "," + symbolChosen + "," ) ;

        if (!connections.isEmpty()) {

                for (int i = 0; i < connections.size(); i++) {
                    String messageFull;
                    if (i == 0) {
                        messageFull = message.concat(clientisX + "," + connections.get(i).getConnectionNum() + ",");
                    }
                    else {
                        messageFull = message.concat(!clientisX + "," + connections.get(i).getConnectionNum() + ",");
                    }
                    connections.get(i).send(messageFull);
                }
        }

    }




    @Override
    public void onReceive(Connection<String> connection, String message) {
        var tokens = message.split(",");
        Arrays.stream(tokens).skip(1).forEach(key -> {
            if (key.matches("LEFT_DOWN")) {
                int x = Integer.parseInt(tokens[2]);
                int y = Integer.parseInt(tokens[3]);
                onUserMove(board[x][y]);
            }
            else if(key.matches("X")){
            symbolChosen = true;
            clientisX = true;
            }
            else if (key.matches("O")){
                symbolChosen = true;
                clientisX = false;
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
    @Override
    protected void initUI() {
        Line line1 = new Line(getAppWidth() / 3, 0, getAppWidth() / 3, 0);
        Line line2 = new Line(getAppWidth() / 3 * 2, 0, getAppWidth() / 3 * 2, 0);
        Line line3 = new Line(0, getAppHeight() / 3, 0, getAppHeight() / 3);
        Line line4 = new Line(0, getAppHeight() / 3 * 2, 0, getAppHeight() / 3 * 2);

        getGameScene().addUINodes(line1, line2, line3, line4);

        // animation
        KeyFrame frame1 = new KeyFrame(Duration.seconds(0.5),
                new KeyValue(line1.endYProperty(), getAppHeight()));

        KeyFrame frame2 = new KeyFrame(Duration.seconds(1),
                new KeyValue(line2.endYProperty(), getAppHeight()));

        KeyFrame frame3 = new KeyFrame(Duration.seconds(0.5),
                new KeyValue(line3.endXProperty(), getAppWidth()));

        KeyFrame frame4 = new KeyFrame(Duration.seconds(1),
                new KeyValue(line4.endXProperty(), getAppWidth()));

        Timeline timeline = new Timeline(frame1, frame2, frame3, frame4);
        timeline.play();
    }
    private boolean checkGameFinished() {
        for (TileCombo combo : combos) {
            if (combo.isComplete()) {
                playWinAnimation(combo);
                return true;
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Entity tile = board[x][y];
                if (tile.getComponent(GridCellComponent.class).isEmpty()) {
                    // at least 1 tile is empty
                    return false;
                }
            }
        }

        gameOver("DRAW");
        return true;
    }
    private void playWinAnimation(TileCombo combo) {
        Line line = new Line();
        line.setStartX(combo.getTile1().getCenter().getX());
        line.setStartY(combo.getTile1().getCenter().getY());
        line.setEndX(combo.getTile1().getCenter().getX());
        line.setEndY(combo.getTile1().getCenter().getY());
        line.setStroke(Color.YELLOW);
        line.setStrokeWidth(3);

        getGameScene().addUINode(line);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                new KeyValue(line.endXProperty(), combo.getTile3().getCenter().getX()),
                new KeyValue(line.endYProperty(), combo.getTile3().getCenter().getY())));
        timeline.setOnFinished(e -> gameOver(combo.getWinSymbol()));
        timeline.play();
    }
    private void gameOver(String winner) {
        getDialogService().showConfirmationBox("Winner: " + winner + "\nDid you enjoy it?", yes -> {
            if (yes)
                getGameController().exit();
            else
                getGameController().exit();
        });
    }
    public void onUserMove(Entity tile) {

        if (playerXTurn) {
            tile.getComponent(GridCellComponent.class).mark(TileValue.X);
            playerXTurn = false;
        }
        else    {
            tile.getComponent(GridCellComponent.class).mark(TileValue.O);
            playerXTurn = true;
            }
        clientisX = !clientisX;
        checkGameFinished();
        toString();
        }
    public static void main(String[] args) {
        launch(args);
    }
}

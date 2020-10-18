package sample;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TicTacToeClient {

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("");
    private JLabel scoreLabel = new JLabel("0");
    private JPanel boardPanel = new JPanel();
    Color ion;
    Color opponentIon;

    private Square[] board = new Square[9];
    private Square currentSquare;

    private static int PORT = 8901;
    public Socket socket;
    private BufferedReader input;
    private PrintWriter output;


    public void genConnection(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, PORT);
        input = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void genBoard() {
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, "South");

        scoreLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(scoreLabel, "North");


        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    output.println("MOVE " + j);}});
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

   public void play(Boolean again) throws Exception {
        System.out.println(socket);
        System.out.println(input);
        System.out.println(output);
        if(again){
            cleanBoard();
        }
        String response;
        try {
            response = input.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);

                ion = mark == 'X' ? Color.blue : Color.red;
                opponentIon  = mark == 'X' ? Color.red : Color.blue;

                frame.setTitle("Tic Tac Toe - Player " + mark);
            }
            while (true) {
                response = input.readLine();
                if (response.startsWith("SCORE")) {
                    scoreLabel.setText(response.substring(6));
                }
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setBackground(ion);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setBackground(opponentIon);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }


            }
//            out.println("QUIT");
        }
        finally {
        }
    }

    private void cleanBoard() {

        for (int i = 0; i < board.length; i++) {
            final int j = i;

            board[i].setBackground(Color.white);
        }
        frame.getContentPane().add(boardPanel, "Center");

        output.println("CLEAN");
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
                "Want to play again?",
                "Tic Tac Toe",
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    //Graphical square in the client window.  
    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);

        public Square() {
            setBackground(Color.white);
            add(label);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }
    }


    //main
    public static void main(String[] args) throws Exception {
        int clientScore = 0;
        Boolean again = false;
        String serverAddress = (args.length == 0) ? "localhost" : args[1];

        TicTacToeClient client = new TicTacToeClient();
        client.genConnection(serverAddress);
        client.genBoard();


        while (true) {
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(380, 400);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.play(again);

            if (!client.wantsToPlayAgain()) {
                break;
            }
            again = true;
        }


    }


}
/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.Random;
import java.util.ArrayList;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Michelle
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        if (sense == 1) {
            int best = -INFTY;
            ArrayList<Move> legalMoves = legalMoves(board, myColor());
            for (Move m: legalMoves) {
                Board tempBoard = new Board(board);
                tempBoard.makeMove(m);
                int res = minMax(tempBoard, depth - 1, true, -1, alpha, beta);
                if (res > best) {
                    if (saveMove) {
                        _lastFoundMove = m;
                    }
                    best = res;
                    alpha = max(alpha, best);
                    if (alpha >= beta) {
                        return best;
                    }
                }
            }
        } else if (sense == -1) {
            int best = INFTY;
            ArrayList<Move> legalMoves = legalMoves(board, myColor());
            for (Move m: legalMoves) {
                Board tempBoard = new Board(board);
                tempBoard.makeMove(m);
                int res = minMax(tempBoard, depth - 1, true, 1, alpha, beta);
                if (res < best) {
                    if (saveMove) {
                        _lastFoundMove = m;
                    }
                    best = res;
                    beta = min(beta, best);
                    if (alpha >= beta) {
                        return best;
                    }
                }
            }
        }
        return 0;
    }

    ArrayList<Move> legalMoves(Board b, PieceColor p) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (char row = '1'; row <= '7'; row++) {
            for (char col = 'a'; col <= 'g'; col++) {
                int currIndex = b.index(col, row);
                if (b.get(currIndex) == p) {
                    for (int j = -2; j <= 2; j++) {
                        for (int k = -2; k <= 2; k++) {
                            char newCol = (char) (col + k);
                            char newRow = (char) (row + j);
                            Move curMove = Move.move(col, row, newCol, newRow);
                            if (curMove != null && b.legalMove(curMove)) {
                                allMoves.add(curMove);
                            }
                        }
                    }
                }
            }
        }
        return allMoves;
    }



    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner == RED) {
            return winningValue;
        } else if (winner == BLUE) {
            return -1 * winningValue;
        } else if (winner == EMPTY) {
            return 0;
        } else {
            return board.numPieces(RED) - board.numPieces(BLUE);
        }
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}


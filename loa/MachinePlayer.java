/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.Collections;
import java.util.List;

import static loa.Piece.*;

/**
 * An automated Player.
 * 
 * @author Israel Rodriguez
 */
class MachinePlayer extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new MachinePlayer with no piece or controller (intended to produce
     * a template).
     */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move after searching the game tree to DEPTH>0 moves
     * from the current position. Assumes the game is not over.
     */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels. Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int findMove(Board board, int depth, boolean saveMove,
            int sense, int alpha, int beta) {
        boolean maximizing;
        maximizing = sense == 1;
        if (depth == 0) {
            return (int) evaluateScore(board, board.turn());
        }
        if (maximizing) {
            int maxVal = -INFTY;
            List<Move> p = board.legalMoves();
            for (Move m : p) {
                board.makeMove(m);
                int eval = findMove(board, depth - 1, false,
                        sense * -1, alpha, beta);
                board.retract();
                maxVal = Math.max(maxVal, eval);
                alpha = Math.max(alpha, eval);
                if (saveMove) {
                    _foundMove = m;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return maxVal;
        } else {
            int minValue = INFTY;
            for (Move m : board.legalMoves()) {
                board.makeMove(m);
                int eval = findMove(board, depth - 1, false,
                        sense * -1, alpha, beta);
                board.retract();
                minValue = Math.min(minValue, eval);
                beta = Math.min(beta, eval);
                if (saveMove) {
                    _foundMove = m;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return minValue;
        }

    }

    /**
     * Evaluates the position of Board B, and returns
     * an assigned score.
     */
    static double evaluateScore(Board board, Piece turn) {
        final double constA = 0.5;
        final double constB = 1.2;
        int turn1 = board.getRegionSizes(turn.opposite()).size();
        int turn2 = board.getRegionSizes(turn).size();
        int max1 = Collections.max(board.getRegionSizes(turn));
        int max2 = Collections.max(board.getRegionSizes(turn.opposite()));
        int factor1 = turn1 - turn2;
        int factor2 = max1 - max2;
        double val = constA * factor2 + constB * factor1;
        return val;
        // The more pieces I have, the better (are you sure about that?)
        // Some combinations of moves will lead to the same board state.
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}

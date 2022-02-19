/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import org.junit.Test;

import java.util.List;
import java.util.Arrays;

import static loa.Square.*;
import static org.junit.Assert.*;

import static loa.Piece.*;
import static loa.Move.mv;

/**
 * Tests of the Board class API.
 * 
 * @author Israel Rodriguez
 */
public class BoardTest {

    /** A "general" position. */
    static final Piece[][] BOARD1 = {
            { EMP, BP, EMP, BP, BP, EMP, EMP, EMP },
            { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
            { WP, EMP, EMP, EMP, BP, BP, EMP, WP },
            { WP, EMP, BP, EMP, EMP, WP, EMP, EMP },
            { WP, EMP, WP, WP, EMP, WP, EMP, EMP },
            { WP, EMP, EMP, EMP, BP, EMP, EMP, WP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, BP, BP, BP, EMP, BP, BP, EMP }
    };

    /** A position in which black, but not white, pieces are contiguous. */
    static final Piece[][] BOARD2 = {
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, BP, WP, BP, BP, BP, EMP, EMP },
            { EMP, WP, BP, WP, WP, EMP, EMP, EMP },
            { EMP, EMP, BP, BP, WP, WP, EMP, WP },
            { EMP, WP, WP, BP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, BP, EMP, EMP, EMP, EMP },
    };

    /** A position in which black, but not white, pieces are contiguous. */
    static final Piece[][] BOARD3 = {
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, BP, WP, BP, WP, EMP, EMP, EMP },
            { EMP, WP, BP, WP, WP, EMP, EMP, EMP },
            { EMP, EMP, BP, BP, WP, WP, WP, EMP },
            { EMP, WP, WP, WP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
    };

    static final String BOARD1_STRING = "===\n"
            + "    - b b b - b b - \n"
            + "    - - - - - - - - \n"
            + "    w - - - b - - w \n"
            + "    w - w w - w - - \n"
            + "    w - b - - w - - \n"
            + "    w - - - b b - w \n"
            + "    w - - - - - - w \n"
            + "    - b - b b - - - \n"
            + "Next move: black\n"
            + "===";

    /** Test display */
    @Test
    public void toStringTest() {
        assertEquals(BOARD1_STRING, new Board(BOARD1, BP).toString());
    }

    /** Tests function that returns the opposite direction. */
    @Test
    public void oppositeDirTest() {
        assertEquals(0, Square.oppositeDir(4));
        assertEquals(7, Square.oppositeDir(3));
        assertEquals(1, Square.oppositeDir(5));
    }

    /**
     * Tests whether the number of pieces on a particular line
     * can be computed.
     */
    @Test
    public void piecesInLineTest() {
        Square s1 = ALL_SQUARES[26];
        Square s2 = ALL_SQUARES[2];
        Board b = new Board(BOARD1, BP);
        List<Move> p = b.legalMoves();
        Square[] e = ALL_EDGES;
        assertEquals(3, b.piecesInLine(s1, s2));
        Square s3 = ALL_SQUARES[21];
        Square s4 = ALL_SQUARES[7];
        Square s5 = ALL_SQUARES[22];
        assertEquals(2, b.piecesInLine(s3, s4));
        assertEquals(4, b.piecesInLine(s3, s5));
    }

    /** Test legal moves. */
    @Test
    public void testLegality1() {
        Square a = Square.sq(2, 3);
        Square[] s = a.adjacent();
        Board b = new Board(BOARD1, BP);
        assertTrue("f3-d5", b.isLegal(mv("f3-d5")));
        assertTrue("f3-h5", b.isLegal(mv("f3-h5")));
        assertTrue("f3-h1", b.isLegal(mv("f3-h1")));
        assertTrue("f3-b3", b.isLegal(mv("f3-b3")));
        assertFalse("e3-h3", b.isLegal(mv("e3-h3")));
        assertFalse("e9-h3", b.isLegal(mv("e9-h3")));
        assertFalse("f3-d1", b.isLegal(mv("f3-d1")));
        assertFalse("f3-h3", b.isLegal(mv("f3-h3")));
        assertFalse("f3-e4", b.isLegal(mv("f3-e4")));
        assertFalse("c4-c7", b.isLegal(mv("c4-c7")));
        assertFalse("b1-b4", b.isLegal(mv("b1-b4")));
    }

    /** Test contiguity. */
    @Test
    public void testContiguous1() {
        Board b1 = new Board(BOARD1, BP);
        assertFalse("Board 1 black contiguous?", b1.piecesContiguous(BP));
        assertFalse("Board 1 white contiguous?", b1.piecesContiguous(WP));
        assertFalse("Board 1 game over?", b1.gameOver());
        Board b2 = new Board(BOARD2, BP);
        assertTrue("Board 2 black contiguous?", b2.piecesContiguous(BP));
        assertFalse("Board 2 white contiguous?", b2.piecesContiguous(WP));
        assertTrue("Board 2 game over", b2.gameOver());
        Board b3 = new Board(BOARD3, BP);
        assertTrue("Board 3 white contiguous?", b3.piecesContiguous(WP));
        assertTrue("Board 3 black contiguous?", b3.piecesContiguous(WP));
        assertTrue("Board 3 game over", b2.gameOver());
    }

    @Test
    public void testNumContig() {
        Board b1 = new Board(BOARD2, BP);
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        assertEquals(9, b1.numContig(sq(1, 3), visited, BP));
        for (boolean[] b : visited) {
            Arrays.fill(b, false);
        }
        Board b2 = new Board(BOARD3, BP);
        assertEquals(11, b2.numContig(sq(1, 6), visited, WP));
    }

    @Test
    public void testEquals1() {
        Board b1 = new Board(BOARD1, BP);
        Board b2 = new Board(BOARD1, BP);

        assertEquals("Board 1 equals Board 1", b1, b2);
    }

    @Test
    public void testCopyFrom() {
        Board b1 = new Board(Board.INITIAL_PIECES, BP);
        Board b2 = new Board(BOARD2, BP);
        b1.copyFrom(b2);
        assertEquals(b1, b2);
    }

    @Test
    public void testMove1() {
        Board b0 = new Board(BOARD1, BP);
        Board b1 = new Board(BOARD1, BP);
        b1.makeMove(mv("f3-d5"));
        assertEquals("square d5 after f3-d5", BP, b1.get(sq(3, 4)));
        assertEquals("square f3 after f3-d5", EMP, b1.get(sq(5, 2)));
        assertEquals("Check move count for board 1 after one move",
                1, b1.movesMade());
        b1.retract();
        assertEquals("Check for board 1 restored after retraction", b0, b1);
        assertEquals("Check move count for board 1 after move + retraction",
                0, b1.movesMade());
    }

}

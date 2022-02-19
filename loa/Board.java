/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.List;
import java.util.Formatter;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Israel Rodriguez
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        // FIXME
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _winner = null;
        flatten(contents);
        computeRegions();
    }

    /** Helper function that flattens 2D array TWOD into BOARD. */
    void flatten(Piece[][] twoD) {
        int ind = 0;
        for (int i = 0; i < twoD.length; i++) {
            for (int j = 0; j < twoD[i].length; j++) {
                _board[ind] = twoD[i][j];
                ind++;
            }
        }
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _turn = board._turn;
        _winner = board._winner;
        _moveLimit = board._moveLimit;
        _subsetsInitialized = board._subsetsInitialized;
        _movesMade = board._movesMade;
        for (int i = 0; i < _board.length; i++) {
            _board[i] = board._board[i];
        }
        _moves.clear();
        for (Move m : board._moves) {
            _moves.push(m);
        }
        // FIXME
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }


    /** Set limit on number of moves (before tie results) to LIMIT. */
    void setMoveLimit(int limit) {
        _moveLimit = limit;
        _winnerKnown = false;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        if (get(move.getTo()) == turn().opposite()) {
            _moves.push(move.captureMove());
            _movesMade.put(move.captureMove(), get(move.getTo()));
        } else {
            _moves.push(move);
            _movesMade.put(move, get(move.getTo()));
        }
        set(move.getTo(), get(move.getFrom()));
        set(move.getFrom(), EMP, turn().opposite());
        _moveLimit--;
        _winnerKnown = false;
        _subsetsInitialized = false;
        computeRegions();

    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        // FIXME
        Move prev = _moves.pop();
        Piece last = _movesMade.get(prev);
        set(prev.getFrom(), get(prev.getTo()));
        set(prev.getTo(), last, turn().opposite());
        /** if (!prev.isCapture()) {
         set(prev.getFrom(), get(prev.getTo()));
         set(prev.getTo(), EMP, turn().opposite());
         } else {
         Piece last = _movesMade.get(prev);
         set(prev.getFrom(), get(prev.getTo()));
         set(prev.getTo(), last, turn().opposite());
         } */
        _turn = turn().opposite();
        _movesMade.remove(prev);
        _winnerKnown = false;
        _moveLimit++;
        _subsetsInitialized = false;
        computeRegions();
    }

    /** Return the Piece representing who is currently supposed to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (!exists(to.col(), to.row()) ||
        !exists(from.col(), from.row())) {
            return false;
        }
        if (from.distance(to) != piecesInLine(from, to)) {
            return false;
        }
        if (_board[from.index()] != turn()) {
            return false;
        }
        if (blocked(from, to)) {
            return false;
        } else {
            return true;
        }
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        if (move == null) {
            return false;
        }
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        ArrayList<Move> legal = new ArrayList<>();
        for (Square s : ALL_SQUARES) {
            if (get(s) == EMP) {
                continue;
            }
            if (get(s) == turn()) {
                for (int a = 0; a < 4; a++) {
                    Square adj = s.moveDest(a, 1);
                    if (adj == null) {
                        continue;
                    }
                    SqList possibilities = lineOfSquares(s, adj);
                    for (Square p : possibilities) {
                        Move m = Move.mv(s, p);
                        if (isLegal(m)) {
                            legal.add(m);
                        }
                    }
                }
            }
        }
        return legal;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            if (_moveLimit == 0) {
                _winner = EMP;
                _winnerKnown = true;
            }
            else if (piecesContiguous(turn())) {
                _winner = turn();
                _winnerKnown = true;
            } else {
                _winner = null;
                _winnerKnown = false;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    /** Returns an array containing the squares between
     *  FROM and TO, inclusive. */
    public Square[] path(Square from, Square to) {
        int dir = from.direction(to);
        int dis = from.distance(to);
        int[] disp = dir(dir);
        Square[] path = new Square[dis + 1];
        int fromCol = from.col();
        int fromRow = from.row();
        path[0] = sq(fromCol, fromRow);
        for (int i = 1; i <= dis; i++) {
            fromCol += disp[0];
            fromRow += disp[1];
            path[i] = sq(fromCol,
                    fromRow);
        }
        return path;
    }

    /** Concatenates square arrays ARR1 and ARR2. */
    public Square[] mergeArrays(Square[] arr1, Square[] arr2) {
        Square[] merged = new Square[arr1.length + arr2.length - 1];
        System.arraycopy(arr1, 0, merged, 0, arr1.length);
        System.arraycopy(arr2, 1, merged, arr1.length, arr2.length - 1);
        return merged;
    }

    /** Returns a list of the Squares along the line of direction
     * of the move from Square F to Square T. */
    public SqList lineOfSquares(Square f, Square t) {
        SqList line = new SqList();
        int dir = f.direction(t);
        Square s = f;
        while (s != null) {
            line.add(s);
            s = s.moveDest(dir, 1);
        }
        s = f.moveDest(oppositeDir(dir), 1);
        while (s != null) {
            line.add(s);
            s = s.moveDest(oppositeDir(dir), 1);
        }
        return line;
    }

    /** Returns the number of pieces found in both the path between FROM
     * and TO, and the path between FROM and the square opposite from TO. */
    public int piecesInLine(Square from, Square to) {
        SqList line = lineOfSquares(from, to);
        int pieces = 0;
        for (Square sq : line) {
            if (_board[sq.index()] != EMP) {
                pieces++;
            }
        }
        return pieces;
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        Square[] path = path(from, to);
        if (_board[to.index()] == turn()) {
            return true;
        }
        for (int i = 1; i < path.length - 1; i++) {
            if (_board[path[i].index()] == turn().opposite()) {
                return true;
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    public int numContig(Square sq, boolean[][] visited, Piece p) {
        Square[] adj = sq.adjacent();
        if (p == EMP) {
            return  0;
        }
        if (get(sq) != p) {
            return 0;
        }
        if (visited[sq.col()][sq.row()]) {
            return 0;
        }
        visited[sq.col()][sq.row()] = true;
        int total = 1;
        for (Square s : adj) {
            total += numContig(s, visited, p);
        }
        return total;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();

        for (Square s : ALL_SQUARES) {
            if (get(s) == BP) {
                int bsize = numContig(s, visited, BP);
                if (bsize != 0) {
                    _blackRegionSizes.add(bsize);
                }
            }
            if (get(s) == WP) {
                int wsize = numContig(s, visited, WP);
                if (wsize != 0) {
                    _whiteRegionSizes.add(wsize);
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }



    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];
    /** List of all unretracted moves on this board, in order. */
    private final Stack<Move> _moves = new Stack<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;
    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;
    /** HashMap containing moves already made and the type
     * of piece found before the move was made. */
    private HashMap<Move, Piece> _movesMade = new HashMap<>();
    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}

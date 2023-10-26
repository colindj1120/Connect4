package com.colin.game.algorithms.interfaces;

import com.colin.game.algorithms.objects.Move;

@FunctionalInterface
public interface BestMoveUpdater {
    Move apply(Move bestMove, int column, int moveScore);
}


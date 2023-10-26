package com.colin.game.algorithms.interfaces;

import com.colin.game.algorithms.objects.AlphaBeta;

@FunctionalInterface
public interface AlphaBetaUpdater {
    AlphaBeta apply(AlphaBeta alphaBeta, int moveScore);
}


package br.org.catolicasc.trekking.views;

import android.content.Context;

import br.org.catolicasc.trekking.models.Point;

public interface MainView {
    Context getContextForPresenter();
    void successfullySavedPoint(Point p);
    void successfullyRemovedPoint(Point p);
}

package br.org.catolicasc.trekking.presenters;

import android.util.Log;

import java.util.List;

import br.org.catolicasc.trekking.dal.PointDal;
import br.org.catolicasc.trekking.models.Point;
import br.org.catolicasc.trekking.views.MainView;

public class MainPresenter {
    private static final String TAG = "MainPresenter";
    private MainView view;

    public MainPresenter(MainView view) {
        this.view = view;
    }

    public List<Point> fetchPoints() {
        PointDal dal = new PointDal(view.getContextForPresenter());
//        Point p = new Point(-26.4673054, -49.1158967);
//        dal.createGeographicPoint(p);
        return dal.findAllGeographicPoints();
    }

    public boolean deletePoint(Point point) {
        if (!point.isPersisted()) { return false; }

        PointDal dal = new PointDal(view.getContextForPresenter());
        if (dal.deleteGeographicPoint(point.getId())) {
            view.successfullyRemovedPoint(point);
            return true;
        }

        return false;
    }

    public boolean savePoint(Point point) {
        if (!point.isValid()) {
            return false;
        }

        PointDal dal = new PointDal(view.getContextForPresenter());
        if (point.isPersisted()) {
            if (dal.updateGeographicPoint(point)) {
                view.successfullySavedPoint(point);
                Log.d(TAG, "Successfully updated Point: "+ point.getId());
            } else {
                Log.d(TAG, "Failed to update Point");
            }
        } else {
            long id = dal.createGeographicPoint(point);
            if (id > 0) {
                point.setId(id);
                view.successfullySavedPoint(point);
                Log.d(TAG, "Successfully created a new Point with id: " + id);
                return true;
            } else {
                Log.d(TAG, "Failed to create a new Point");
            }
        }


        return false;
    }
}

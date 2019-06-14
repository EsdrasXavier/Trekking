package br.org.catolicasc.trekking.presenters;

import java.util.List;

import br.org.catolicasc.trekking.dal.PointDal;
import br.org.catolicasc.trekking.models.Point;
import br.org.catolicasc.trekking.views.MainView;

public class MainPresenter {
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
}

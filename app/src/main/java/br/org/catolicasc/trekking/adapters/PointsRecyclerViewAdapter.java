package br.org.catolicasc.trekking.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import br.org.catolicasc.trekking.R;
import br.org.catolicasc.trekking.models.Point;
import br.org.catolicasc.trekking.models.PointType;

public class PointsRecyclerViewAdapter extends RecyclerView.Adapter<PointsRecyclerViewAdapter.PointViewHolder> {
    private static final String TAG = "PointsRecyclerViewAdapter";
    private List<Point> mPoints;
    private Context mContext;
    private Point mRecentlyDeletedPoint;
    private int mRecentlyDeletedPointPosition;
    private PointsCRUD mCrud;
    private CompoundButton.OnCheckedChangeListener mSwitchChangeListener;

    public interface PointsCRUD {
        void updatePoint(Point p);
        void deletePoint(Point p);
    }

    public PointsRecyclerViewAdapter(Context context, List<Point> Points, PointsCRUD crud) {
        this.mPoints = Points;
        this.mContext = context;
        this.mCrud = crud;
    }

    public void setPoints(List<Point> Points) {
        this.mPoints = Points;
        notifyDataSetChanged();
    }

    public void addPoints(Point p) {
        this.mPoints.add(p);
        notifyDataSetChanged();
    }
    public Point getPoint(int index) {
        return ((mPoints != null) && (mPoints.size() != 0) ? mPoints.get(index) : null);
    }

    public void deleteItem(int index) {
        // @TODO Add undo option
        mRecentlyDeletedPoint = mPoints.get(index);
        mRecentlyDeletedPointPosition = index;
        mPoints.remove(index);
        notifyItemRemoved(index);
        mCrud.deletePoint(mRecentlyDeletedPoint);
    }

    public Context getContext() {
        return mContext;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.point_item, parent, false);
        PointViewHolder holder = new PointViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder viewHolder, int i) {
        Point resource = mPoints.get(i);

        viewHolder.tvLat.setText(resource.getPreciseLat(8));
        viewHolder.tvLon.setText(resource.getPreciseLon(8));
        if (resource.getType().isObstacle()) {
            viewHolder.swPointType.setChecked(true);
        } else {
            viewHolder.swPointType.setChecked(false);
        }

        viewHolder.swPointType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                resource.setTypeId(isChecked ? 2 : 1);
                mCrud.updatePoint(resource);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ((mPoints != null) && (mPoints.size() != 0) ? mPoints.size() : 0);
    }

    static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView tvLat;
        TextView tvLon;
        Switch swPointType;

        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvLat = itemView.findViewById(R.id.tv_lat);
            this.tvLon = itemView.findViewById(R.id.tv_lon);
            this.swPointType = itemView.findViewById(R.id.sw_point_type);
        }
    }
}

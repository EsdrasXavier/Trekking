package br.org.catolicasc.trekking.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import br.org.catolicasc.trekking.R;
import br.org.catolicasc.trekking.models.Point;

public class PointsRecyclerViewAdapter extends RecyclerView.Adapter<PointsRecyclerViewAdapter.PointViewHolder> {
    private static final String TAG = "PointsRecyclerViewAdapter";
    private List<Point> mPoints;
    private Context mContext;

    public PointsRecyclerViewAdapter(Context context, List<Point> Points) {
        this.mPoints = Points;
        this.mContext = context;
    }

    public void setPoints(List<Point> Points) {
        this.mPoints = Points;
        notifyDataSetChanged();
    }

    public Point getPoint(int index) {
        return ((mPoints != null) && (mPoints.size() != 0) ? mPoints.get(index) : null);
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

        viewHolder.tvLat.setText(Double.toString(resource.getLat()));
        viewHolder.tvLon.setText(Double.toString(resource.getLon()));
        if (resource.getType().isObstacle()) {
            viewHolder.swPointType.setChecked(true);
        } else {
            viewHolder.swPointType.setChecked(false);
        }
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

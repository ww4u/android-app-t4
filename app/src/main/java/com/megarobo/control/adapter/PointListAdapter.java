package com.megarobo.control.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.activity.EquipmentActivity;
import com.megarobo.control.activity.SearchActivity;
import com.megarobo.control.bean.Point;
import com.megarobo.control.bean.Pose;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.blockly.SetPointActivity;
import com.megarobo.control.utils.Utils;

import java.util.List;

public class PointListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Point> pointList;

    public PointListAdapter(SetPointActivity context, List<Point> pointList){
        this.mContext = context;
        this.pointList = pointList;
    }
    @Override
    public int getCount() {
        return pointList.size();
    }

    @Override
    public Object getItem(int position) {
        return pointList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.point_list_item,null);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.point = convertView.findViewById(R.id.point);
            viewHolder.chooseBtn = convertView.findViewById(R.id.choose);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Point point = pointList.get(position);
        final Pose pose = point.getPose();
        viewHolder.name.setText(point.getName());
        String positionStr = Utils.format(pose.getX())+","+
                Utils.format(pose.getY())+","+
                Utils.format(pose.getZ());
        viewHolder.point.setText(positionStr);

        viewHolder.chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetPointActivity setPointActivity = (SetPointActivity)mContext;
                setPointActivity.setEndPosition(pose);
                setPointActivity.goneBottomView();

            }
        });
        return convertView;
    }

    class ViewHolder{
        public TextView name;
        public TextView point;
        public TextView chooseBtn;
    }
}

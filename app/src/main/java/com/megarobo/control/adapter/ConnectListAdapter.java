package com.megarobo.control.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.activity.EquipmentActivity;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.utils.Utils;

import java.util.List;

public class ConnectListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Robot> robotList;

    public ConnectListAdapter(Context context, List<Robot> robotList){
        this.mContext = context;
        this.robotList = robotList;
    }
    @Override
    public int getCount() {
        return robotList.size();
    }

    @Override
    public Object getItem(int position) {
        return robotList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.connect_list_item,null);
            viewHolder.robotName = convertView.findViewById(R.id.robotName);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String sn = robotList.get(position).getMeta().getSn();
        viewHolder.robotName.setText(Utils.replaceX(sn));

        return convertView;
    }

    class ViewHolder{
        public TextView robotName;
    }
}

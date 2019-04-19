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

public class SearchListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Robot> robotList;

    public SearchListAdapter(Context context, List<Robot> robotList){
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.search_list_item,null);
            viewHolder.aliasTextView = convertView.findViewById(R.id.arpitem);
            viewHolder.connectBtn = convertView.findViewById(R.id.connect);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String sn = robotList.get(position).getMeta().getSn();
        viewHolder.aliasTextView.setText(Utils.replaceX(sn));
//        viewHolder.aliasTextView.setText(robotList.get(position).getIp());
        final String robotIP = robotList.get(position).getIp();
        viewHolder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //根据ip发起连接，并跳转到页面
                Intent intent = new Intent(mContext, EquipmentActivity.class);
                MegaApplication.ip = robotIP;
                MegaApplication.name = Utils.replaceX(robotList.get(position).getMeta().getSn());
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    class ViewHolder{
        public TextView aliasTextView;
        public TextView connectBtn;
    }
}

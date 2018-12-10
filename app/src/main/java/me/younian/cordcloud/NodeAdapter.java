package me.younian.cordcloud;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class NodeAdapter extends BaseAdapter {
    private int errorCount = 0;
    private Context context;
    private List<Map<String, String>> dataBeans;
    private ViewHolder viewHolder;

    public NodeAdapter(Context context, List<Map<String, String>> dataBeans) {
        this.context = context;
        this.dataBeans = dataBeans;
        for (Map<String, String> map : dataBeans) {
            if ("1".equals(map.get("error"))) {
                this.errorCount++;
            }
        }
    }

    @Override
    public int getCount() {
        return dataBeans.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_node, null);
            viewHolder.item = (RelativeLayout) view.findViewById(R.id.item);
            viewHolder.nodeName = (TextView) view.findViewById(R.id.nodeName);
            viewHolder.person = (TextView) view.findViewById(R.id.person);
            viewHolder.remain = (TextView) view.findViewById(R.id.remain);
            viewHolder.beilv = (TextView) view.findViewById(R.id.beilv);
            viewHolder.status = (TextView) view.findViewById(R.id.status);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (position == 0) {
            viewHolder.nodeName.setText("" + dataBeans.get(position).get("remain"));
            viewHolder.person.setText("" + dataBeans.get(position).get("today"));
            viewHolder.remain.setText("" + dataBeans.get(position).get("used"));
            int allCount = (dataBeans.size() - 1);
            viewHolder.status.setText("节点数：" + (allCount - errorCount) + "/" + allCount);
            viewHolder.beilv.setText("" + dataBeans.get(position).get("checkin"));

            viewHolder.nodeName.setTextColor(Color.parseColor("#000000"));
            viewHolder.person.setTextColor(Color.parseColor("#000000"));
            viewHolder.remain.setTextColor(Color.parseColor("#000000"));
            viewHolder.status.setTextColor(Color.parseColor("#000000"));
            viewHolder.beilv.setTextColor(Color.parseColor("#000000"));
            viewHolder.item.setBackgroundColor(Color.parseColor("#5997f9"));
            return view;
        }
        int personInt = Integer.parseInt(dataBeans.get(position).get("person"));

        viewHolder.nodeName.setText("" + dataBeans.get(position).get("nodeName"));
        viewHolder.person.setText("人数：" + personInt);
        viewHolder.remain.setText("余量：" + dataBeans.get(position).get("remain"));
        viewHolder.beilv.setText("倍率：" + dataBeans.get(position).get("beilv"));


        if (dataBeans.get(position).get("status").equals("warning")) {
            viewHolder.status.setText("报警");
            viewHolder.status.setTextColor(Color.parseColor("#FFF74905"));
        } else {
            viewHolder.status.setText("正常");
            viewHolder.status.setTextColor(Color.parseColor("#FF999999"));
        }

        if (personInt < MainActivity.baseCount) {
            viewHolder.person.setTextColor(Color.parseColor("#FFF74905"));
        } else {
            viewHolder.person.setTextColor(Color.parseColor("#FF999999"));
        }

        if (dataBeans.get(position).get("error").equals("1")) {
            viewHolder.item.setBackgroundColor(Color.parseColor("#FFDDDDDD"));
        } else {
            viewHolder.item.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }
        return view;
    }

    public class ViewHolder {
        private RelativeLayout item;
        private TextView nodeName;
        private TextView person;
        private TextView remain;
        private TextView beilv;
        private TextView status;

    }
}

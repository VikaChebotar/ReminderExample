package com.example.viktoria.reminderexample;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by viktoria on 27.01.15.
 */
public class MyListAdapter extends ArrayAdapter<Reminder> {
    private Context context;
    private Calendar calendar;
    private List<Reminder> items;
    private SparseBooleanArray selectedItemsIds;
    private int layoutResourceId; //layout of row

    public MyListAdapter(Context context, int layoutResourceId,
                         List<Reminder> items) {
        super(context, layoutResourceId, items);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.items = items;
        calendar = Calendar.getInstance();
        selectedItemsIds = new SparseBooleanArray();
    }

    //holder pattern used to avoids frequent call of findViewById()
    static class TaskHolder {
        TextView itemTitle;
        TextView itemTime;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Reminder getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TaskHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TaskHolder();
            holder.itemTitle = (TextView) row.findViewById(R.id.titleItem);
            holder.itemTime = (TextView) row.findViewById(R.id.timeItem);
            row.setTag(holder);
        } else {
            holder = (TaskHolder) row.getTag();
        }
        Reminder item = getItem(position);
        if (item != null) {
            holder.itemTitle.setText(item.getTitle());
            calendar.setTimeInMillis(item.getEventTime());
            calendar.set(Calendar.SECOND, 0);
            switch (item.getMinutesBeforeEventTime()) {
                case 0:
                    break;
                case 1:
                    calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 1);
                    break;
                case 5:
                    calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 5);
                    break;
                case 60:
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
                    break;
            }
            Long reminderTime = calendar.getTimeInMillis();
            holder.itemTime.setText(MainActivity.dateFormat.format(reminderTime) + ", " + MainActivity.timeFormat.format(reminderTime));
        }
        return row;
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            selectedItemsIds.put(position, value);
        else
            selectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public void removeSelection() {
        selectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedItemsIds;
    }
}

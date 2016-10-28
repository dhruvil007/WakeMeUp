package com.catacomblabs.wakemeup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private LayoutInflater inflater;
    List<Stations> dataName = Collections.emptyList();
    private ClickListener clickListener;

    public Adapter(Context context, List<Stations> data) {
        inflater = LayoutInflater.from(context);
        this.dataName = new ArrayList<>(data);

        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String temp = sharedPreferences.getString("sort_list", "1");
        int sortType = Integer.parseInt(temp);
        if (sortType == 0)
            sortList();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Stations current = dataName.get(position);
        holder.textView1.setText(current.name);
    }

    @Override
    public Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.cardviewforlist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataName.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setModels(List<Stations> dataName) {
        this.dataName = new ArrayList<>(dataName);
    }

    public Stations removeItem(int position) {
        final Stations removable = dataName.remove(position);
        notifyItemRemoved(position);
        return removable;
    }

    public void addItem(int position, Stations addable) {
        dataName.add(addable);
        notifyItemInserted(position);
    }

    private void sortList() {
        Collections.sort(this.dataName, new CustomComparator());
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Stations movable = dataName.remove(fromPosition);
        dataName.add(movable);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<Stations> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Stations> newModels) {
        for (int i = dataName.size() - 1; i >= 0; i--) {
            final Stations model = dataName.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Stations> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Stations model = newModels.get(i);
            if (!dataName.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Stations> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Stations model = newModels.get(toPosition);
            final int fromPosition = dataName.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView1;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            textView1 = (TextView) view.findViewById(R.id.text_view_list);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getAdapterPosition());
            }
        }
    }

    class CustomComparator implements Comparator<Stations> {
        @Override
        public int compare(Stations lhs, Stations rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    }

    public interface ClickListener {
        void itemClicked(View view, int position);
    }
}



package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
This class is an adapter for the RecyclerView of the highscore list (inside the DialogFR_LevelEnd).
 */
public class RV_Adapter_Highscore extends RecyclerView.Adapter<RV_Adapter_Highscore.HighscoreViewHolder> {

    private final ArrayList<RV_Item_Highscore> highscoreList;

    public RV_Adapter_Highscore(ArrayList<RV_Item_Highscore> list){
        highscoreList = list;
    }

    public static class HighscoreViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_position;
        public TextView tv_playerName;
        public TextView tv_CO2SavingsScoreValue;
        public TextView tv_Date;

    public HighscoreViewHolder (View itemView) {
        super(itemView);
        tv_position = itemView.findViewById(R.id.tv_position);
        tv_playerName = itemView.findViewById(R.id.tv_playerName);
        tv_CO2SavingsScoreValue = itemView.findViewById(R.id.tv_CO2SavingsScoreValue);
        tv_Date = itemView.findViewById(R.id.tv_Date);

    }


    }

    @NonNull
    public HighscoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_highscore, parent, false);
        return new HighscoreViewHolder(v);
    }


    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull RV_Adapter_Highscore.HighscoreViewHolder holder, int position) {
        RV_Item_Highscore currentItem = highscoreList.get(position);
        holder.tv_position.setText("" + currentItem.getPosition());
        holder.tv_playerName.setText(currentItem.getName());
        holder.tv_CO2SavingsScoreValue.setText("" + currentItem.getCo2Score());
        holder.tv_Date.setText(currentItem.getDate());

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<RV_Item_Highscore> newList) {
        highscoreList.clear(); // Clear the existing list
        highscoreList.addAll(newList); // Add all items from the new list
        notifyDataSetChanged(); // Notify adapter about the change
    }



    public int getItemCount() {
        return highscoreList.size();
    }



}

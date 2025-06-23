package io.thomasdengiz.heatPumpMaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
This class is an adapter for the RecyclerView of the level selection fragment.
 */
public class RV_Adapter_Level_Selection extends RecyclerView.Adapter<RV_Adapter_Level_Selection.LevelSelectionViewHolder> {

    private final ArrayList<RV_Item_Level_Selection_Menu> levelList;
    private OnItemClickListener listener;

    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }



    public RV_Adapter_Level_Selection(ArrayList<RV_Item_Level_Selection_Menu> list, Context context){
        levelList = list;
        this.context = context;
    }

    public void setOnItemClickListener (OnItemClickListener mListener) {

        listener = mListener;
    }

    public static class LevelSelectionViewHolder extends RecyclerView.ViewHolder {

        public ImageView levelImage;
        public TextView tv_bestResultThisLevel;
        public TextView tv_CO2SavingsTotalThisLevel;
        public TextView tv_gasSavingsTotalThisLevel;

        public CardView cardView_itemLevel;

        public LevelSelectionViewHolder (View itemView, final RV_Adapter_Level_Selection.OnItemClickListener listener)  {
            super(itemView);
            levelImage = itemView.findViewById(R.id.imageView_levelImage);
            tv_bestResultThisLevel = itemView.findViewById(R.id.textView_bestResultThisLevel);
            tv_CO2SavingsTotalThisLevel = itemView.findViewById(R.id.textView_CO2SavingsTotalThisLevel);
            tv_gasSavingsTotalThisLevel = itemView.findViewById(R.id.textView_gasSavingsTotalThisLevel);
            cardView_itemLevel = itemView.findViewById(R.id.cardView_itemLevel);
            itemView.setOnClickListener(view -> {
                if (listener !=null)  {
                    int position = getAdapterPosition();
                    if (position !=RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }


    }

    @NonNull
    public RV_Adapter_Level_Selection.LevelSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_level_selection_menu, parent, false);
        return new LevelSelectionViewHolder(v, listener);
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void onBindViewHolder(@NonNull RV_Adapter_Level_Selection.LevelSelectionViewHolder holder, int position) {
        RV_Item_Level_Selection_Menu currentItem = levelList.get(position);

        @SuppressLint("DiscouragedApi") int levelResourceIdentifier = context.getResources().getIdentifier("level" + currentItem.getLevelNumber() + "_not_selected", "drawable", context.getPackageName());
        if (currentItem.isSelected()) {
            holder.cardView_itemLevel.setBackgroundResource(R.drawable.cardview_foreground_rim_selected);
            levelResourceIdentifier = context.getResources().getIdentifier("level" + currentItem.getLevelNumber() + "_selected", "drawable", context.getPackageName());
        }
        else {
            holder.cardView_itemLevel.setBackgroundResource(R.drawable.cardview_foreground_rim_not_selected);
        }


        //Make the background light if the level is not unlocked
        if (!currentItem.isLevelUnlocked()) {
            holder.cardView_itemLevel.setAlpha(0.3f);
        }
        else {
            holder.cardView_itemLevel.setAlpha(1.0f);
        }

        holder.levelImage.setBackgroundResource(levelResourceIdentifier);
        holder.tv_bestResultThisLevel.setText(context.getString(R.string.best_result_semicolon) + " " + String.format("%.1f", levelList.get(position).getBestResultThisLevel()) + " %");
        holder.tv_CO2SavingsTotalThisLevel.setText(context.getString(R.string.co2_savings_total_semicolon) +" "+ levelList.get(position).getTotalCO2SavingsThisLevel()+ " g");
        holder.tv_gasSavingsTotalThisLevel.setText(context.getString(R.string.gas_savings_total_semicolon) +" "+ levelList.get(position).getGasSavingsTotalThisLevel() + " kWh");

    }

    public int getItemCount() {
        int itemCount = 0;
        if (levelList !=null) {
            itemCount = levelList.size();
        }
        return itemCount;
    }

}

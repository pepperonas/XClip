package io.celox.xclip.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.celox.xclip.R;
import io.celox.xclip.data.ClipboardEntity;

public class ClipboardAdapter extends RecyclerView.Adapter<ClipboardAdapter.ClipboardViewHolder> {

    private List<ClipboardEntity> clipboardList = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(ClipboardEntity entity);
    }

    public ClipboardAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ClipboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clipboard, parent, false);
        return new ClipboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipboardViewHolder holder, int position) {
        ClipboardEntity entity = clipboardList.get(position);
        holder.bind(entity, clickListener);
    }

    @Override
    public int getItemCount() {
        return clipboardList.size();
    }

    public void setClipboardList(List<ClipboardEntity> list) {
        this.clipboardList = list;
        notifyDataSetChanged();
    }

    public ClipboardEntity getClipboardAt(int position) {
        return clipboardList.get(position);
    }

    static class ClipboardViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView timestampView;

        public ClipboardViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_clipboard);
            timestampView = itemView.findViewById(R.id.text_timestamp);
        }

        public void bind(ClipboardEntity entity, OnItemClickListener listener) {
            textView.setText(entity.getText());

            // Zeitstempel formatieren
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    entity.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            timestampView.setText(relativeTime);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(entity);
                }
            });
        }
    }
}

package io.celox.xclip.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.celox.xclip.R;
import io.celox.xclip.data.ClipboardEntity;

public class ClipboardDialog extends DialogFragment {

    private ClipboardViewModel viewModel;
    private ClipboardAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_clipboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel initialisieren
        viewModel = new ViewModelProvider(this).get(ClipboardViewModel.class);

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Adapter setup
        adapter = new ClipboardAdapter(entity -> {
            // Item clicked - copy to clipboard and close dialog
            copyToClipboard(entity.getText());
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ClipboardEntity entity = adapter.getClipboardAt(position);
                viewModel.delete(entity);
                Toast.makeText(getContext(), "Eintrag gelöscht", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        // Observer für Datenänderungen
        viewModel.getAllClipboards().observe(getViewLifecycleOwner(), clipboards -> {
            adapter.setClipboardList(clipboards);
        });

        // Close button
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager)
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("XClip", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "In Zwischenablage kopiert", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }
}

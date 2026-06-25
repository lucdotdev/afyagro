package com.example.afyagro.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afyagro.R;
import com.example.afyagro.models.Delivery;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Liste des livraisons pour l'administrateur.
 *
 * Chaque élément permet à l'admin de :
 *  - confirmer la date et l'heure de livraison (DatePicker + TimePicker) ;
 *  - signaler un retard avec une note ;
 *  - marquer la livraison comme effectuée.
 *
 * Toutes ces actions mettent à jour le document correspondant dans la
 * collection Firestore "deliveries".
 */
public class DeliveryRecycleAdapter
        extends FirestoreRecyclerAdapter<Delivery, DeliveryRecycleAdapter.DeliveryViewHolder> {

    private final Context context;
    private final OnEmptyList onEmptyList;

    public DeliveryRecycleAdapter(@NonNull FirestoreRecyclerOptions<Delivery> options,
                                  Context context, OnEmptyList onEmptyList) {
        super(options);
        this.context = context;
        this.onEmptyList = onEmptyList;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_delivery_item, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (onEmptyList != null) {
            onEmptyList.onEmpty(getItemCount() != 0);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position,
                                    @NonNull final Delivery model) {

        final String docId = model.getUid();

        holder.itemName.setText(context.getString(R.string.delivery_label_item)
                + safe(model.getItemName()));
        holder.buyer.setText(context.getString(R.string.delivery_label_buyer)
                + safe(model.getBuyerName()));
        holder.status.setText(context.getString(R.string.delivery_label_status)
                + statusLabel(model.getStatus()));

        String date = TextUtils.isEmpty(model.getDeliveryDate())
                ? context.getString(R.string.delivery_not_set) : model.getDeliveryDate();
        String time = TextUtils.isEmpty(model.getDeliveryTime())
                ? context.getString(R.string.delivery_not_set) : model.getDeliveryTime();
        holder.dateTime.setText(context.getString(R.string.delivery_label_date) + date
                + "   " + context.getString(R.string.delivery_label_time) + time);

        if (TextUtils.isEmpty(model.getDelayNote())) {
            holder.delay.setVisibility(View.GONE);
        } else {
            holder.delay.setVisibility(View.VISIBLE);
            holder.delay.setText(context.getString(R.string.delivery_label_delay)
                    + model.getDelayNote());
        }

        holder.confirmDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDateThenTime(docId);
            }
        });

        holder.reportDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDelayDialog(docId, model.getDelayNote());
            }
        });

        holder.markDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> update = new HashMap<>();
                update.put("status", Delivery.STATUS_DELIVERED);
                applyUpdate(docId, update);
            }
        });
    }

    private void pickDateThenTime(final String docId) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        final String pickedDate = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timeView, int hourOfDay, int minute) {
                                        String pickedTime = String.format(Locale.getDefault(),
                                                "%02d:%02d", hourOfDay, minute);
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("deliveryDate", pickedDate);
                                        update.put("deliveryTime", pickedTime);
                                        update.put("status", Delivery.STATUS_CONFIRMED);
                                        applyUpdate(docId, update);
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showDelayDialog(final String docId, String currentNote) {
        final EditText input = new EditText(context);
        input.setHint(R.string.admin_delay_hint);
        if (!TextUtils.isEmpty(currentNote)) {
            input.setText(currentNote);
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.admin_report_delay)
                .setView(input)
                .setPositiveButton(R.string.admin_save, (dialog, which) -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("delayNote", input.getText().toString().trim());
                    update.put("status", Delivery.STATUS_DELAYED);
                    applyUpdate(docId, update);
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    private void applyUpdate(String docId, Map<String, Object> update) {
        FirebaseFirestore.getInstance()
                .collection("deliveries")
                .document(docId)
                .update(update)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, R.string.admin_updated, Toast.LENGTH_SHORT).show());
    }

    private String statusLabel(String status) {
        if (status == null) {
            return context.getString(R.string.delivery_status_pending);
        }
        switch (status) {
            case Delivery.STATUS_CONFIRMED:
                return context.getString(R.string.delivery_status_confirmed);
            case Delivery.STATUS_DELAYED:
                return context.getString(R.string.delivery_status_delayed);
            case Delivery.STATUS_DELIVERED:
                return context.getString(R.string.delivery_status_delivered);
            case Delivery.STATUS_PENDING:
            default:
                return context.getString(R.string.delivery_status_pending);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        final TextView itemName;
        final TextView buyer;
        final TextView status;
        final TextView dateTime;
        final TextView delay;
        final Button confirmDateTime;
        final Button reportDelay;
        final Button markDelivered;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.deliveryItemName);
            buyer = itemView.findViewById(R.id.deliveryBuyer);
            status = itemView.findViewById(R.id.deliveryStatus);
            dateTime = itemView.findViewById(R.id.deliveryDateTime);
            delay = itemView.findViewById(R.id.deliveryDelay);
            confirmDateTime = itemView.findViewById(R.id.confirmDateTime);
            reportDelay = itemView.findViewById(R.id.reportDelay);
            markDelivered = itemView.findViewById(R.id.markDelivered);
        }
    }

    public interface OnEmptyList {
        void onEmpty(boolean hasItems);
    }
}

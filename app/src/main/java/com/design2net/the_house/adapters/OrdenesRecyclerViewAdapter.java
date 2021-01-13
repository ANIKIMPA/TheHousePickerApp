package com.design2net.the_house.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.design2net.the_house.R;
import com.design2net.the_house.models.Orden;

import java.util.List;

public class OrdenesRecyclerViewAdapter extends RecyclerView.Adapter<OrdenesRecyclerViewAdapter.ViewHolder> {

    private final List<Orden> mOrdenes;
    private OnOrdenListener mOnOrdenListener;

    public OrdenesRecyclerViewAdapter(List<Orden> ordenes, OnOrdenListener onOrdenListener) {
        this.mOrdenes = ordenes;
        this.mOnOrdenListener = onOrdenListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_orden, viewGroup, false);

        return new ViewHolder(vista, mOnOrdenListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (position != RecyclerView.NO_POSITION) {

            viewHolder.txtOrderNumber.setText(mOrdenes.get(position).orderNumber);
            viewHolder.txtCliente.setText(mOrdenes.get(position).nombreCliente);
            viewHolder.txtCantidadProductos.setText("PRD: " + mOrdenes.get(position).productsCompleted + "/" + mOrdenes.get(position).cantidadProductos + " (" + mOrdenes.get(position).getPorcientoCompletado() + "%)");
            viewHolder.txtHorario.setText(mOrdenes.get(position).horaDesde + " - " + mOrdenes.get(position).horaHasta);
            viewHolder.txtUser.setText(Html.fromHtml("<b>" +mOrdenes.get(position).tarea + ":</b> " + (mOrdenes.get(position).user.isEmpty() ? "-" : mOrdenes.get(position).user)));

            if (mOrdenes.get(position).not_available_products > 0) {
                viewHolder.txtViewNoDisponible.setVisibility(View.VISIBLE);
                viewHolder.txtViewNoDisponible.setText(mOrdenes.get(position).not_available_products + " No disponible");
            }
            else {
                viewHolder.txtViewNoDisponible.setVisibility(View.GONE);
            }

            if (mOrdenes.get(position).tipo_entrega.equals("pickup"))
                viewHolder.txtTipo.setText("Recogido");
            else
                viewHolder.txtTipo.setText("Entrega");

            viewHolder.imgViewNetAvailableFlag.setVisibility(mOrdenes.get(position).netAvailableFlag == 1 ? View.VISIBLE : View.GONE);
            viewHolder.setData(position);
        }
    }

    @Override
    public int getItemCount() {
        return mOrdenes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private TextView txtOrderNumber;
        private TextView txtCliente;
        private TextView txtCantidadProductos;
        private TextView txtHorario;
        private TextView txtUser;
        private TextView txtTipo;
        private TextView txtViewNoDisponible;
        private ImageView imgViewNetAvailableFlag;
        OnOrdenListener onOrdenListener;

        ViewHolder(@NonNull View itemView, OnOrdenListener onOrdenListener) {
            super(itemView);

            this.mView = itemView;
            this.txtOrderNumber = itemView.findViewById(R.id.txtOrderNumber);
            this.txtCliente = itemView.findViewById(R.id.txtViewCliente);
            this.txtCantidadProductos = itemView.findViewById(R.id.txtQtyProductos);
            this.txtHorario = itemView.findViewById(R.id.txtHorario);
            this.txtUser = itemView.findViewById(R.id.txtViewUser);
            this.txtTipo = itemView.findViewById(R.id.txtViewTipo);
            this.txtViewNoDisponible = itemView.findViewById(R.id.txtViewNoDisponible);
            this.imgViewNetAvailableFlag = itemView.findViewById(R.id.imgViewNetAvailableFlag);
            this.onOrdenListener = onOrdenListener;
        }

        void setData(final int position) {
            if (mOrdenes.get(position).getPorcientoCompletado() >= 100)
                mView.setBackgroundResource(R.color.good);
            else if (mOrdenes.get(position).productsCompleted > 0)
                mView.setBackgroundResource(R.color.in_process);
            else
                mView.setBackgroundResource(R.color.trasparent);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOrdenListener.onOrdenClick(position);
                }
            });
        }
    }

    public interface OnOrdenListener {
        void onOrdenClick(int position);
    }
}
